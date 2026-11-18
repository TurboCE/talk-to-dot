package local.zipshortcut;
import android.content.Context;
import android.content.SharedPreferences;
final class CallSettings {
    static final String PROCESS_ID=java.util.UUID.randomUUID().toString();
    static final String[] KEYS={"composer","menu","sidebar","up","call","connected"};
    static final String[] DEFAULTS={"Message {dot}","Menu","Scheduled","Navigate up","",""};
    static String get(Context c,int i) { return c.getSharedPreferences("shortcut",0).getString(KEYS[i],DEFAULTS[i]).trim(); }
    static String[] resolved(String[] values,String name) {
        String[] result=values.clone(); result[0]=result[0].replace("{dot}",name.trim()); return result;
    }
    static boolean distinct(String[] v) {
        for(int i=0;i<v.length;i++) for(int j=i+1;j<v.length;j++)
            if(!v[i].isEmpty() && v[i].equals(v[j])) return false;
        return true;
    }
    static boolean valid(String[] v,String name) {
        if(v==null || v.length!=KEYS.length || name==null || name.trim().isEmpty())return false;
        for(String value:v)if(value==null)return false;
        if(!v[0].contains("{dot}") || v[0].replace("{dot}","").trim().isEmpty()
                || v[1].isEmpty() || v[2].isEmpty() || v[4].isEmpty() || v[5].isEmpty())return false;
        String target=name.trim();
        if(target.equals(v[1]) || target.equals(v[4]) || target.equals(v[5]) || (!v[3].isEmpty() && target.equals(v[3])))return false;
        return distinct(v) && distinct(resolved(v,name));
    }
    static boolean complete(String[] values,String name,String entry) {
        if(!valid(values,name))return false;
        String target=entry==null || entry.trim().isEmpty()?name.trim():entry.trim();
        return !target.equals(values[1]) && !target.equals(values[4]) && !target.equals(values[5])
                && (values[3].isEmpty() || !target.equals(values[3]));
    }
    static final String[] OBSERVED_FOLD_LABELS={"내 {dot}에게 메시지 보내기","메뉴","Dot","","통화","종료"};
    static boolean applyObservedFoldProfile(Context c) {return store(c,"dot",OBSERVED_FOLD_LABELS,false,"Dot");}
    static boolean configured(Context c) {
        String[] v=new String[KEYS.length]; for(int i=0;i<v.length;i++)v[i]=get(c,i);
        return complete(v,DotSettings.name(c),DotSettings.entry(c)) && c.getSharedPreferences("shortcut",0).getBoolean("consent",false);
    }
    static SharedPreferences.Editor clearRequest(SharedPreferences.Editor e) {
        return e.remove("startedAtElapsed").remove("armedUntil").remove("callAttempted")
                .remove("attemptedAt").remove("observedTarget").remove("requestProcess");
    }
    static boolean cancelRequest(Context c) {
        return clearRequest(c.getSharedPreferences("shortcut",0).edit()).remove("lastStatus")
                .putString("lastResult",ResultStatus.CANCELLED).commit();
    }
    /** Revocation must succeed even when other fields are incomplete or invalid. */
    static boolean store(Context c,String name,String[] values,boolean consent) {
        return store(c,name,values,consent,"");
    }
    static boolean store(Context c,String name,String[] values,boolean consent,String entry) {
        boolean complete=complete(values,name,entry);
        if(consent && !complete)return false;
        SharedPreferences.Editor edit=clearRequest(c.getSharedPreferences("shortcut",0).edit()).putBoolean("consent",consent);
        if(complete) {
            edit.putString("dotName",name.trim()).putString("dotEntry",entry==null?"":entry.trim());
            for(int i=0;i<values.length;i++)edit.putString(KEYS[i],values[i]);
        }
        return edit.commit();
    }
}
