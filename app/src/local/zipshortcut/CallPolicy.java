package local.zipshortcut;

/** Pure state machine. A click attempt is consumed BEFORE dispatch, including failed dispatches. */
final class CallPolicy {
    static final long TOTAL_MS=30000, CONFIRM_MS=8000, STABLE_MS=500;
    final long started;
    long candidateSince=-1, markerSince=-1, attemptedAt=-1;
    boolean attempted;
    CallPolicy(long started, boolean attempted, long attemptedAt) {
        this.started=started; this.attempted=attempted; this.attemptedAt=attemptedAt;
    }
    boolean expired(long now) { return now<started || now-started>=TOTAL_MS
            || (attempted && (now<attemptedAt || now-attemptedAt>=CONFIRM_MS)); }
    boolean ready(long now, boolean destination, boolean uniqueButton, boolean marker) {
        if (expired(now) || attempted || !destination || !uniqueButton || marker) {
            candidateSince=-1; return false;
        }
        if (candidateSince<0) candidateSince=now;
        return now-candidateSince>=STABLE_MS;
    }
    boolean consume(long now) {
        if (attempted || expired(now)) return false;
        attempted=true; attemptedAt=now; return true;
    }
    boolean confirmed(long now, boolean marker) {
        if (!attempted || expired(now) || !marker) { markerSince=-1; return false; }
        if (markerSince<0) markerSince=now;
        return now-markerSince>=STABLE_MS;
    }
}
