package local.zipshortcut;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
public final class BixbyHandoffTest {
 static int checks;
 static void check(boolean v){checks++;if(!v)throw new AssertionError("Bixby handoff check "+checks);}
 static AccessibilityNodeInfo bixby(){AccessibilityNodeInfo n=FoldControlResolverTest.view("",false);n.pkg=BixbyHandoff.PACKAGE;return n;}
 static ZipService service(){ZipService s=FoldControlResolverTest.service();s.preferences.edit().putBoolean("dismissBixby",true).apply();s.root=bixby();return s;}
 static void tick(ZipService s,long t){SystemClock.now=t;s.step();}
 static void waitReady(ZipService s){tick(s,100);tick(s,600);}
 public static void main(String[] args){
  ZipService s=service();tick(s,100);check(s.globalBacks==0);tick(s,599);check(s.globalBacks==0);tick(s,600);check(s.globalBacks==1);check(s.preferences.getBoolean("bixbyDismissed",false));check(!s.policy.attempted);tick(s,700);tick(s,1500);check(s.globalBacks==1);
  // An explicit request may close only the exact optional Bixby package, never another app.
  s=service();s.preferences.edit().putBoolean("dismissBixby",false).apply();waitReady(s);check(s.globalBacks==0);
  for(String pkg:new String[]{"other.app",BixbyHandoff.PACKAGE+".fake","com.android.systemui"}){s=service();s.root.pkg=pkg;waitReady(s);check(s.globalBacks==0);}
  s=service();s.root.window=-1;waitReady(s);check(s.globalBacks==0);
  s=service();s.root.visible=false;waitReady(s);check(s.globalBacks==0);
  s=service();s.root.enabled=false;waitReady(s);check(s.globalBacks==0);
  s=service();s.keyguard.locked=true;waitReady(s);check(s.globalBacks==0);check(!s.running);
  s=service();s.preferences.edit().putBoolean("consent",false).apply();waitReady(s);check(s.globalBacks==0);check(!s.running);
  s=service();s.globalResult=false;waitReady(s);check(s.globalBacks==1);tick(s,700);check(s.globalBacks==1);check(s.running);
  // Window changes and a missing root reset the 500ms initial-window stability.
  s=service();tick(s,100);s.root.window=2;tick(s,600);check(s.globalBacks==0);tick(s,1100);check(s.globalBacks==1);
  s=service();tick(s,100);AccessibilityNodeInfo saved=s.root;s.root=null;tick(s,200);s.root=saved;tick(s,600);check(s.globalBacks==0);tick(s,1100);check(s.globalBacks==1);
  // Once ChatGPT has been observed, assistant arrival is app departure, not another dismissal.
  s=service();s.root=FoldControlResolverTest.view("",false);tick(s,100);check(s.enteredTarget);s.root=bixby();tick(s,600);check(s.globalBacks==0);check(!s.running);
  // A same-process reconnect cannot dispatch a second Back after the saved reservation.
  s=service();waitReady(s);ZipService resumed=new ZipService();resumed.preferences=s.preferences;resumed.root=bixby();resumed.begin();tick(resumed,700);tick(resumed,1200);check(resumed.globalBacks==0);check(resumed.running);
  resumed.preferences.edit().putString("requestProcess","old-process").apply();ZipService restarted=new ZipService();restarted.preferences=resumed.preferences;restarted.begin();check(!restarted.running);check(restarted.globalBacks==0);
  // Failed reservation persistence forbids the global action.
  s=service();tick(s,100);s.preferences.failCommits=true;tick(s,600);check(s.globalBacks==0);check(!s.running);
  // A changed root after persistence, even to another legitimate window, forbids Back.
  ZipService base=service();ZipService changing=new ZipService(){int roots;@Override public AccessibilityNodeInfo getRootInActiveWindow(){roots++;return roots>=4?new AccessibilityNodeInfo("other"){{pkg="other.app";}}:root;}};
  changing.preferences=base.preferences;changing.root=bixby();changing.begin();waitReady(changing);check(changing.globalBacks==0);check(!changing.running);
  // Refresh mutations immediately before action cannot bypass package, lock, deadline, or consent.
  for(int mode=0;mode<6;mode++){
   final int which=mode;final ZipService current=service();
   current.root=new FoldControlResolverTest.ViewNode("android.view.View","",false){int refreshes;{pkg=BixbyHandoff.PACKAGE;}@Override public boolean refresh(){if(++refreshes==4){if(which==0)pkg="other.app";if(which==1)current.keyguard.locked=true;if(which==2)SystemClock.now=30100;if(which==3)current.preferences.edit().putBoolean("consent",false).apply();if(which==4)current.preferences.edit().putBoolean("dismissBixby",false).apply();if(which==5)CallSettings.clearRequest(current.preferences.edit()).commit();}return true;}};
   waitReady(current);check(current.globalBacks==0);check(!current.running);
  }
  // A successful handoff is not authority to click: the full Dot binding/stability is still required.
  s=service();waitReady(s);s.root=FoldControlResolverTest.view("",false).add(FoldControlResolverTest.inputContainer("unrelated input")).add(FoldControlResolverTest.control("통화",4));AccessibilityNodeInfo call=s.root.getChild(1);tick(s,700);tick(s,1200);check(call.clicks==0);check(s.globalBacks==1);
  s=service();waitReady(s);call=FoldControlResolverTest.control("통화",4);s.root=FoldControlResolverTest.view("",false).add(FoldControlResolverTest.inputContainer("내 dot에게 메시지 보내기")).add(call);tick(s,700);check(call.clicks==0);tick(s,1200);check(call.clicks==1);s.root=bixby();tick(s,1300);check(s.globalBacks==1);check(!s.running);
  s=service();check(CallSettings.applyObservedFoldProfile(s));check(s.preferences.getBoolean("dismissBixby",false));check(!s.preferences.getBoolean("consent",true));check(!s.armed());check(!s.preferences.getBoolean("bixbyDismissed",false));
  // Explicit Bixby-only diagnostic can never arm a call, navigate ChatGPT, or reuse a Back.
  s=service();s.inspectBixbyOnce();check(!s.running);check(!s.armed());SystemClock.now=100;s.inspectStep();SystemClock.now=600;s.inspectStep();check(s.globalBacks==1);check(s.inspectionBixbyAttempted);SystemClock.now=700;s.inspectStep();check(s.globalBacks==1);
  call=FoldControlResolverTest.control("통화",4);s.root=FoldControlResolverTest.view("",false).add(FoldControlResolverTest.inputContainer("내 dot에게 메시지 보내기")).add(call);SystemClock.now=800;s.inspectStep();check(call.clicks==0);check(s.inspectionDeadline==0);check(ZipService.inspectionReport.contains("no call dispatch"));
  s=service();s.globalResult=false;s.inspectBixbyOnce();SystemClock.now=100;s.inspectStep();SystemClock.now=600;s.inspectStep();s.inspectStep();check(s.globalBacks==1);check(s.inspectionDeadline==0);
  s=service();s.root.pkg="other.app";s.inspectBixbyOnce();SystemClock.now=100;s.inspectStep();SystemClock.now=600;s.inspectStep();check(s.globalBacks==0);SystemClock.now=10100;s.inspectStep();check(s.inspectionDeadline==0);check(ZipService.inspectionReport.contains("Back attempts=0"));
  s=service();s.inspectBixbyOnce();s.keyguard.locked=true;s.inspectStep();check(s.globalBacks==0);check(s.inspectionDeadline==0);
  System.out.println("PASS "+checks+" optional Bixby handoff checks (synthetic Android; no live calls)");
 }
}
