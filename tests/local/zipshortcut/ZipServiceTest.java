package local.zipshortcut;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
public class ZipServiceTest {
 static int checks;
 static void check(boolean b){checks++;if(!b)throw new AssertionError("service check "+checks);}
 static AccessibilityNodeInfo node(String label,boolean clickable){AccessibilityNodeInfo n=new AccessibilityNodeInfo(label);n.clickable=clickable;return n;}
 static ZipService service() {
  SystemClock.now=100;
  ZipService s=new ZipService();s.preferences.edit().putLong("startedAtElapsed",100).putInt("boot",1).putBoolean("consent",true)
    .putString("call","REAL_CALL").putString("connected","REAL_END").apply();
  s.root=node("root",false);s.begin();return s;
 }
 static AccessibilityNodeInfo composer(){AccessibilityNodeInfo c=node("Message zip",false);c.editable=true;return c;}
 static void tick(ZipService s,long now){SystemClock.now=now;s.step();}
 public static void main(String[] args) {
  ZipService s=service(); AccessibilityNodeInfo call=node("REAL_CALL",true);s.root.add(composer()).add(call);
  tick(s,100);check(call.clicks==0);tick(s,600);check(call.clicks==1);tick(s,700);check(call.clicks==1);
  check(s.preferences.getBoolean("callAttempted",false));
  ZipService restarted=new ZipService();restarted.preferences=s.preferences;restarted.root=s.root;restarted.begin();tick(restarted,800);check(call.clicks==1);
  restarted.root=node("root",false).add(node("REAL_END",true));tick(restarted,900);tick(restarted,1400);check(!restarted.running);
  check(restarted.preferences.getInt("lastStatus",0)==R.string.confirmed);
  s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call).add(node("REAL_CALL",true));tick(s,100);tick(s,600);check(call.clicks==0);
  s=service();call=node("REAL_CALL",true);call.enabled=false;s.root.add(composer()).add(call);tick(s,100);tick(s,600);check(call.clicks==0);
  s=service();call=node("REAL_CALL",true);s.root.add(node("Message zip",false)).add(call);tick(s,100);tick(s,600);check(call.clicks==0);
  s=service();call=node("REAL_CALL",false);s.root.add(composer()).add(node("container",true).add(call));tick(s,100);tick(s,600);check(call.clicks==0);check(s.root.children.get(1).clicks==0);
  s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call);s.root.pkg="other.app";tick(s,100);tick(s,600);check(call.clicks==0);
  s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call);s.keyguard.locked=true;tick(s,100);check(call.clicks==0);check(!s.running);
  s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call).add(node("REAL_END",false));tick(s,100);check(call.clicks==0);check(!s.running);
  s=service();call=node("REAL_CALL",true);call.result=false;s.root.add(composer()).add(call);tick(s,100);tick(s,600);tick(s,700);check(call.clicks==1);check(!s.running);
  s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call);tick(s,100);tick(s,600);tick(s,8600);check(call.clicks==1);check(!s.running);check(s.preferences.getInt("lastStatus",0)==R.string.unconfirmed);
  s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call);tick(s,100);tick(s,30100);check(call.clicks==0);check(!s.running);
  s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call);tick(s,100);tick(s,600);s.root.add(node("REAL_END",false));tick(s,700);tick(s,1200);check(s.running); // composer remains: no false confirmation
  s=service();AccessibilityNodeInfo menu=node("Menu",true);s.root.add(menu);tick(s,100);check(menu.clicks==1);
  s.root=node("root",false).add(node("Scheduled",false)).add(node("zip",true));tick(s,200);check(s.root.children.get(1).clicks==1);
  s.root=node("root",false).add(composer()).add(node("REAL_CALL",true));tick(s,300);tick(s,800);check(s.root.children.get(1).clicks==1);
  s=service();menu=node("Menu",true);s.root.add(menu);tick(s,100);s.root.pkg="other.app";tick(s,200);check(!s.running);
  s=service();call=node("REAL_CALL",true);s.root.add(composer()).add(call);tick(s,100);s.root.pkg="other.app";tick(s,200);check(!s.running);s.root.pkg="com.openai.chatgpt";tick(s,800);check(call.clicks==0);
  System.out.println("PASS "+checks+" service checks with synthetic Android nodes (not device verification)");
 }
}
