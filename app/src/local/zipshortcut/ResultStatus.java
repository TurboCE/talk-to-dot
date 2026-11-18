package local.zipshortcut;

/** Persist stable names, never generated Android resource integers. Unknown/legacy values are ignored. */
final class ResultStatus {
    static final String AMBIGUOUS="ambiguous_state";
    static final String CANCELLED="cancelled", CONFIGURE="configure_first", UNCONFIRMED="unconfirmed",
            TIMEOUT="timed_out", LOCKED="locked", CONFIRMED="confirmed", ALREADY="already_call", CLICK_FAILED="click_failed";
    static int message(String code) {
        if(code==null)return 0;
        switch(code) {
            case AMBIGUOUS:return R.string.ambiguous_state;
            case CANCELLED:return R.string.cancelled;
            case CONFIGURE:return R.string.configure_first;
            case UNCONFIRMED:return R.string.unconfirmed;
            case TIMEOUT:return R.string.timed_out;
            case LOCKED:return R.string.locked;
            case CONFIRMED:return R.string.confirmed;
            case ALREADY:return R.string.already_call;
            case CLICK_FAILED:return R.string.click_failed;
            default:return 0;
        }
    }
}
