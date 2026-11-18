package local.zipshortcut;
import android.content.Context;
final class CallSettings {
    static final String[] KEYS={"composer","menu","sidebar","up","call","connected"};
    static final String[] DEFAULTS={"Message {dot}","Menu","Scheduled","Navigate up","",""};
    static String get(Context c,int i) { return c.getSharedPreferences("shortcut",0).getString(KEYS[i],DEFAULTS[i]).trim(); }
    static boolean valid(String[] v) {
        if ((!v[0].contains("{dot}") || v[0].replace("{dot}","").trim().isEmpty()) || v[1].isEmpty() || v[2].isEmpty() || v[4].isEmpty() || v[5].isEmpty()) return false;
        for(int i=0;i<v.length;i++) for(int j=i+1;j<v.length;j++)
            if(!v[i].isEmpty() && v[i].equals(v[j])) return false;
        return true;
    }
    static boolean configured(Context c) {
        String[] v=new String[KEYS.length]; for(int i=0;i<v.length;i++)v[i]=get(c,i);
        return valid(v) && c.getSharedPreferences("shortcut",0).getBoolean("consent",false);
    }
}
