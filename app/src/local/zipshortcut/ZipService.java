package local.zipshortcut;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.SharedPreferences;
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
    final Handler handler=new Handler(Looper.getMainLooper());
    final Runnable tick=this::step;
    boolean running, enteredTarget;
    CallPolicy policy;
    String targetName, lastAction="";
    String[] labels;
    long lastClick;
    int candidateWindow=-1;
    android.graphics.Rect candidateBounds;
    SharedPreferences prefs() { return getSharedPreferences("shortcut",0); }
    @Override protected void onServiceConnected() { instance=this; if(armed()) begin(); }
    @Override public void onDestroy() { stop(R.string.cancelled); instance=null; super.onDestroy(); }
    @Override public void onInterrupt() { stop(R.string.cancelled); }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(!running && armed()) begin(); else if(running) schedule(0);
    }
    boolean armed() {
        long start=prefs().getLong("startedAtElapsed",-1), now=SystemClock.elapsedRealtime();
        return prefs().getInt("boot",-2)==android.provider.Settings.Global.getInt(getContentResolver(),android.provider.Settings.Global.BOOT_COUNT,-1) && start>=0 && now>=start && now-start<CallPolicy.TOTAL_MS;
    }
    void begin() {
        if(running || !armed())return;
        if(!CallSettings.configured(this)) { stop(R.string.configure_first); return; }
        targetName=DotSettings.name(this); labels=new String[CallSettings.KEYS.length];
        for(int i=0;i<labels.length;i++)labels[i]=CallSettings.get(this,i);
        labels[0]=labels[0].replace("{dot}",targetName);
        policy=new CallPolicy(prefs().getLong("startedAtElapsed",-1),prefs().getBoolean("callAttempted",false),
                prefs().getLong("attemptedAt",-1));
        running=true; enteredTarget=false; lastAction=""; lastClick=0; candidateWindow=-1; candidateBounds=null; schedule(0);
    }
    void schedule(long delay) { handler.removeCallbacks(tick); handler.postDelayed(tick,delay); }
    void stop(int status) {
        running=false; handler.removeCallbacks(tick);
        prefs().edit().remove("startedAtElapsed").remove("armedUntil").remove("callAttempted")
                .remove("attemptedAt").putInt("lastStatus",status).apply();
        if(status!=0) Toast.makeText(this,status,Toast.LENGTH_LONG).show();
    }
    void step() {
        if(!running)return;
        long now=SystemClock.elapsedRealtime();
        if(policy.expired(now)) { stop(policy.attempted?R.string.unconfirmed:R.string.timed_out); return; }
        KeyguardManager keyguard=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        if(keyguard!=null && keyguard.isKeyguardLocked()) { stop(R.string.locked); return; }
        AccessibilityNodeInfo root=getRootInActiveWindow();
        if(!current(root)) {
            // Leaving ChatGPT cancels; never resume later and place an unexpected call.
            if(enteredTarget || policy.attempted || !lastAction.isEmpty()) { stop(R.string.cancelled); return; }
            schedule(150); return;
        }
        enteredTarget=true;
        boolean marker=has(root,labels[5]);
        if(policy.attempted) {
            if(policy.confirmed(now,marker && !has(root,labels[0]) && !has(root,labels[2]))) { stop(R.string.confirmed); return; }
            schedule(150); return;
        }
        if(marker) { stop(R.string.already_call); return; }
        boolean sidebar=has(root,labels[2]);
        boolean destination=!sidebar && destination(root)!=null;
        AccessibilityNodeInfo call=destination?callControl(root):null;
        android.graphics.Rect bounds=new android.graphics.Rect();
        if(call!=null)call.getBoundsInScreen(bounds);
        if(root.getWindowId()!=candidateWindow || candidateBounds==null || !candidateBounds.equals(bounds)) {
            policy.candidateSince=-1; candidateWindow=root.getWindowId(); candidateBounds=bounds;
        }
        if(policy.ready(now,destination,call!=null,marker)) {
            // Re-read a fresh foreground tree and verify all preconditions immediately before dispatch.
            AccessibilityNodeInfo fresh=getRootInActiveWindow();
            if(!current(fresh) || fresh.getWindowId()!=candidateWindow || has(fresh,labels[2])
                    || destination(fresh)==null || has(fresh,labels[5])) {
                policy.candidateSince=-1; schedule(150); return;
            }
            call=callControl(fresh);
            android.graphics.Rect freshBounds=new android.graphics.Rect();
            if(call!=null)call.getBoundsInScreen(freshBounds);
            now=SystemClock.elapsedRealtime();
            AccessibilityNodeInfo active=getRootInActiveWindow();
            if(call!=null && bounds.equals(freshBounds) && call.refresh() && call.isVisibleToUser() && call.isEnabled()
                    && call.isClickable() && current(active) && active.getWindowId()==candidateWindow
                    && (keyguard==null || !keyguard.isKeyguardLocked()) && policy.consume(now)) {
                // Synchronous persistence prevents a service/process restart from attempting twice.
                if(!prefs().edit().putBoolean("callAttempted",true).putLong("attemptedAt",now).commit()) {
                    stop(R.string.cancelled); return;
                }
                if(!call.performAction(AccessibilityNodeInfo.ACTION_CLICK)) { stop(R.string.click_failed); return; }
            }
        } else if(!destination) {
            String action=sidebar?"dot":"menu";
            AccessibilityNodeInfo target=unique(root,sidebar?targetName:labels[1],true);
            if(target==null && !sidebar && !labels[3].isEmpty()) { target=unique(root,labels[3],true); action="up"; }
            // Allow destination to settle; never use screen coordinates or broad label matching.
            if(!sidebar && lastAction.equals("dot") && now-lastClick<4000) { schedule(150); return; }
            if(target!=null && (!action.equals(lastAction) || now-lastClick>=1000)) {
                if(target.performAction(AccessibilityNodeInfo.ACTION_CLICK)) { lastAction=action; lastClick=now; }
            }
        }
        schedule(150);
    }
    boolean current(AccessibilityNodeInfo root) {
        return root!=null && "com.openai.chatgpt".contentEquals(root.getPackageName()==null?"":root.getPackageName()) && root.refresh();
    }
    static boolean matches(AccessibilityNodeInfo n,String value) {
        return value.equals(String.valueOf(n.getText())) || value.equals(String.valueOf(n.getContentDescription()))
                || value.equals(String.valueOf(n.getHintText()));
    }
    AccessibilityNodeInfo destination(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo node=unique(root,labels[0],false);
        return node!=null && node.isEditable()?node:null;
    }
    AccessibilityNodeInfo callControl(AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> found=new ArrayList<>(); collectCall(root,found);
        return found.size()==1?found.get(0):null;
    }
    void collectCall(AccessibilityNodeInfo node,List<AccessibilityNodeInfo> found) {
        if(node==null || found.size()>1)return;
        // Call control must itself be actionable and explicitly labelled; no ancestor/gesture fallback.
        if(node.isVisibleToUser() && node.isEnabled() && node.isClickable()
                && (labels[4].contentEquals(node.getText()==null?"":node.getText())
                || labels[4].contentEquals(node.getContentDescription()==null?"":node.getContentDescription())))found.add(node);
        for(int i=0;i<node.getChildCount();i++)collectCall(node.getChild(i),found);
    }
    boolean has(AccessibilityNodeInfo node,String label) {
        if(node==null || label.isEmpty())return false;
        if(node.isVisibleToUser() && matches(node,label))return true;
        for(int i=0;i<node.getChildCount();i++)if(has(node.getChild(i),label))return true;
        return false;
    }
    AccessibilityNodeInfo unique(AccessibilityNodeInfo root,String label,boolean clickable) {
        if(label.isEmpty())return null;
        List<AccessibilityNodeInfo> found=new ArrayList<>(); collect(root,label,clickable,found);
        return found.size()==1?found.get(0):null;
    }
    void collect(AccessibilityNodeInfo node,String label,boolean clickable,List<AccessibilityNodeInfo> out) {
        if(node==null || out.size()>1)return;
        if(node.isVisibleToUser() && node.isEnabled() && matches(node,label)) {
            AccessibilityNodeInfo target=node;
            if(clickable) {
                for(int i=0;i<6 && target!=null && !target.isClickable();i++)target=target.getParent();
                if(target!=null && (!target.isEnabled() || !target.isVisibleToUser()))target=null;
            }
            if(target!=null && !out.contains(target))out.add(target);
        }
        for(int i=0;i<node.getChildCount();i++)collect(node.getChild(i),label,clickable,out);
    }
}
