package local.zipshortcut;
public final class CallPolicyTest {
    static int checks;
    static void check(boolean value) { checks++; if(!value)throw new AssertionError("check "+checks); }
    public static void main(String[] args) {
        CallPolicy p=new CallPolicy(100,false,-1);
        check(!p.ready(100,false,true,false));
        check(!p.ready(110,true,false,false));
        check(!p.ready(120,true,true,true));
        check(!p.ready(130,true,true,false));
        check(!p.ready(629,true,true,false));
        check(p.ready(630,true,true,false));
        check(p.consume(630)); check(!p.consume(631));
        check(!p.ready(1200,true,true,false));
        check(!p.confirmed(1200,true)); check(p.confirmed(1700,true));
        check(!p.confirmed(1701,false)); check(!p.confirmed(1702,true));
        CallPolicy restarted=new CallPolicy(100,true,630);
        check(!restarted.consume(900)); check(!restarted.ready(1000,true,true,false));
        check(!restarted.expired(8629)); check(restarted.expired(8630));
        CallPolicy timeout=new CallPolicy(100,false,-1);
        check(!timeout.expired(30099)); check(timeout.expired(30100)); check(timeout.expired(99));
        check(!timeout.consume(30100));
        CallPolicy unstable=new CallPolicy(0,false,-1);
        check(!unstable.ready(10,true,true,false)); check(!unstable.ready(509,true,true,false));
        check(!unstable.ready(510,false,true,false)); check(!unstable.ready(511,true,true,false));
        check(!unstable.ready(1010,true,true,false)); check(unstable.ready(1011,true,true,false));
        System.out.println("PASS "+checks+" call-policy checks");
    }
}
