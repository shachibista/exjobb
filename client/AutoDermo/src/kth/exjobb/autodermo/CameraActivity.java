package kth.exjobb.autodermo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.*;

public class CameraActivity extends Activity {
    private DermoCameraFragment cameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        cameraFragment = DermoCameraFragment.newInstance(0);

        getFragmentManager().beginTransaction().replace(R.id.camera, cameraFragment).commit();

        ShutterButton shutter = (ShutterButton) findViewById(R.id.shutter);

        final Toast toast = Toast.makeText(CameraActivity.this, "Done", Toast.LENGTH_LONG);

        shutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Bitmap img = cameraFragment.takePicture();

                            Server.getInstance().uploadImage(img, new OnResponseListener() {
                                @Override
                                public void onResponse(ServerResponse r) {
                                    toast.show();
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).run();
            }
        });
    }
}