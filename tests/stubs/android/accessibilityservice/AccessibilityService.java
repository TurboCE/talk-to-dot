package android.accessibilityservice;
public class AccessibilityService extends android.content.Context {
 public static final int GLOBAL_ACTION_BACK=1;public int globalBacks;public boolean globalResult=true;
 public boolean performGlobalAction(int action){if(action!=GLOBAL_ACTION_BACK)throw new AssertionError();globalBacks++;return globalResult;}
 public android.view.accessibility.AccessibilityNodeInfo root;
 protected void onServiceConnected(){} public void onDestroy(){} public void onInterrupt(){}
 public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent e){}
 public android.view.accessibility.AccessibilityNodeInfo getRootInActiveWindow(){return root;}
}
