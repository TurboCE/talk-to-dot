package local.zipshortcut;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;

/** Synthetic fixtures contain only observed control labels/roles; no conversation content or raw device dump. */
public final class FoldControlResolverTest {
    static int checks;
    static void check(boolean value) { checks++;if(!value)throw new AssertionError("Fold control regression "+checks); }
    static class ViewNode extends AccessibilityNodeInfo {
        String role,description;
        ViewNode(String role,String description,boolean clickable) {super("");this.role=role;this.description=description;this.clickable=clickable;}
        @Override public CharSequence getClassName(){return role;}
        @Override public CharSequence getContentDescription(){return description;}
    }
    static ViewNode view(String description,boolean clickable) {return new ViewNode("android.view.View",description,clickable);}
    static ViewNode buttonRole(){return new ViewNode("android.widget.Button","",false);}
    static ViewNode labelBranch(String label,int depth) {
        ViewNode first=view(depth==1?label:"",false),last=first;
        for(int i=1;i<depth;i++) {ViewNode next=view(i==depth-1?label:"",false);last.add(next);last=next;}
        return first;
    }
    static ViewNode control(String label,int depth) {
        ViewNode owner=view("",true);owner.add(labelBranch(label,depth)).add(buttonRole());return owner;
    }
    static ViewNode inputContainer(String label) {
        ViewNode parent=view("",false),edit=new ViewNode("android.widget.EditText","",true);edit.editable=true;
        parent.add(labelBranch(label,1)).add(edit);return parent;
    }
    static ZipService service() {
        SystemClock.now=100;ZipService s=new ZipService();
        s.preferences.edit().putLong("startedAtElapsed",100).putInt("boot",1).putBoolean("consent",true)
                .putString("requestProcess",CallSettings.PROCESS_ID).putString("composer","내 {dot}에게 메시지 보내기")
                .putString("dotName","dot").putString("call","통화").putString("connected","TEST_END").apply();
        s.root=view("",false);s.begin();return s;
    }
    static void tick(ZipService s,long now){SystemClock.now=now;s.step();}
    public static void main(String[] args) {
        // Observed call/menu owner: clickable View + four-deep nonclick label branch + sibling Button role.
        ViewNode call=control("통화",4);check(ControlResolver.control(call,"통화",true));
        check(!ControlResolver.control(call,"other",true));check(!ControlResolver.control(call.getChild(0),"통화",true));
        ViewNode input=inputContainer("내 dot에게 메시지 보내기");check(ControlResolver.input(input.getChild(1),"내 dot에게 메시지 보내기"));
        check(!ControlResolver.input(input.getChild(1),"내 other에게 메시지 보내기"));
        // Missing/multiple Button roles and deep/broad/interactive label branches cannot authorize a click.
        ViewNode missing=control("통화",4);missing.children.remove(1);check(!ControlResolver.control(missing,"통화",true));
        ViewNode two=control("통화",4);two.add(buttonRole());check(!ControlResolver.control(two,"통화",true));
        ViewNode deep=control("통화",5);check(!ControlResolver.control(deep,"통화",true));
        ViewNode branchClick=control("통화",4);branchClick.children.get(0).clickable=true;check(!ControlResolver.control(branchClick,"통화",true));
        ViewNode broad=control("통화",4);broad.children.get(0).add(view("extra",false));check(!ControlResolver.control(broad,"통화",true));
        ViewNode duplicate=control("통화",4);((ViewNode)duplicate.children.get(0)).description="통화";check(!ControlResolver.control(duplicate,"통화",true));
        ViewNode disabledRole=control("통화",4);disabledRole.children.get(1).enabled=false;check(!ControlResolver.control(disabledRole,"통화",true));
        ViewNode foreign=control("통화",4);foreign.children.get(0).pkg="other.app";check(!ControlResolver.control(foreign,"통화",true));
        ViewNode wrongWindow=control("통화",4);wrongWindow.children.get(1).window=2;check(!ControlResolver.control(wrongWindow,"통화",true));
        ViewNode interactiveRole=control("통화",4);interactiveRole.children.get(1).clickable=true;check(!ControlResolver.control(interactiveRole,"통화",true));
        ViewNode labelledRole=control("통화",4);((ViewNode)labelledRole.children.get(1)).description="different";check(!ControlResolver.control(labelledRole,"통화",true));
        ViewNode textBody=new ViewNode("android.widget.TextView","통화",true);check(!ControlResolver.control(textBody,"통화",true));
        ViewNode genericParent=view("",true);genericParent.add(view("통화",false));check(!ControlResolver.control(genericParent,"통화",true));
        // An unrelated label anywhere in a large ancestor does not qualify as the input's own label.
        ViewNode large=view("",false),edit=new ViewNode("android.widget.EditText","",true);edit.editable=true;
        large.add(edit).add(view("unrelated",false)).add(view("내 dot에게 메시지 보내기",false));
        check(!ControlResolver.input(edit,"내 dot에게 메시지 보내기"));
        ViewNode badInput=inputContainer("내 dot에게 메시지 보내기");badInput.children.get(0).clickable=true;
        check(!ControlResolver.input(badInput.getChild(1),"내 dot에게 메시지 보내기"));
        // Full service keeps single-attempt semantics with the actual observed owner/input representation.
        ZipService s=service();call=control("통화",4);s.root.add(inputContainer("내 dot에게 메시지 보내기")).add(call);
        tick(s,100);check(call.clicks==0);tick(s,600);check(call.clicks==1);tick(s,800);check(call.clicks==1);
        // TEST_END is a fixture label only, not a claimed device-observed call-screen identifier.
        s.root=view("",false).add(control("TEST_END",1));tick(s,900);tick(s,1400);check(!s.running);
        check(s.preferences.getString("lastResult","").equals(ResultStatus.CONFIRMED));
        s=service();call=control("통화",4);s.root.add(inputContainer("내 dot에게 메시지 보내기")).add(call).add(control("TEST_END",2)).add(control("TEST_END",2));
        tick(s,100);tick(s,600);check(call.clicks==0);check(!s.running);check(s.preferences.getString("lastResult","").equals(ResultStatus.AMBIGUOUS));
        // A nested label changing during owner refresh cannot dispatch the owner's action.
        s=service();ViewNode changed=new ViewNode("android.view.View","",true){
            @Override public boolean refresh(){((ViewNode)children.get(0)).description="NOT_CALL";return true;}};
        changed.add(labelBranch("통화",1)).add(buttonRole());s.root.add(inputContainer("내 dot에게 메시지 보내기")).add(changed);
        tick(s,100);tick(s,600);check(changed.clicks==0);
        s=service();call=control("통화",4);s.root.add(inputContainer("내 dot에게 메시지 보내기")).add(call).add(control("통화",4));
        tick(s,100);tick(s,600);check(call.clicks==0);
        // Read-only diagnostics expose verified button labels / input hints, never editable drafts or transcript.
        ViewNode privateRoot=view("",false),privateInput=new ViewNode("android.widget.EditText","",true);
        privateInput.editable=true;privateInput.label="PRIVATE_DRAFT";
        privateRoot.add(privateInput).add(new ViewNode("android.widget.TextView","PRIVATE_BODY",true))
                .add(control("통화",4)).add(inputContainer("내 dot에게 메시지 보내기"));
        String report=ScreenInspector.scan(privateRoot);
        check(report.contains("Button: 통화"));check(report.contains("Input label: 내 dot에게 메시지 보내기"));
        check(!report.contains("PRIVATE_DRAFT"));check(!report.contains("PRIVATE_BODY"));
        s=service();call=control("통화",4);s.root.add(inputContainer("내 dot에게 메시지 보내기")).add(call);
        s.inspectOnce();check(!s.running);check(!s.armed());s.inspectStep();check(call.clicks==0);
        check(s.inspectionDeadline==0);check(ZipService.inspectionReport.contains("통화"));
        System.out.println("PASS "+checks+" Fold7 structural regression checks (observed normal-screen shape; synthetic runtime)");
    }
}
