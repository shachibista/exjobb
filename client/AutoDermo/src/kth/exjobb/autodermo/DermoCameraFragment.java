package kth.exjobb.autodermo;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * Fragment that encapsulates the custom CameraPreview view
 */

public class DermoCameraFragment extends Fragment {
    private CameraWrapper camera;
    private int cameraId;
    private TextureView cameraView;

    public static DermoCameraFragment newInstance(int cameraId){
        DermoCameraFragment f = new DermoCameraFragment();

        Bundle args = new Bundle();
        args.putInt("cameraId", cameraId);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_camera, container, false);

        Bundle arguments = getArguments();
        cameraId = arguments.getInt("cameraId", 0);

        openCamera(cameraId);

        cameraView = new CameraPreview(getActivity(), camera);
        // rescale the view so that the image is in the correct aspect ratio
        cameraView.setScaleY((float) (4.0 / 3.0));

        FrameLayout cameraFrame = (FrameLayout) content.findViewById(R.id.underlay_frame);
        cameraFrame.addView(cameraView);

        return content;
    }

    private void openCamera(int cameraId) {
        camera = CameraWrapper.open(cameraId);

        // rotate the camera view 90 degrees, since the sensor is
        // mounted in the landscape mode
        camera.setDisplayOrientation(90);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(camera != null){
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(camera == null){
            openCamera(cameraId);
        }
    }

    public Bitmap takePicture() throws IOException {
        if(cameraView != null){
            // this isn't the most optimal way of getting
            // the image, but suffices in our case.
            // Practically, we need to wait for the autofocus to stabilize
            // before we take a picture
            return cameraView.getBitmap();
        }

        throw new IOException();
    }
}
