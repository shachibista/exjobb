package kth.exjobb.autodermo;

import android.graphics.Bitmap;

/**
 * Specifies the server operations.
 */
public interface ServerInterface {
    void getQuestions(OnResponseListener l);
    void uploadImage(Bitmap b, OnResponseListener l);
}
