package local.zipshortcut;

import android.view.accessibility.AccessibilityNodeInfo;

/** Label ownership observed on Fold7 / ChatGPT 1.2026.314. Never clicks coordinates or arbitrary ancestors. */
final class ControlResolver {
    private static final int MAX_LABEL_DEPTH=4;
    static boolean equal(CharSequence actual,String wanted) { return actual!=null && wanted!=null && wanted.contentEquals(actual); }
    static boolean explicit(AccessibilityNodeInfo n,String label) { return equal(n.getText(),label) || equal(n.getContentDescription(),label); }
    static boolean view(AccessibilityNodeInfo n) { return equal(n.getClassName(),"android.view.View"); }
    static boolean button(AccessibilityNodeInfo n) {
        return equal(n.getClassName(),"android.widget.Button") || equal(n.getClassName(),"android.widget.ImageButton");
    }
    static boolean usable(AccessibilityNodeInfo n) { return n!=null && n.isVisibleToUser() && n.isEnabled(); }
    static boolean sameWindow(AccessibilityNodeInfo a,AccessibilityNodeInfo b) {
        return a.getWindowId()==b.getWindowId() && equal(a.getPackageName(),String.valueOf(b.getPackageName()));
    }
    static boolean nonempty(CharSequence value) { return value!=null && value.length()>0; }
    /** A single noninteractive View chain containing exactly one explicit label, and no other text. */
    static boolean labelBranch(AccessibilityNodeInfo branch,String label,AccessibilityNodeInfo owner) {
        int labels=0;
        for(int depth=1;depth<=MAX_LABEL_DEPTH;depth++) {
            if(!usable(branch) || !view(branch) || branch.isClickable() || branch.isEditable() || !sameWindow(branch,owner))return false;
            if(nonempty(branch.getText()) && !equal(branch.getText(),label))return false;
            if(nonempty(branch.getContentDescription()) && !equal(branch.getContentDescription(),label))return false;
            if(explicit(branch,label))labels++;
            if(labels>1)return false;
            int count=branch.getChildCount();
            if(count==0)return labels==1;
            if(count!=1)return false;
            branch=branch.getChild(0);
        }
        return false;
    }
    static boolean control(AccessibilityNodeInfo owner,String label,boolean requireButton) {
        if(label==null || label.isEmpty() || !usable(owner) || !owner.isClickable() || owner.isEditable())return false;
        if(explicit(owner,label))return !requireButton || button(owner);
        // Verified Compose representation: [noninteractive label branch, nonclickable Button role].
        // Broad transcript containers, multiple buttons, nested actions and arbitrary parent climbing fail closed.
        if(!view(owner) || owner.getChildCount()!=2)return false;
        AccessibilityNodeInfo first=owner.getChild(0),second=owner.getChild(1);
        if(first==null || second==null)return false;
        AccessibilityNodeInfo role=button(first)?first:button(second)?second:null;
        AccessibilityNodeInfo branch=role==first?second:first;
        if(role==null || !usable(role) || role.isClickable() || role.isEditable() || role.getChildCount()!=0
                || !sameWindow(role,owner) || nonempty(role.getText()) || nonempty(role.getContentDescription()))return false;
        return labelBranch(branch,label,owner);
    }
    static boolean input(AccessibilityNodeInfo input,String label) {
        if(!usable(input) || !input.isEditable())return false;
        if(explicit(input,label) || equal(input.getHintText(),label))return true;
        // Verified input representation: one labelled View sibling and one editable input under their immediate parent.
        AccessibilityNodeInfo parent=input.getParent();
        if(!usable(parent) || !view(parent) || parent.isClickable() || parent.getChildCount()!=2 || !sameWindow(input,parent))return false;
        AccessibilityNodeInfo first=parent.getChild(0),second=parent.getChild(1);
        AccessibilityNodeInfo branch=input.equals(first)?second:input.equals(second)?first:null;
        return branch!=null && labelBranch(branch,label,input);
    }
}
