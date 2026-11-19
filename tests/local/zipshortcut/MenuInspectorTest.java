package local.zipshortcut;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
public final class MenuInspectorTest {
    static int checks;
    static void check(boolean value){checks++;if(!value)throw new AssertionError("Menu-only check "+checks);}
    static AccessibilityNodeInfo control(String label){return FoldControlResolverTest.control(label,4);}
    static AccessibilityNodeInfo normal(){return FoldControlResolverTest.view("",false)
            .add(FoldControlResolverTest.inputContainer(MenuInspector.COMPOSER)).add(control(MenuInspector.MENU)).add(control(MenuInspector.CALL));}
    static ZipService service(){return FoldControlResolverTest.service();}
    public static void main(String[] args){
        AccessibilityNodeInfo root=normal(),menu=root.getChild(1),call=root.getChild(2);
        check(MenuInspector.candidate(root).equals(menu));
        root.add(control(MenuInspector.MENU));check(MenuInspector.candidate(root)==null);
        root=normal().add(control(MenuInspector.END));check(MenuInspector.candidate(root)==null);
        root=normal();root.pkg="other.app";check(MenuInspector.candidate(root)==null);
        root=normal();root.children.remove(0);check(MenuInspector.candidate(root)==null);
        root=normal();root.getChild(1).enabled=false;check(MenuInspector.candidate(root)==null);
        ZipService s=service();s.root=normal();menu=s.root.getChild(1);call=s.root.getChild(2);s.inspectMenuOnce("dot");
        check(!s.running);check(!s.armed());s.inspectStep();check(menu.clicks==1);check(call.clicks==0);
        SystemClock.now=200;s.inspectStep();check(menu.clicks==1);check(call.clicks==0);
        s.root=FoldControlResolverTest.view("",false).add(control("dot"));SystemClock.now=500;s.inspectStep();
        check(s.inspectionDeadline==0);check(ZipService.inspectionReport.contains("recognizedTargetControls=1"));
        check(menu.clicks==1);check(call.clicks==0);
        // An overlay drawer can leave the old composer visible; the verified target row still completes inspection.
        s=service();s.root=normal();menu=s.root.getChild(1);call=s.root.getChild(2);s.inspectMenuOnce("dot");s.inspectStep();
        s.root.add(control("dot"));SystemClock.now=500;s.inspectStep();check(s.inspectionDeadline==0);check(call.clicks==0);check(menu.clicks==1);
        // Failed menu action is not retried, and never falls back to the call control.
        s=service();s.root=normal();menu=s.root.getChild(1);call=s.root.getChild(2);menu.result=false;
        s.inspectMenuOnce("dot");s.inspectStep();s.inspectStep();check(menu.clicks==1);check(call.clicks==0);check(s.inspectionDeadline==0);
        // Label mutation after refresh is rejected even when its owner remains clickable.
        s=service();s.root=normal();FoldControlResolverTest.ViewNode changed=new FoldControlResolverTest.ViewNode("android.view.View","",true){
            @Override public boolean refresh(){((FoldControlResolverTest.ViewNode)children.get(0)).description=MenuInspector.CALL;return true;}};
        changed.add(FoldControlResolverTest.labelBranch(MenuInspector.MENU,1)).add(FoldControlResolverTest.buttonRole());changed.parent=s.root;
        s.root.children.set(1,changed);call=s.root.getChild(2);s.inspectMenuOnce("dot");s.inspectStep();
        check(changed.clicks==0);check(call.clicks==0);check(s.inspectionDeadline==0);
        s=service();s.root=normal();menu=s.root.getChild(1);call=s.root.getChild(2);s.inspectMenuOnce("");s.inspectStep();
        check(menu.clicks==0);check(call.clicks==0);
        // Unknown target ownership is returned as metadata only, never selected or called.
        s=service();s.root=FoldControlResolverTest.view("",false).add(FoldControlResolverTest.view("Dot",false));
        s.inspectMenuOnce("Dot");s.inspectStep();check(s.inspectionDeadline==0);check(!s.inspectionMenuAttempted);
        check(ZipService.inspectionReport.contains("recognizedTargetControls=0"));
        check(ZipService.inspectionReport.contains("not a validated selection"));
        String report=ScreenInspector.targetSummary(FoldControlResolverTest.view("",false).add(control("dot"))
                .add(FoldControlResolverTest.view("PRIVATE_OTHER_TITLE",true)),"dot");
        check(report.contains("recognizedTargetControls=1"));check(!report.contains("PRIVATE_OTHER_TITLE"));
        System.out.println("PASS "+checks+" menu-only diagnostic checks (no call dispatch)");
    }
}
