package android.content;
public class Context {
 public static final String KEYGUARD_SERVICE="keyguard";
 public android.content.pm.PackageManager packageManager=new android.content.pm.PackageManager();
 public android.content.pm.PackageManager getPackageManager(){return packageManager;}
 public String getPackageName(){return "local.zipshortcut";}
 public SharedPreferences preferences=new SharedPreferences();
 public android.app.KeyguardManager keyguard=new android.app.KeyguardManager();
 public SharedPreferences getSharedPreferences(String n,int m){return preferences;}
 public Object getSystemService(String n){return keyguard;}
 public Object getContentResolver(){return null;}
}
