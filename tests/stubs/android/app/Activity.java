package android.app;
public class Activity extends android.content.Context {
 public java.util.List<android.content.Intent> started=new java.util.ArrayList<>();public boolean finished,failChatLaunch;
 public void onCreate(android.os.Bundle b){} protected void onNewIntent(android.content.Intent i){} public void setIntent(android.content.Intent i){}
 public void startActivity(android.content.Intent i){if(failChatLaunch && i.target==null)throw new SecurityException("test");started.add(i);}
 public void finish(){finished=true;}
}
