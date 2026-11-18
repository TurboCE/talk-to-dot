package local.zipshortcut;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) { super.onCreate(state); openDot(); }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); openDot(); }
    private void openDot() {
        String enabled=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        ComponentName service=new ComponentName(this,ZipService.class);
        boolean access=false;
        if(enabled!=null)for(String s:enabled.split(":"))if(service.equals(ComponentName.unflattenFromString(s)))access=true;
        if(!access || ZipService.instance==null) {
            recordFailure(ResultStatus.SERVICE_UNAVAILABLE);
            startActivity(new Intent(this,SettingsActivity.class)); finish(); return;
        }
        if(!CallSettings.configured(this)) {
            startActivity(new Intent(this,SettingsActivity.class)); finish(); return;
        }
        KeyguardManager lock=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        if(lock!=null && lock.isKeyguardLocked()) { recordFailure(ResultStatus.LOCKED); Toast.makeText(this,R.string.locked,Toast.LENGTH_LONG).show(); finish(); return; }
        Intent launch=getPackageManager().getLaunchIntentForPackage("com.openai.chatgpt");
        if(launch==null) { recordFailure(ResultStatus.LAUNCH_FAILED); Toast.makeText(this,R.string.missing_app,Toast.LENGTH_LONG).show(); finish(); return; }
        android.content.SharedPreferences prefs=getSharedPreferences("shortcut",0);
        long now=SystemClock.elapsedRealtime(), previous=prefs.getLong("lastLaunch",-1);
        if(prefs.getInt("boot",-2)==Settings.Global.getInt(getContentResolver(),Settings.Global.BOOT_COUNT,-1) && previous>=0 && now>=previous && now-previous<CallPolicy.TOTAL_MS) { finish(); return; }
        if(!prefs.edit().putLong("startedAtElapsed",now).putLong("lastLaunch",now).putInt("boot",Settings.Global.getInt(getContentResolver(),Settings.Global.BOOT_COUNT,-1)).putString("requestProcess",CallSettings.PROCESS_ID).putString("lastResult",ResultStatus.STARTING).putBoolean("bixbyDismissed",false).putBoolean("observedTarget",false).putBoolean("callAttempted",false).remove("attemptedAt").commit()) { finish(); return; }
        launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try { startActivity(launch); if(ZipService.instance!=null)ZipService.instance.begin(); }
        catch(RuntimeException error) { recordFailure(ResultStatus.LAUNCH_FAILED); Toast.makeText(this,R.string.missing_app,Toast.LENGTH_LONG).show(); }
        finish();
    }
    private void recordFailure(String status) {
        CallSettings.clearRequest(getSharedPreferences("shortcut",0).edit()).remove("lastStatus").putString("lastResult",status).commit();
    }
}
