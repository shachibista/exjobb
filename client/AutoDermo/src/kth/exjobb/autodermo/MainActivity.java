package kth.exjobb.autodermo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends Activity
{
    private ProgressBar progressBar;

    private LinearLayout formLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        formLayout = (LinearLayout) findViewById(R.id.form_container);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // get the questions as soon as the activity is created
        Server.getInstance().getQuestions(new OnResponseListener() {
            @Override
            public void onResponse(final ServerResponse r) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);

                        List<FormBuilder.ElementGroup> elements;
                        // build the form according to questions JSON structure
                        // TODO: Validate the schema before passing it to FormBuilder
                        elements = FormBuilder.build(formLayout, r.content);

                        // append a submit button to the form so that we can move forward
                        Button submitButton = new Button(MainActivity.this);
                        submitButton.setText("Next");

                        final List<FormBuilder.ElementGroup> finalElements = elements;
                        submitButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (finalElements != null) {
                                    JSONArray values = new JSONArray();
                                    for (FormBuilder.ElementGroup e : finalElements) {
                                        try {
                                            values.put(e.getValue());
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    /*
                                     attach form data to the intent so that it can be passed to the server
                                     in a real application, this would be a complex chain of interaction
                                     where the questions are modelled as a decision tree, and can take one
                                     of many paths
                                      */
                                    Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
                                    cameraIntent.putExtra("pdata", values.toString());

                                    MainActivity.this.startActivity(cameraIntent);
                                }
                            }
                        });
                        formLayout.addView(submitButton);
                    }
                });
            }
        });
    }
}
