package kth.exjobb.autodermo;

/**
 * A listener class for asynchronous operations.
 * Better to use Futures/RxJava but this is much simpler
 * for this use-case.
 */
public interface OnResponseListener {
    void onResponse(ServerResponse r);
}
