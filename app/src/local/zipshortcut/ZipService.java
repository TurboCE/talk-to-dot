package local.zipshortcut;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class ZipService extends AccessibilityService {
    static ZipService instance;
    static final long TREE_GRACE_MS=1000;
    final Handler handler=new Handler(Looper.getMainLooper());
    final Runnable tick=this::step;
    final Runnable inspectTick=this::inspectStep;
    static String inspectionReport="";
    long inspectionDeadline, inspectedMenuAt;
    boolean inspectMenuMode, inspectionMenuAttempted, inspectEntryMode, inspectionEntryAttempted;
    long inspectedEntryAt;
    String inspectionTarget;
    boolean running, enteredTarget;
    CallPolicy policy;
    String targetName, targetEntry, lastAction="";
    String[] labels;
    long lastClick, unavailableSince=-1;
    int candidateWindow=-1;
    Rect candidateBounds;
    SharedPreferences prefs() { return getSharedPreferences("shortcut",0); }
    @Override protected void onServiceConnected() { instance=this; if(armed())begin(); }
    @Override public void onDestroy() { handler.removeCallbacks(inspectTick); inspectionDeadline=0; if(running)stop(ResultStatus.CANCELLED); instance=null; super.onDestroy(); }
    @Override public void onInterrupt() { handler.removeCallbacks(inspectTick); inspectionDeadline=0; if(running)stop(ResultStatus.CANCELLED); }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(!running && armed())begin(); else if(running)schedule(0);
    }
    void inspectOnce() {
        if(running)stop(ResultStatus.CANCELLED);
        CallSettings.clearRequest(prefs().edit()).commit();
        inspectionReport="";inspectionDeadline=SystemClock.elapsedRealtime()+10000;
        inspectMenuMode=false;inspectionMenuAttempted=false;inspectEntryMode=false;inspectionEntryAttempted=false;
        inspectedEntryAt=0;inspectedMenuAt=0;inspectionTarget="";
        handler.removeCallbacks(inspectTick);handler.postDelayed(inspectTick,0);
    }
    void inspectMenuOnce(String name) {
        inspectOnce();inspectMenuMode=true;
        inspectionTarget=name==null?"":name.trim();
        if(inspectionTarget.isEmpty() || inspectionTarget.length()>80)finishInspection("Invalid target; no action performed.");
    }
    void inspectEntryOnce() {inspectOnce();inspectEntryMode=true;inspectionTarget="Dot";}
    void finishInspection(String report) {
        inspectionReport=report;inspectionDeadline=0;
        Toast.makeText(this,(inspectMenuMode || inspectEntryMode)?R.string.inspect_menu_done:R.string.inspect_done,Toast.LENGTH_LONG).show();
    }
    void inspectStep() {
        if(inspectionDeadline==0)return;
        long now=SystemClock.elapsedRealtime();
        if(now>=inspectionDeadline) {
            inspectionReport=inspectEntryMode?"Entry-only inspection timed out; entry attempts="+(inspectionEntryAttempted?1:0)+"; no call dispatch.\n"+inspectionReport:inspectMenuMode?"Menu-only inspection timed out; menu attempts="+(inspectionMenuAttempted?1:0)+"; no call dispatch.\n"+inspectionReport:"Read-only inspection timed out; no actions.";
            inspectionDeadline=0;Toast.makeText(this,R.string.inspect_timeout,Toast.LENGTH_LONG).show();return;
        }
        AccessibilityNodeInfo root=getRootInActiveWindow();
        if(current(root)) {
            if(inspectEntryMode) {
                inspectEntryStep(root,now);return;
            }
            if(inspectMenuMode && !inspectionMenuAttempted) {
                AccessibilityNodeInfo menu=MenuInspector.candidate(root);
                if(menu==null && ScreenInspector.targetLabels(root,inspectionTarget)>0) {
                    finishInspection("Menu attempts=0; exact target metadata only, not a validated selection.\n"+ScreenInspector.targetSummary(root,inspectionTarget));return;
                }
                if(menu!=null) {
                    AccessibilityNodeInfo active=getRootInActiveWindow();
                    if(!menu.refresh() || !current(active) || !ControlResolver.control(menu,MenuInspector.MENU,true)
                            || !menu.equals(MenuInspector.candidate(active)) || locked()) {
                        finishInspection("Menu changed before dispatch; no action performed.");return;
                    }
                    // This diagnostic dispatches only the fixed, verified MENU binding, never a setting/call label.
                    inspectionMenuAttempted=true;inspectedMenuAt=now;
                    if(!menu.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {finishInspection("Menu action failed; no retry and no call dispatch.");return;}
                    handler.postDelayed(inspectTick,300);return;
                }
            } else if(inspectMenuMode) {
                inspectionReport="Menu attempts=1; exact target metadata only, not a validated selection.\n"+ScreenInspector.targetSummary(root,inspectionTarget);
                if(now-inspectedMenuAt>=300 && ScreenInspector.targetLabels(root,inspectionTarget)>0) {
                    finishInspection(inspectionReport);return;
                }
            } else {finishInspection(ScreenInspector.scan(root));return;}
        }
        handler.postDelayed(inspectTick,150);
    }
    void inspectEntryStep(AccessibilityNodeInfo root,long now) {
        if(!inspectionEntryAttempted) {
            AccessibilityNodeInfo entry=NativeNavigation.dotEntry(root);
            if(entry!=null) {
                AccessibilityNodeInfo active=getRootInActiveWindow();
                if(!entry.refresh() || !current(active) || locked() || NativeNavigation.rowEvidence(entry,"Dot")!=NativeNavigation.VERIFIED
                        || !entry.equals(NativeNavigation.dotEntry(active))) {
                    finishInspection("Entry binding changed; no action performed.");return;
                }
                inspectionEntryAttempted=true;inspectedEntryAt=now;
                if(!entry.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {finishInspection("Entry action failed; no retry and no call dispatch.");return;}
                handler.postDelayed(inspectTick,300);return;
            }
            inspectionReport=ScreenInspector.targetSummary(root,"Dot");
        } else if(now-inspectedEntryAt>=300 && MenuInspector.candidate(root)!=null) {
            finishInspection("Dot-entry-only; entry attempts=1; no call dispatch.\n"+ScreenInspector.scan(root));return;
        }
        handler.postDelayed(inspectTick,150);
    }
    boolean armed() {
        long start=prefs().getLong("startedAtElapsed",-1), now=SystemClock.elapsedRealtime();
        return prefs().getInt("boot",-2)==android.provider.Settings.Global.getInt(getContentResolver(),android.provider.Settings.Global.BOOT_COUNT,-1)
                && start>=0 && now>=start && now-start<CallPolicy.TOTAL_MS;
    }
    void begin() {
        if(running || !armed())return;
        boolean attempted=prefs().getBoolean("callAttempted",false);
        // A new process cannot infer what happened while it was absent. Never resume an old request.
        // A same-process service reconnect before its click also cancels if ChatGPT was already seen.
        if(!CallSettings.PROCESS_ID.equals(prefs().getString("requestProcess",""))
                || (!attempted && prefs().getBoolean("observedTarget",false))) { stop(ResultStatus.CANCELLED); return; }
        if(!CallSettings.configured(this)) { stop(ResultStatus.CONFIGURE); return; }
        targetName=DotSettings.name(this);targetEntry=DotSettings.entry(this); labels=new String[CallSettings.KEYS.length];
        for(int i=0;i<labels.length;i++)labels[i]=CallSettings.get(this,i);
        labels=CallSettings.resolved(labels,targetName);
        policy=new CallPolicy(prefs().getLong("startedAtElapsed",-1),attempted,prefs().getLong("attemptedAt",-1));
        running=true; enteredTarget=false; unavailableSince=-1; lastAction=""; lastClick=0;
        candidateWindow=-1; candidateBounds=null; schedule(0);
    }
    void schedule(long delay) { handler.removeCallbacks(tick); handler.postDelayed(tick,delay); }
    void stop(String status) {
        running=false; handler.removeCallbacks(tick);
        CallSettings.clearRequest(prefs().edit()).remove("lastStatus").putString("lastResult",status).commit();
        int message=ResultStatus.message(status);
        if(message!=0)Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }
    void resetEvidence() { policy.candidateSince=-1; policy.markerSince=-1; candidateWindow=-1; candidateBounds=null; }
    void unavailable(long now) {
        resetEvidence();
        if(unavailableSince<0)unavailableSince=now;
        if(enteredTarget && now-unavailableSince>=TREE_GRACE_MS) { stop(ResultStatus.CANCELLED); return; }
        schedule(150);
    }
    void step() {
        if(!running)return;
        long now=SystemClock.elapsedRealtime();
        if(!prefs().getBoolean("consent",false)) { stop(ResultStatus.CANCELLED); return; }
        if(policy.expired(now)) { stop(policy.attempted?ResultStatus.UNCONFIRMED:ResultStatus.TIMEOUT); return; }
        if(locked()) { stop(ResultStatus.LOCKED); return; }
        AccessibilityNodeInfo root=getRootInActiveWindow();
        // A missing/unrefreshable tree is not proof of app departure. Observe briefly without clicking.
        if(root==null || !root.refresh()) { unavailable(now); return; }
        if(!chatGpt(root)) {
            resetEvidence();
            if(enteredTarget || policy.attempted) { stop(ResultStatus.CANCELLED); return; }
            schedule(150); return;
        }
        unavailableSince=-1;
        if(!enteredTarget) {
            if(!prefs().edit().putBoolean("observedTarget",true).commit()) { stop(ResultStatus.CANCELLED); return; }
            enteredTarget=true;
        }
        boolean composer=destination(root)!=null;
        // Preserve absent / unique / ambiguous states; duplicate controls must never imply absence.
        int markerCount=stateCount(root,labels[5]), sidebarCount=stateCount(root,labels[2]);
        if(markerCount>1 || sidebarCount>1) { stop(ResultStatus.AMBIGUOUS); return; }
        boolean marker=markerCount==1, sidebar=sidebarCount==1;
        if(policy.attempted) {
            if(policy.confirmed(now,marker && !composer && !sidebar)) { stop(ResultStatus.CONFIRMED); return; }
            schedule(150); return;
        }
        if(marker) { stop(ResultStatus.ALREADY); return; }
        if(composer && !sidebar && NativeNavigation.drawerEvidence(root)!=NativeNavigation.NONE) {stop(ResultStatus.AMBIGUOUS);return;}
        boolean destination=composer && !sidebar;
        AccessibilityNodeInfo call=destination?callControl(root):null;
        Rect bounds=new Rect(); if(call!=null)call.getBoundsInScreen(bounds);
        if(root.getWindowId()!=candidateWindow || candidateBounds==null || !candidateBounds.equals(bounds)) {
            policy.candidateSince=-1; candidateWindow=root.getWindowId(); candidateBounds=bounds;
        }
        if(policy.ready(now,destination,call!=null,marker)) {
            AccessibilityNodeInfo checked=dispatchCandidate(call,bounds,candidateWindow);
            if(checked==null) { if(running) { resetEvidence(); schedule(150); } return; }
            now=SystemClock.elapsedRealtime();
            if(policy.consume(now)) {
                // Reserve synchronously, then revalidate after potentially blocking disk persistence.
                if(!prefs().edit().putBoolean("callAttempted",true).putLong("attemptedAt",now).commit()) { stop(ResultStatus.CANCELLED); return; }
                checked=dispatchCandidate(checked,bounds,candidateWindow);
                if(!running)return;
                if(checked==null || policy.expired(SystemClock.elapsedRealtime())) { stop(ResultStatus.CANCELLED); return; }
                if(!checked.performAction(AccessibilityNodeInfo.ACTION_CLICK)) { stop(ResultStatus.CLICK_FAILED); return; }
            }
        } else if(!destination) {
            String action=sidebar?"dot":"menu", expectedLabel=sidebar?targetEntry:labels[1];
            AccessibilityNodeInfo target=NativeNavigation.unique(root,expectedLabel);
            if(target==null && !sidebar && !labels[3].isEmpty()) {
                expectedLabel=labels[3];target=NativeNavigation.unique(root,expectedLabel);action="up";
            }
            if(!sidebar && lastAction.equals("dot") && now-lastClick<4000) { schedule(150); return; }
            if(target!=null && (!action.equals(lastAction) || now-lastClick>=1000) && navigationAllowed(root,target,expectedLabel)) {
                if(target.performAction(AccessibilityNodeInfo.ACTION_CLICK)) { lastAction=action; lastClick=now; }
            }
        }
        if(running)schedule(150);
    }
    boolean navigationAllowed(AccessibilityNodeInfo previous,AccessibilityNodeInfo target,String expectedLabel) {
        AccessibilityNodeInfo active=getRootInActiveWindow();
        if(!current(active))return false;
        if(ambiguousState(active)) { stop(ResultStatus.AMBIGUOUS); return false; }
        if(stateCount(active,labels[5])>0) { stop(ResultStatus.ALREADY); return false; }
        if(expectedLabel.equals(labels[4]) || expectedLabel.equals(labels[5]))return false;
        if(policy.expired(SystemClock.elapsedRealtime()) || locked() || active.getWindowId()!=previous.getWindowId()
                || !target.equals(NativeNavigation.unique(active,expectedLabel)) || !target.refresh())return false;
        AccessibilityNodeInfo latest=getRootInActiveWindow();
        return current(latest) && latest.getWindowId()==previous.getWindowId() && !ambiguousState(latest)
                && stateCount(latest,labels[5])==0 && target.equals(NativeNavigation.unique(latest,expectedLabel))
                && NativeNavigation.evidence(target,expectedLabel)==NativeNavigation.VERIFIED && chatGpt(target)
                && target.getWindowId()==latest.getWindowId() && !locked() && !policy.expired(SystemClock.elapsedRealtime());
    }
    boolean locked() {
        KeyguardManager keyguard=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        return keyguard!=null && keyguard.isKeyguardLocked();
    }
    AccessibilityNodeInfo dispatchCandidate(AccessibilityNodeInfo expected,Rect bounds,int window) {
        if(expected==null || locked() || !prefs().getBoolean("consent",false))return null;
        AccessibilityNodeInfo root=getRootInActiveWindow();
        if(!current(root) || root.getWindowId()!=window || destination(root)==null || NativeNavigation.drawerEvidence(root)!=NativeNavigation.NONE
                || !statesClearForCall(root))return null;
        AccessibilityNodeInfo call=callControl(root);
        if(call==null || !call.equals(expected) || !call.refresh())return null;
        Rect refreshedBounds=new Rect();call.getBoundsInScreen(refreshedBounds);
        // refresh() can change label, window, package, bounds and actionability: check all of them AFTER it.
        if(!callLabel(call) || !chatGpt(call) || call.getWindowId()!=window || !bounds.equals(refreshedBounds))return null;
        AccessibilityNodeInfo active=getRootInActiveWindow();
        if(!current(active) || active.getWindowId()!=window || destination(active)==null || NativeNavigation.drawerEvidence(active)!=NativeNavigation.NONE
                || !statesClearForCall(active))return null;
        AccessibilityNodeInfo finalCall=callControl(active);
        if(finalCall==null || !finalCall.equals(call) || !callLabel(finalCall) || !chatGpt(finalCall)
                || finalCall.getWindowId()!=window || locked())return null;
        Rect finalBounds=new Rect();finalCall.getBoundsInScreen(finalBounds);
        return bounds.equals(finalBounds)?finalCall:null;
    }
    boolean chatGpt(AccessibilityNodeInfo root) {
        return root!=null && "com.openai.chatgpt".contentEquals(root.getPackageName()==null?"":root.getPackageName());
    }
    boolean current(AccessibilityNodeInfo root) { return root!=null && root.refresh() && chatGpt(root); }
    static boolean equalsLabel(CharSequence actual,String wanted) { return actual!=null && wanted.contentEquals(actual); }
    static boolean matches(AccessibilityNodeInfo n,String value) {
        return equalsLabel(n.getText(),value) || equalsLabel(n.getContentDescription(),value) || equalsLabel(n.getHintText(),value);
    }
    static boolean explicitLabel(AccessibilityNodeInfo n,String value) { return equalsLabel(n.getText(),value) || equalsLabel(n.getContentDescription(),value); }
    AccessibilityNodeInfo destination(AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> inputs=new ArrayList<>();collectInputs(root,inputs);
        return inputs.size()==1?inputs.get(0):null;
    }
    void collectInputs(AccessibilityNodeInfo node,List<AccessibilityNodeInfo> inputs) {
        if(node==null || inputs.size()>1)return;
        if(ControlResolver.input(node,labels[0]))inputs.add(node);
        for(int i=0;i<node.getChildCount();i++)collectInputs(node.getChild(i),inputs);
    }
    boolean callLabel(AccessibilityNodeInfo node) {
        return ControlResolver.control(node,labels[4],true);
    }
    AccessibilityNodeInfo callControl(AccessibilityNodeInfo root) { return directControl(root,labels[4],true); }
    int stateCount(AccessibilityNodeInfo root,String label) {
        if(label.isEmpty())return 0;
        if(label.equals(labels[2]))return NativeNavigation.match(root,label,false).state();
        List<AccessibilityNodeInfo> found=new ArrayList<>();collectControls(root,label,true,found);
        return found.size(); // Saturated at two: absent, unique, or ambiguous.
    }
    boolean ambiguousState(AccessibilityNodeInfo root) { return stateCount(root,labels[2])>1 || stateCount(root,labels[5])>1; }
    boolean statesClearForCall(AccessibilityNodeInfo root) {
        int sidebar=stateCount(root,labels[2]), marker=stateCount(root,labels[5]);
        if(sidebar>1 || marker>1) { stop(ResultStatus.AMBIGUOUS); return false; }
        return sidebar==0 && marker==0;
    }
    AccessibilityNodeInfo directControl(AccessibilityNodeInfo root,String label,boolean buttonRole) {
        if(label.isEmpty())return null;
        List<AccessibilityNodeInfo> found=new ArrayList<>();collectControls(root,label,buttonRole,found);
        return found.size()==1?found.get(0):null;
    }
    void collectControls(AccessibilityNodeInfo node,String label,boolean buttonRole,List<AccessibilityNodeInfo> found) {
        if(node==null || found.size()>1)return;
        if(ControlResolver.control(node,label,buttonRole))found.add(node);
        for(int i=0;i<node.getChildCount();i++)collectControls(node.getChild(i),label,buttonRole,found);
    }
}
