package kth.exjobb.autodermo;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import com.squareup.okhttp.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Simple implementation of Server interface
 */
public class Server implements ServerInterface {
    // we hardcode this for now, possible should be saved in the preferences
    // or some other storage
    private static final String QUESTIONS_ENDPOINT = "http://192.168.43.49:5000/questions";
    private static final String UPLOAD_ENDPOINT = "http://192.168.43.49:5000/upload";

    // Implement server as a singleton for simplicity
    private Server(){}

    private static Server instance = null;

    static {
        instance = new Server();
    }

    public static Server getInstance(){
        return instance;
    }

    /**
     * Gets the questions list, delegates to an AsyncTask so that
     * main thread isn't blocked
     * @param l
     */
    @Override
    public void getQuestions(OnResponseListener l) {
        new GetQuestionsTask().execute(l);
    }

    /**
     * Uploads the image to the server
     * @param b
     * @param listener
     */
    public void uploadImage(final Bitmap b, final OnResponseListener listener){
        OkHttpClient httpClient = new OkHttpClient();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // encode image as PNG, re-encoding as JPG may cause compression artefacts to be visible
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);

        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addPart(
                        // the filename parameter is needed to specify that this is indeed a file and not an inline
                        // attachment
                        Headers.of("Content-Disposition", "form-data; name=\"image\"; filename=\"image.png\""),
                        RequestBody.create(MediaType.parse("image/png"), bos.toByteArray())
                )
                .build();
        Request request = new Request.Builder().url(UPLOAD_ENDPOINT).post(requestBody).build();

        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                listener.onResponse(new ServerResponse(response.code(), response.body().string()));
            }
        });
    }

    private class GetQuestionsTask extends AsyncTask<OnResponseListener, Void, Void> {
        @Override
        protected Void doInBackground(OnResponseListener... params) {
            OnResponseListener l = params[0];
            OkHttpClient httpClient = new OkHttpClient();

            Request request = new Request.Builder().url(QUESTIONS_ENDPOINT).build();
            try {
                Response response = httpClient.newCall(request).execute();

                l.onResponse(new ServerResponse(response.code(), response.body().string()));
            } catch (IOException e) {
                l.onResponse(new ServerResponse(404, "[]"));
            }

            return null;
        }
    }
}
