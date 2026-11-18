package local.zipshortcut;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;

public final class NativeNavigationTest {
    static int checks;
    static void check(boolean value){checks++;if(!value)throw new AssertionError("Native navigation check "+checks);}
    static FoldControlResolverTest.ViewNode view(){return FoldControlResolverTest.view("",false);}
    static AccessibilityNodeInfo emptyChain(int count){AccessibilityNodeInfo first=view(),last=first;for(int i=1;i<count;i++){AccessibilityNodeInfo next=view();last.add(next);last=next;}return first;}
    static AccessibilityNodeInfo row(String label){
        AccessibilityNodeInfo row=FoldControlResolverTest.view("",true),textBranch=view(),text=new FoldControlResolverTest.ViewNode("android.widget.TextView","",false);text.label=label;
        textBranch.add(text);row.add(emptyChain(3)).add(textBranch).add(emptyChain(2)).add(FoldControlResolverTest.buttonRole());return row;
    }
    static AccessibilityNodeInfo drawer(){
        return view().add(row("Dot")).add(new FoldControlResolverTest.ViewNode("android.widget.Button","새 채팅",true))
                .add(FoldControlResolverTest.control("검색",4));
    }
    static ZipService service(){return FoldControlResolverTest.service();}
    public static void main(String[] args){
        AccessibilityNodeInfo root=drawer(),dot=root.getChild(0);
        check(NativeNavigation.exactRow(dot,"Dot"));check(NativeNavigation.rowEvidence(dot,"Dot")==1);
        check(NativeNavigation.unique(root,"Dot").equals(dot));check(NativeNavigation.dotEntry(root).equals(dot));
        check(NativeNavigation.drawerEvidence(root)==1);
        root.add(row("Dot"));check(NativeNavigation.match(root,"Dot",false).state()==2);check(NativeNavigation.dotEntry(root)==null);
        root=drawer();root.children.remove(2);dot=root.getChild(0);check(NativeNavigation.rowEvidence(dot,"Dot")==2);
        root=drawer().add(FoldControlResolverTest.control("검색",4));check(NativeNavigation.rowEvidence(root.getChild(0),"Dot")==2);
        root=drawer();dot=root.getChild(0);dot.getChild(0).label="unexpected decoration";
        check(!NativeNavigation.exactRow(dot,"Dot"));check(NativeNavigation.rowEvidence(dot,"Dot")==2);
        check(NativeNavigation.unique(root,"Dot")==null);
        root=drawer();dot=root.getChild(0);dot.getChild(3).clickable=true;check(NativeNavigation.rowEvidence(dot,"Dot")==2);
        root=drawer();dot=root.getChild(0);dot.getChild(1).getChild(0).window=2;check(NativeNavigation.rowEvidence(dot,"Dot")==2);
        root=drawer();dot=root.getChild(0);check(NativeNavigation.rowEvidence(dot,"dot")==0);
        root=drawer();dot=root.getChild(0);dot.getChild(2).children.clear();check(NativeNavigation.rowEvidence(dot,"Dot")==1);
        root=drawer();dot=root.getChild(0);dot.getChild(2).getChild(0).visible=false;check(NativeNavigation.rowEvidence(dot,"Dot")==1);
        root=drawer();dot=root.getChild(0);dot.getChild(2).getChild(0).clickable=true;check(NativeNavigation.rowEvidence(dot,"Dot")==2);
        root=drawer();dot=root.getChild(0);dot.getChild(2).getChild(0).label="unexpected";check(NativeNavigation.rowEvidence(dot,"Dot")==2);
        // Unknown/duplicated drawer evidence blocks calls rather than collapsing to marker absence.
        ZipService s=service();s.labels[2]="Dot";s.root=drawer();s.root.add(FoldControlResolverTest.inputContainer(MenuInspector.COMPOSER));
        AccessibilityNodeInfo call=FoldControlResolverTest.control("통화",4);s.root.add(call);s.root.getChild(0).getChild(0).label="unknown decoration";
        s.step();check(!s.running);check(call.clicks==0);check(s.preferences.getString("lastResult","").equals(ResultStatus.AMBIGUOUS));
        // Explicit fixed-entry diagnostic invokes only Dot once and never the call owner.
        s=service();s.root=drawer();dot=s.root.getChild(0);s.inspectEntryOnce();s.inspectStep();check(dot.clicks==1);
        SystemClock.now=200;s.inspectStep();check(dot.clicks==1);
        s.root=view().add(FoldControlResolverTest.inputContainer(MenuInspector.COMPOSER)).add(FoldControlResolverTest.control(MenuInspector.MENU,4)).add(FoldControlResolverTest.control(MenuInspector.CALL,4));
        call=s.root.getChild(2);SystemClock.now=500;s.inspectStep();check(call.clicks==0);check(s.inspectionDeadline==0);
        check(ZipService.inspectionReport.contains("entry attempts=1; no call dispatch"));
        s=service();s.root=drawer();dot=s.root.getChild(0);dot.result=false;s.inspectEntryOnce();s.inspectStep();s.inspectStep();check(dot.clicks==1);check(s.inspectionDeadline==0);
        // Entry source cannot alias a compact call button, even if it is relabelled Dot.
        s=service();s.root=view().add(FoldControlResolverTest.control("Dot",4));call=s.root.getChild(0);s.inspectEntryOnce();s.inspectStep();check(call.clicks==0);
        s=service();check(CallSettings.applyObservedFoldProfile(s));check(DotSettings.name(s).equals("dot"));
        check(DotSettings.entry(s).equals("Dot"));check(!CallSettings.configured(s));check(!s.preferences.getBoolean("consent",true));check(!s.armed());
        // Actual navigation path revalidates and dispatches the same native owner used by the diagnostic.
        s=service();s.targetEntry="Dot";s.labels[2]="Dot";s.root=drawer();dot=s.root.getChild(0);s.step();check(dot.clicks==1);
        // Full synthetic native drawer -> Dot composer -> one call -> stable actual end marker.
        s=service();s.targetEntry="Dot";s.labels[2]="Dot";s.labels[5]="종료";
        s.root=drawer();dot=s.root.getChild(0);call=FoldControlResolverTest.control("통화",4);
        s.root.add(FoldControlResolverTest.inputContainer(MenuInspector.COMPOSER)).add(call);s.step();
        check(dot.clicks==1);check(call.clicks==0);
        s.root=view().add(FoldControlResolverTest.inputContainer(MenuInspector.COMPOSER)).add(call);
        SystemClock.now=200;s.step();SystemClock.now=700;s.step();check(call.clicks==1);
        SystemClock.now=800;s.step();check(call.clicks==1);
        s.root=view().add(FoldControlResolverTest.control("종료",4));SystemClock.now=900;s.step();SystemClock.now=1400;s.step();
        check(!s.running);check(s.preferences.getString("lastResult","").equals(ResultStatus.CONFIRMED));
        System.out.println("PASS "+checks+" native drawer/entry-only checks (observed four-child shape; synthetic runtime)");
    }
}
