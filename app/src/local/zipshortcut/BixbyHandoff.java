package local.zipshortcut;
import android.view.accessibility.AccessibilityNodeInfo;

/** Optional initial handoff only. No content matching, call dispatch, or arbitrary-app Back. */
final class BixbyHandoff {
    static final String PACKAGE="com.samsung.android.bixby.agent";
    static final long STABLE_MS=500;
    int window=-1; long since=-1;
    static boolean bixby(AccessibilityNodeInfo n) {
        return n!=null && n.isVisibleToUser() && n.isEnabled() && n.getWindowId()>=0 && ControlResolver.equal(n.getPackageName(),PACKAGE);
    }
    static boolean same(AccessibilityNodeInfo a,AccessibilityNodeInfo b) {
        return bixby(a) && bixby(b) && a.getWindowId()==b.getWindowId();
    }
    void reset(){window=-1;since=-1;}
    boolean ready(AccessibilityNodeInfo root,long now) {
        if(!bixby(root)){reset();return false;}
        if(window!=root.getWindowId() || since<0 || now<since){window=root.getWindowId();since=now;return false;}
        return now-since>=STABLE_MS;
    }
}
