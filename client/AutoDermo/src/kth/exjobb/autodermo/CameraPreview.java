package kth.exjobb.autodermo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.*;

import java.io.IOException;

/**
 * Custom CameraPreview view that subscribes to the
 * camera preview and sets a 1:1 aspect ratio
 */

public class CameraPreview extends TextureView implements TextureView.SurfaceTextureListener {

    private final CameraInterface camera;

    public CameraPreview(Context context, CameraInterface camera) {
        super(context);

        this.camera = camera;

        setSurfaceTextureListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        // set dimensions to be a square
        setMeasuredDimension(width, width);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Camera.Parameters parameters = camera.getParameters();

        // we use auto-focus here since we do not yet know the optical characteristics
        // of the dermoscopic attachment
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);

        try {
            camera.setPreviewTexture(surface);

            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if(camera != null) {
            camera.stopPreview();
        }

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
