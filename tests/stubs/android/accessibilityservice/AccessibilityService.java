package android.accessibilityservice;
public class AccessibilityService extends android.content.Context {
 public android.view.accessibility.AccessibilityNodeInfo root;
 protected void onServiceConnected(){} public void onDestroy(){} public void onInterrupt(){}
 public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent e){}
 public android.view.accessibility.AccessibilityNodeInfo getRootInActiveWindow(){return root;}
}
