package local.zipshortcut;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
import android.graphics.Rect;
public final class ReviewRegressionTest {
    static int checks;
    static void check(boolean value) { checks++;if(!value)throw new AssertionError("review regression "+checks); }
    static AccessibilityNodeInfo node(String label,boolean clickable) { AccessibilityNodeInfo n=new AccessibilityNodeInfo(label);n.clickable=clickable;return n; }
    static AccessibilityNodeInfo composer() { AccessibilityNodeInfo n=node("Message zip",false);n.editable=true;return n; }
    static ZipService service() {
        SystemClock.now=100;ZipService s=new ZipService();
        s.preferences.edit().putLong("startedAtElapsed",100).putInt("boot",1).putBoolean("consent",true)
                .putString("requestProcess",CallSettings.PROCESS_ID).putString("call","REAL_CALL").putString("connected","REAL_END").apply();
        s.root=node("root",false);s.begin();return s;
    }
    static void tick(ZipService s,long now) { SystemClock.now=now;s.step(); }
    static class ChangingControl extends AccessibilityNodeInfo {
        final String change;
        ChangingControl(String change) {super("REAL_CALL");clickable=true;this.change=change;}
        @Override public boolean refresh() {
            if(change.equals("label"))label="REAL_END";
            if(change.equals("package"))pkg="other.app";
            if(change.equals("window"))window=2;
            if(change.equals("disabled"))enabled=false;
            return true;
        }
    }
    static class MovingControl extends AccessibilityNodeInfo {
        boolean moved;
        MovingControl(){super("REAL_CALL");clickable=true;}
        @Override public boolean refresh(){moved=true;return true;}
        @Override public void getBoundsInScreen(Rect r){r.x=moved?2:1;}
    }
    static class ChangesAfterReservation extends AccessibilityNodeInfo {
        int refreshes;
        ChangesAfterReservation(){super("REAL_CALL");clickable=true;}
        @Override public boolean refresh(){if(++refreshes>=2)label="REAL_END";return true;}
    }
    static class ReplacedControl extends AccessibilityNodeInfo {
        ReplacedControl(){super("REAL_CALL");clickable=true;}
        @Override public boolean refresh(){parent.children.set(1,node("REAL_CALL",true));return true;}
    }
    static class BodyText extends AccessibilityNodeInfo {
        BodyText(String text,boolean clickable){super(text);this.clickable=clickable;}
        @Override public CharSequence getClassName(){return "android.widget.TextView";}
    }
    static ZipService duplicatesOnRead(String label,int trigger) {
        android.content.SharedPreferences prefs=service().preferences;
        ZipService service=new ZipService(){int reads;
            @Override public AccessibilityNodeInfo getRootInActiveWindow() {
                if(++reads==trigger)root.add(node(label,true)).add(node(label,true));
                return root;
            }};
        service.preferences=prefs;service.root=node("root",false).add(composer()).add(node("REAL_CALL",true));
        service.begin();return service;
    }
    public static void main(String[] args) {
        // 1. Post-refresh changes never dispatch a click, including unchanged screen bounds.
        for(String change:new String[]{"label","package","window","disabled"}) {
            ZipService s=service();ChangingControl call=new ChangingControl(change);s.root.add(composer()).add(call);
            tick(s,100);tick(s,600);check(call.clicks==0);check(!s.preferences.getBoolean("callAttempted",false));
        }
        ZipService s=service();MovingControl moving=new MovingControl();s.root.add(composer()).add(moving);
        tick(s,100);tick(s,600);check(moving.clicks==0);
        s=service();ChangesAfterReservation late=new ChangesAfterReservation();s.root.add(composer()).add(late);
        tick(s,100);tick(s,600);check(late.clicks==0);check(!s.running);tick(s,1000);check(late.clicks==0);
        s=service();ReplacedControl replaced=new ReplacedControl();s.root.add(composer()).add(replaced);
        tick(s,100);tick(s,600);check(replaced.clicks==0);check(s.root.children.get(1).clicks==0);
        // 2. Service reconnect before the click and a new process both fail closed.
        s=service();s.root.add(composer()).add(node("REAL_CALL",true));tick(s,100);
        ZipService reconnected=new ZipService();reconnected.preferences=s.preferences;
        reconnected.root=node("other",false);reconnected.root.pkg="other.app";reconnected.begin();
        check(!reconnected.running);check(!reconnected.armed());
        AccessibilityNodeInfo later=node("REAL_CALL",true);reconnected.root=node("root",false).add(composer()).add(later);
        tick(reconnected,600);check(later.clicks==0);
        s=service();s.preferences.edit().putString("requestProcess","previous-process").apply();
        ZipService newProcess=new ZipService();newProcess.preferences=s.preferences;newProcess.root=s.root;newProcess.begin();
        check(!newProcess.running);check(!newProcess.armed());
        s=service();ZipService initialConnection=new ZipService();initialConnection.preferences=s.preferences;
        AccessibilityNodeInfo callForInitial=node("REAL_CALL",true);initialConnection.root=node("root",false).add(composer()).add(callForInitial);
        initialConnection.begin();check(initialConnection.running);tick(initialConnection,100);tick(initialConnection,600);check(callForInitial.clicks==1);
        // 3. Null/stale trees pause and reset evidence, not a click or immediate cancellation.
        s=service();AccessibilityNodeInfo call=node("REAL_CALL",true);AccessibilityNodeInfo root=s.root.add(composer()).add(call);
        tick(s,100);s.root=null;tick(s,200);check(s.running);check(call.clicks==0);
        s.root=root;tick(s,600);check(call.clicks==0);tick(s,1100);check(call.clicks==1);
        s=service();s.root.add(composer());tick(s,100);s.root=null;tick(s,200);tick(s,1200);check(!s.running);
        s=service();call=node("REAL_CALL",true);root=s.root.add(composer()).add(call);tick(s,100);
        s.root=new AccessibilityNodeInfo("stale"){@Override public boolean refresh(){return false;}};
        tick(s,200);check(s.running);s.root=root;tick(s,300);check(call.clicks==0);tick(s,800);check(call.clicks==1);
        // Known different package still cancels immediately.
        s=service();tick(s,100);s.root.pkg="other.app";tick(s,200);check(!s.running);
        // 4. Check both raw and resolved labels. A name change can introduce a collision.
        String[] labels={"Message {dot}","Menu","Scheduled","Navigate up","REAL_CALL","REAL_END"};
        check(CallSettings.valid(labels,"zip"));String[] colliding=labels.clone();colliding[5]="Message zip";
        check(!CallSettings.valid(colliding,"zip"));check(CallSettings.valid(colliding,"other"));
        check(!CallSettings.valid(labels,""));String[] duplicate=labels.clone();duplicate[5]="REAL_CALL";check(!CallSettings.valid(duplicate,"zip"));
        // 5. Revocation does not depend on valid name/labels and clears the pending request.
        s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call);tick(s,100);
        check(CallSettings.store(s,"",new String[]{},false));check(!s.preferences.getBoolean("consent",true));check(!s.armed());
        tick(s,600);check(!s.running);check(call.clicks==0);check(!CallSettings.configured(s));
        check(!CallSettings.store(s,"zip",colliding,true));check(!s.preferences.getBoolean("consent",true));
        check(CallSettings.store(s,"zip",labels,true));check(CallSettings.configured(s));
        check(CallSettings.store(s,"otherDot",labels,false));check(DotSettings.name(s).equals("otherDot"));
        check(!s.preferences.getBoolean("consent",true));check(!CallSettings.configured(s));
        check(CallSettings.store(s,"",new String[]{},false));check(DotSettings.name(s).equals("otherDot"));
        check(CallSettings.store(s,"dot",labels,false,"Dot"));check(DotSettings.entry(s).equals("Dot"));
        check(DotSettings.name(s).equals("dot"));check(!CallSettings.configured(s));
        check(!CallSettings.complete(labels,"dot","REAL_CALL"));check(!CallSettings.complete(labels,"dot","Menu"));
        // 6. Transcript text, even clickable TextViews, cannot indicate sidebar or call state.
        s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call).add(new BodyText("Scheduled",false)).add(new BodyText("REAL_END",false));
        tick(s,100);tick(s,600);check(call.clicks==1);
        s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call).add(new BodyText("Scheduled",true)).add(new BodyText("REAL_END",true));
        tick(s,100);tick(s,600);check(call.clicks==1);
        s.root=node("root",false).add(new BodyText("REAL_END",false));tick(s,700);tick(s,1200);check(s.running);
        s.root=node("root",false).add(node("REAL_END",true));tick(s,1300);tick(s,1800);check(!s.running);check(s.preferences.getString("lastResult","").equals(ResultStatus.CONFIRMED));
        s=service();call=node("REAL_CALL",true);AccessibilityNodeInfo dot=node("zip",true);
        s.root.add(composer()).add(call).add(node("Scheduled",true)).add(dot);tick(s,100);tick(s,600);check(call.clicks==0);check(dot.clicks==1);
        s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call).add(new BodyText("Message zip",false));tick(s,100);tick(s,600);check(call.clicks==1);
        android.content.SharedPreferences launchPrefs=service().preferences;
        ZipService switchedDuringNavigation=new ZipService(){int reads;
            @Override public AccessibilityNodeInfo getRootInActiveWindow(){
                if(++reads==1)return root;
                AccessibilityNodeInfo foreign=node("other",false);foreign.pkg="other.app";return foreign;
            }};
        switchedDuringNavigation.preferences=launchPrefs;AccessibilityNodeInfo menu=node("Menu",true);
        switchedDuringNavigation.root=node("root",false).add(menu);switchedDuringNavigation.begin();tick(switchedDuringNavigation,100);check(menu.clicks==0);
        // 8. A duplicate state is ambiguous, never evidence that the state is absent.
        for(String marker:new String[]{"REAL_END","Scheduled"}) {
            s=service();call=node("REAL_CALL",true);dot=node("zip",true);menu=node("Menu",true);
            s.root.add(composer()).add(call).add(dot).add(menu).add(node(marker,true)).add(node(marker,true));
            check(s.stateCount(s.root,marker)==2);tick(s,100);tick(s,600);
            check(call.clicks==0);check(dot.clicks==0);check(menu.clicks==0);check(!s.running);
            check(s.preferences.getString("lastResult","").equals(ResultStatus.AMBIGUOUS));
            // Markers appear only after the stable-button phase, during preflight / final read.
            for(int read:new int[]{3,4,5,6}) {
                s=duplicatesOnRead(marker,read);call=s.root.children.get(1);tick(s,100);tick(s,600);
                check(call.clicks==0);check(!s.running);check(s.preferences.getString("lastResult","").equals(ResultStatus.AMBIGUOUS));
            }
            // After one call attempt, duplicate markers neither confirm nor trigger a second action.
            s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call);tick(s,100);tick(s,600);check(call.clicks==1);
            s.root=node("root",false).add(node(marker,true)).add(node(marker,true));tick(s,700);tick(s,1200);
            check(call.clicks==1);check(!s.running);check(s.preferences.getString("lastResult","").equals(ResultStatus.AMBIGUOUS));
        }
        // Navigation is semantic and label-bound too: refreshed menu controls cannot become call actions.
        s=service();AccessibilityNodeInfo changingMenu=new AccessibilityNodeInfo("Menu"){
            {clickable=true;}
            @Override public boolean refresh(){label="REAL_CALL";return true;}};
        s.root.add(changingMenu);tick(s,100);check(changingMenu.clicks==0);
        check(!CallSettings.valid(labels,"REAL_CALL"));check(!CallSettings.valid(labels,"REAL_END"));
        check(!CallSettings.valid(labels,"Menu"));
        s=service();s.running=false;s.preferences.edit().putString("dotName","dot").putString("dotEntry","Dot").putString("sidebar","Dot").apply();s.begin();
        check(s.targetEntry.equals("Dot"));AccessibilityNodeInfo actualEntry=node("Dot",true);s.root.add(actualEntry);tick(s,100);check(actualEntry.clicks==1);
        AccessibilityNodeInfo namedComposer=node("Message dot",false);namedComposer.editable=true;call=node("REAL_CALL",true);
        s.root=node("root",false).add(namedComposer).add(call);tick(s,200);tick(s,700);check(call.clicks==1);
        // 7. Persistent outcomes are names. Legacy/unknown integer IDs are never dereferenced.
        s=service();s.preferences.edit().putInt("lastStatus",Integer.MAX_VALUE).apply();s.stop(ResultStatus.CANCELLED);
        check(s.preferences.getInt("lastStatus",0)==0);check(s.preferences.getString("lastResult","").equals("cancelled"));
        check(!ZipService.matches(node("anything",false),"null"));
        check(ResultStatus.message("cancelled")==R.string.cancelled);check(ResultStatus.message("future-version-code")==0);check(ResultStatus.message(null)==0);
        System.out.println("PASS "+checks+" review regression checks (8 issues; synthetic Android, no device)");
    }
}
