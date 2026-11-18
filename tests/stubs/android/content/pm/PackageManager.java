package android.content.pm;
public class PackageManager {
 public android.content.Intent launch=new android.content.Intent("chatgpt");
 public android.content.Intent getLaunchIntentForPackage(String p){if(!p.equals("com.openai.chatgpt"))throw new AssertionError();return launch;}
}
