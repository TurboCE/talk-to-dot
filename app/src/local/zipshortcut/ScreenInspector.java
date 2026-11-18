package local.zipshortcut;
import android.view.accessibility.AccessibilityNodeInfo;

/** Opt-in, read-only diagnostic summary. No clicks, editable values, transcript text, files or logs. */
final class ScreenInspector {
    static String branchLabel(AccessibilityNodeInfo node) {
        String result=null;
        for(int depth=1;depth<=4 && node!=null;depth++) {
            if(!ControlResolver.usable(node) || !ControlResolver.view(node) || node.isClickable() || node.isEditable())return null;
            CharSequence text=node.getText(),description=node.getContentDescription();
            String label=ControlResolver.nonempty(description)?description.toString():ControlResolver.nonempty(text)?text.toString():null;
            if(label!=null) { if(result!=null || label.length()>80)return null;result=label; }
            if(node.getChildCount()==0)return result;
            if(node.getChildCount()!=1)return null;
            node=node.getChild(0);
        }
        return null;
    }
    static String controlLabel(AccessibilityNodeInfo owner) {
        String label=null;
        if(ControlResolver.button(owner)) {
            CharSequence desc=owner.getContentDescription(),text=owner.getText();
            label=ControlResolver.nonempty(desc)?desc.toString():ControlResolver.nonempty(text)?text.toString():null;
        } else if(owner.getChildCount()==2) {
            AccessibilityNodeInfo a=owner.getChild(0),b=owner.getChild(1);
            if(a!=null && b!=null)label=branchLabel(ControlResolver.button(a)?b:a);
        }
        return label!=null && label.length()<=80 && ControlResolver.control(owner,label,true)?label:null;
    }
    static String inputLabel(AccessibilityNodeInfo input) {
        if(!ControlResolver.usable(input) || !input.isEditable())return null;
        CharSequence hint=input.getHintText();
        if(ControlResolver.nonempty(hint) && hint.length()<=80)return hint.toString();
        AccessibilityNodeInfo parent=input.getParent();
        if(parent==null || parent.getChildCount()!=2)return null;
        AccessibilityNodeInfo a=parent.getChild(0),b=parent.getChild(1);
        String label=branchLabel(input.equals(a)?b:input.equals(b)?a:null);
        return label!=null && ControlResolver.input(input,label)?label:null;
    }
    static String scan(AccessibilityNodeInfo root) {
        StringBuilder result=new StringBuilder();int[] counts={0,0};walk(root,result,counts);
        return "controls="+counts[0]+", inputLabels="+counts[1]+"\n"+result;
    }
    static void walk(AccessibilityNodeInfo node,StringBuilder result,int[] counts) {
        if(node==null || counts[0]+counts[1]>=32)return;
        String control=controlLabel(node),input=inputLabel(node);
        if(control!=null) {counts[0]++;result.append("Button: ").append(control).append(" | owner=").append(node.getClassName()).append('\n');}
        if(input!=null) {counts[1]++;result.append("Input label: ").append(input).append('\n');}
        for(int i=0;i<node.getChildCount();i++)walk(node.getChild(i),result,counts);
    }
}
