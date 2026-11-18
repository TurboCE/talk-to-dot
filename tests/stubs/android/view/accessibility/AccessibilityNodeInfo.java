package android.view.accessibility;
public class AccessibilityNodeInfo {
 public static final int ACTION_CLICK=16;
 public String label, pkg="com.openai.chatgpt"; public boolean visible=true,enabled=true,clickable,editable,result=true;
 public int clicks,window=1; public AccessibilityNodeInfo parent;
 public java.util.List<AccessibilityNodeInfo> children=new java.util.ArrayList<>();
 public AccessibilityNodeInfo(String label){this.label=label;}
 public AccessibilityNodeInfo add(AccessibilityNodeInfo n){children.add(n);n.parent=this;return this;}
 public CharSequence getText(){return label;} public CharSequence getContentDescription(){return null;}public CharSequence getHintText(){return null;}
 public CharSequence getPackageName(){return pkg;} public boolean refresh(){return true;}
 public boolean isVisibleToUser(){return visible;}public boolean isEnabled(){return enabled;}public boolean isClickable(){return clickable;}public boolean isEditable(){return editable;}
 public int getWindowId(){return window;}public int getChildCount(){return children.size();}
 public AccessibilityNodeInfo getChild(int i){return children.get(i);}public AccessibilityNodeInfo getParent(){return parent;}
 public void getBoundsInScreen(android.graphics.Rect r){r.x=1;}
 public boolean performAction(int action){clicks++;return result;}
}
