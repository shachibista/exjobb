package kth.exjobb.autodermo;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;

/**
 * Wraps the native camera in a "friendlier" interface
 * and handles preview starting/stopping and camera handle
 * releasing
 */
public class CameraWrapper implements CameraInterface {
    private Camera cameraDevice;
    private boolean released = true;
    private boolean previewing = false;

    private CameraWrapper(Camera camera){
        cameraDevice = camera;
        released = false;
    }

    public static CameraWrapper open(int cameraId){
        return new CameraWrapper(Camera.open(cameraId));
    }

    public void startPreview(){
        if(cameraDevice != null && !previewing){
            cameraDevice.startPreview();
            previewing = true;
        }
    }

    public void stopPreview(){
        if(cameraDevice != null && previewing){
            if(!released) {
                cameraDevice.stopPreview();
            }
            previewing = false;
        }
    }

    public void release(){
        if(cameraDevice != null && !released){
            cameraDevice.release();
            released = true;
            previewing = false;
        }
    }

    public void setDisplayOrientation(int deg){
        cameraDevice.setDisplayOrientation(deg);
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture) throws IOException {
        cameraDevice.setPreviewTexture(surfaceTexture);
    }

    public void setParameters(Camera.Parameters parameters){
        cameraDevice.setParameters(parameters);
    }

    public Camera.Parameters getParameters(){
        return cameraDevice.getParameters();
    }

    public Camera getNativeCamera(){
        return cameraDevice;
    }
}
