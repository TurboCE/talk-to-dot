package android.content;
public class SharedPreferences {
 public boolean failCommits;
 public final java.util.Map<String,Object> data=new java.util.HashMap<>();
 public long getLong(String k,long d){return (Long)data.getOrDefault(k,d);}
 public int getInt(String k,int d){return (Integer)data.getOrDefault(k,d);}
 public boolean getBoolean(String k,boolean d){return (Boolean)data.getOrDefault(k,d);}
 public String getString(String k,String d){return (String)data.getOrDefault(k,d);}
 public Editor edit(){return new Editor();}
 public class Editor {
  java.util.Map<String,Object> updates=new java.util.HashMap<>();
  public Editor putLong(String k,long v){updates.put(k,v);return this;}
  public Editor putInt(String k,int v){updates.put(k,v);return this;}
  public Editor putBoolean(String k,boolean v){updates.put(k,v);return this;}
  public Editor putString(String k,String v){updates.put(k,v);return this;}
  public Editor remove(String k){updates.put(k,null);return this;}
  public boolean commit(){if(failCommits)return false;for(String k:updates.keySet()){if(updates.get(k)==null)data.remove(k);else data.put(k,updates.get(k));}return true;}
  public void apply(){commit();}
 }
}
