package android.content;
public class Intent {
 public static final int FLAG_ACTIVITY_CLEAR_TOP=0x04000000, FLAG_ACTIVITY_SINGLE_TOP=0x20000000;
 public Class<?> target; public String action; public int flags;
 public Intent(String a){action=a;} public Intent(Context c,Class<?> t){target=t;}
 public Intent addFlags(int f){flags|=f;return this;}
}
