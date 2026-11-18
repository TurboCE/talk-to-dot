package local.zipshortcut;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;

/** Single menu-only probe for the observed Korean Dot UI. No configurable click label and no call dispatch. */
final class MenuInspector {
    static final String MENU="메뉴", CALL="통화", COMPOSER="내 dot에게 메시지 보내기", END="종료";
    static AccessibilityNodeInfo candidate(AccessibilityNodeInfo root) {
        if(root==null || !ControlResolver.equal(root.getPackageName(),"com.openai.chatgpt"))return null;
        List<AccessibilityNodeInfo> menu=new ArrayList<>(),calls=new ArrayList<>();int[] inputs={0},ends={0};
        walk(root,menu,calls,inputs,ends);
        return menu.size()==1 && calls.size()==1 && inputs[0]==1 && ends[0]==0 && !menu.get(0).equals(calls.get(0))?menu.get(0):null;
    }
    static void walk(AccessibilityNodeInfo n,List<AccessibilityNodeInfo> menu,List<AccessibilityNodeInfo> calls,int[] inputs,int[] ends) {
        if(n==null)return;
        if(ControlResolver.control(n,MENU,true))menu.add(n);
        if(ControlResolver.control(n,CALL,true))calls.add(n);
        if(ControlResolver.control(n,END,true))ends[0]++;
        if(ControlResolver.input(n,COMPOSER))inputs[0]++;
        for(int i=0;i<n.getChildCount();i++)walk(n.getChild(i),menu,calls,inputs,ends);
    }
}
