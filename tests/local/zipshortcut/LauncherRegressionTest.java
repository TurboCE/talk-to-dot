package local.zipshortcut;
import android.os.SystemClock;
import android.provider.Settings;
import android.content.Intent;
public class LauncherRegressionTest {
 static int checks;
 static void check(boolean value){checks++;if(!value)throw new AssertionError("launcher check "+checks);}
 static MainActivity ready(){
  SystemClock.now=100000;android.widget.Toast.lastMessage=0;Settings.Secure.enabled="local.zipshortcut/.ZipService";
  MainActivity a=new MainActivity();check(CallSettings.store(a,"dot",CallSettings.OBSERVED_FOLD_LABELS,true,"Dot"));
  ZipService s=new ZipService();s.preferences=a.preferences;ZipService.instance=s;return a;
 }
 static boolean armed(MainActivity a){return a.preferences.getLong("startedAtElapsed",-1)>=0;}
 static void ownSetup(MainActivity a){check(a.finished);check(a.started.size()==1);check(a.started.get(0).target==SettingsActivity.class);check(!armed(a));}
 public static void main(String[] args){
  MainActivity a=ready();a.preferences.edit().putString("lastResult",ResultStatus.CONFIRMED).putLong("startedAtElapsed",99900).apply();ZipService.instance=null;
  a.onCreate(null);ownSetup(a);check(a.preferences.getString("lastResult","").equals(ResultStatus.SERVICE_UNAVAILABLE));
  ZipService late=new ZipService();late.preferences=a.preferences;late.onServiceConnected();check(!late.running);check(!armed(a)); // Reconnecting cannot revive the refused launch.
  a=ready();Settings.Secure.enabled="other.app/.Service";a.onCreate(null);ownSetup(a);
  a=ready();Settings.Secure.enabled="xlocal.zipshortcut/.ZipService";a.onCreate(null);ownSetup(a); // Exact component, not substring.
  a=ready();Settings.Secure.enabled=null;a.onCreate(null);ownSetup(a);
  a=ready();a.preferences.edit().putBoolean("consent",false).apply();a.onCreate(null);ownSetup(a);check(!ZipService.instance.running);
  a=ready();a.keyguard.locked=true;a.onCreate(null);check(a.started.isEmpty());check(!armed(a));check(a.finished);check(a.preferences.getString("lastResult","").equals(ResultStatus.LOCKED));
  a=ready();a.packageManager.launch=null;a.onCreate(null);check(a.started.isEmpty());check(!armed(a));check(a.preferences.getString("lastResult","").equals(ResultStatus.LAUNCH_FAILED));
  a=ready();a.preferences.edit().putString("lastResult",ResultStatus.CONFIRMED).apply();a.onCreate(null);check(a.started.size()==1);check(a.started.get(0).target==null);check(armed(a));check(ZipService.instance.running);check(a.preferences.getString("lastResult","").equals(ResultStatus.STARTING));check(a.preferences.getString("requestProcess","").equals(CallSettings.PROCESS_ID));
  check((a.started.get(0).flags & Intent.FLAG_ACTIVITY_CLEAR_TOP)!=0);check((a.started.get(0).flags & Intent.FLAG_ACTIVITY_SINGLE_TOP)!=0);
  long first=a.preferences.getLong("startedAtElapsed",-1);a.onNewIntent(new Intent("again"));check(a.started.size()==1);check(a.preferences.getLong("startedAtElapsed",-1)==first);check(android.widget.Toast.lastMessage==R.string.cooldown);check(a.preferences.getString("lastResult","").equals(ResultStatus.STARTING)); // No immediate rearm.
  a=ready();a.failChatLaunch=true;a.onCreate(null);check(a.started.isEmpty());check(!armed(a));check(!ZipService.instance.running);check(a.preferences.getString("lastResult","").equals(ResultStatus.LAUNCH_FAILED));check(a.preferences.getString("requestProcess",null)==null);
  a=ready();a.preferences.failCommits=true;a.onCreate(null);check(a.started.isEmpty());check(!armed(a));check(!ZipService.instance.running);
  a=ready();a.preferences.edit().putString("lastResult",ResultStatus.CONFIRMED).putLong("startedAtElapsed",99900).putBoolean("callAttempted",true).apply();ZipService.instance=null;check(CallSettings.cancelRequest(a));check(!armed(a));check(!a.preferences.getBoolean("callAttempted",false));check(a.preferences.getString("lastResult","").equals(ResultStatus.CANCELLED));
  check(ResultStatus.message(ResultStatus.SERVICE_UNAVAILABLE)==R.string.service_unavailable);check(ResultStatus.message(ResultStatus.STARTING)==R.string.starting);check(ResultStatus.message(ResultStatus.LAUNCH_FAILED)==R.string.missing_app);
  System.out.println("PASS "+checks+" actual-launcher regression checks (synthetic Android; no device calls)");
 }
}
