package local.zipshortcut;
import android.view.accessibility.AccessibilityNodeInfo;

/** Native Korean drawer shape observed on Fold7. NONE / verified / ambiguous remain distinct. */
final class NativeNavigation {
    static final int NONE=0, VERIFIED=1, AMBIGUOUS=2;
    static boolean empty(AccessibilityNodeInfo n) {
        return !ControlResolver.nonempty(n.getText()) && !ControlResolver.nonempty(n.getContentDescription()) && !ControlResolver.nonempty(n.getHintText());
    }
    static boolean inert(AccessibilityNodeInfo n,AccessibilityNodeInfo owner) {
        return ControlResolver.usable(n) && !n.isClickable() && !n.isEditable() && ControlResolver.sameWindow(n,owner);
    }
    static boolean decoration(AccessibilityNodeInfo n,int length,AccessibilityNodeInfo owner) {
        for(int i=0;i<length;i++) {
            if(n==null || !inert(n,owner) || !ControlResolver.view(n) || !empty(n))return false;
            if(i==length-1)return n.getChildCount()==0;
            if(n.getChildCount()!=1)return false;n=n.getChild(0);
        }
        return false;
    }
    static boolean trailingDecoration(AccessibilityNodeInfo node,AccessibilityNodeInfo owner) {
        if(node==null || !inert(node,owner) || !ControlResolver.view(node) || !empty(node))return false;
        if(node.getChildCount()==0)return true;
        if(node.getChildCount()!=1)return false;
        AccessibilityNodeInfo leaf=node.getChild(0);
        // The observed empty indicator may disappear or be hidden. It is never an action or label.
        return leaf!=null && ControlResolver.view(leaf) && !leaf.isClickable() && !leaf.isEditable()
                && ControlResolver.sameWindow(leaf,owner) && empty(leaf) && leaf.getChildCount()==0;
    }
    static boolean possibleRow(AccessibilityNodeInfo owner,String label) {
        if(owner==null || !owner.isVisibleToUser() || !owner.isClickable() || owner.isEditable()
                || !ControlResolver.view(owner) || owner.getChildCount()!=4)return false;
        AccessibilityNodeInfo textBranch=owner.getChild(1),role=owner.getChild(3);
        if(textBranch==null || role==null || !ControlResolver.view(textBranch) || textBranch.getChildCount()!=1 || !ControlResolver.button(role))return false;
        AccessibilityNodeInfo text=textBranch.getChild(0);
        return text!=null && ControlResolver.equal(text.getClassName(),"android.widget.TextView") && ControlResolver.explicit(text,label);
    }
    static boolean exactRow(AccessibilityNodeInfo owner,String label) {
        if(!possibleRow(owner,label) || !owner.isEnabled() || !empty(owner))return false;
        AccessibilityNodeInfo textBranch=owner.getChild(1),text=textBranch.getChild(0),role=owner.getChild(3);
        return decoration(owner.getChild(0),3,owner) && trailingDecoration(owner.getChild(2),owner)
                && inert(textBranch,owner) && empty(textBranch) && inert(text,owner) && text.getChildCount()==0
                && ControlResolver.equal(text.getText(),label) && !ControlResolver.nonempty(text.getContentDescription())
                && inert(role,owner) && role.getChildCount()==0 && empty(role);
    }
    static int anchorCount(AccessibilityNodeInfo n,String label) {
        if(n==null)return 0;
        int count=ControlResolver.control(n,label,true)?1:0;
        for(int i=0;i<n.getChildCount() && count<2;i++)count+=anchorCount(n.getChild(i),label);
        return Math.min(count,2);
    }
    static int drawerEvidence(AccessibilityNodeInfo root) {
        int create=anchorCount(root,"새 채팅"),search=anchorCount(root,"검색");
        if(create==0 && search==0)return NONE;
        return create==1 && search==1?VERIFIED:AMBIGUOUS;
    }
    static AccessibilityNodeInfo rootOf(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo root=node;
        for(int i=0;i<96;i++) {
            AccessibilityNodeInfo parent=root.getParent();if(parent==null)return root;
            if(!ControlResolver.sameWindow(parent,node))return null;root=parent;
        }
        return null;
    }
    static String explain(AccessibilityNodeInfo owner,String label) {
        StringBuilder out=new StringBuilder("nativePossible=").append(possibleRow(owner,label)).append("; nativeExact=").append(exactRow(owner,label));
        if(owner==null || owner.getChildCount()!=4)return out.toString();
        AccessibilityNodeInfo first=owner.getChild(0),middle=owner.getChild(1),third=owner.getChild(2),role=owner.getChild(3);
        AccessibilityNodeInfo text=middle!=null && middle.getChildCount()==1?middle.getChild(0):null;
        out.append("; ownerVisible=").append(owner.isVisibleToUser()).append("; ownerEnabled=").append(owner.isEnabled()).append("; ownerEmpty=").append(empty(owner))
                .append("; decoration3=").append(decoration(first,3,owner)).append("; decoration2=").append(decoration(third,2,owner));
        if(middle!=null)out.append("; labelBranchInert=").append(inert(middle,owner)).append("; labelBranchEmpty=").append(empty(middle));
        if(text!=null)out.append("; labelTextMatches=").append(ControlResolver.equal(text.getText(),label)).append("; labelDescriptionEmpty=").append(!ControlResolver.nonempty(text.getContentDescription())).append("; labelInert=").append(inert(text,owner));
        if(role!=null)out.append("; roleClass=").append(role.getClassName()).append("; roleVisible=").append(role.isVisibleToUser()).append("; roleEnabled=").append(role.isEnabled()).append("; roleClickable=").append(role.isClickable()).append("; roleChildren=").append(role.getChildCount()).append("; roleEmpty=").append(empty(role)).append("; roleSameWindow=").append(ControlResolver.sameWindow(role,owner));
        AccessibilityNodeInfo root=rootOf(owner);out.append("; rootFound=").append(root!=null);
        if(root!=null)out.append("; newChatAnchors=").append(anchorCount(root,"새 채팅")).append("; searchAnchors=").append(anchorCount(root,"검색"));
        return out.toString();
    }
    static int rowEvidence(AccessibilityNodeInfo owner,String label) {
        if(!possibleRow(owner,label))return NONE;
        AccessibilityNodeInfo root=rootOf(owner);
        return exactRow(owner,label) && root!=null && drawerEvidence(root)==VERIFIED?VERIFIED:AMBIGUOUS;
    }
    static int evidence(AccessibilityNodeInfo owner,String label) {
        return ControlResolver.control(owner,label,true)?VERIFIED:rowEvidence(owner,label);
    }
    static final class Match {
        AccessibilityNodeInfo node;int count;boolean ambiguous;
        int state(){return ambiguous || count>1?AMBIGUOUS:count;}
    }
    static void collect(AccessibilityNodeInfo n,String label,boolean rowOnly,Match result) {
        if(n==null || result.state()==AMBIGUOUS)return;
        int state=rowOnly?rowEvidence(n,label):evidence(n,label);
        if(state==AMBIGUOUS)result.ambiguous=true;
        else if(state==VERIFIED){result.node=n;result.count++;}
        for(int i=0;i<n.getChildCount() && result.state()!=AMBIGUOUS;i++)collect(n.getChild(i),label,rowOnly,result);
    }
    static Match match(AccessibilityNodeInfo root,String label,boolean rowOnly) {
        Match result=new Match();if(label!=null && !label.isEmpty())collect(root,label,rowOnly,result);return result;
    }
    static AccessibilityNodeInfo unique(AccessibilityNodeInfo root,String label) {
        Match result=match(root,label,false);return result.state()==VERIFIED?result.node:null;
    }
    static AccessibilityNodeInfo dotEntry(AccessibilityNodeInfo root) {
        Match result=match(root,"Dot",true);return result.state()==VERIFIED?result.node:null;
    }
}
