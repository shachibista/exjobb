package kth.exjobb.autodermo;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;

public interface CameraInterface {
    void startPreview();
    void stopPreview();
    void release();
    void setDisplayOrientation(int deg);
    void setPreviewTexture(SurfaceTexture surfaceTexture) throws IOException;
    void setParameters(Camera.Parameters parameters);

    Camera.Parameters getParameters();
}
