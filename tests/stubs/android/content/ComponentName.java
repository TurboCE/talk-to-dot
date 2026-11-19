package android.content;
public class ComponentName {
 final String value;
 public ComponentName(Context c,Class<?> t){value=c.getPackageName()+"/"+t.getName();}
 private ComponentName(String v){value=v;}
 public static ComponentName unflattenFromString(String s){int i=s.indexOf('/');if(i<0)return null;String p=s.substring(0,i),n=s.substring(i+1);return new ComponentName(p+"/"+(n.startsWith(".")?p+n:n));}
 public boolean equals(Object o){return o instanceof ComponentName && value.equals(((ComponentName)o).value);}
 public int hashCode(){return value.hashCode();}
}
