package com.flask.colorpicker.sample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.net.URLConnection;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class SampleActivity extends ActionBarActivity {
    private View root;
    private int currentBackgroundColor = 0xffffffff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        root = findViewById(R.id.color_screen);
        changeBackgroundColor(currentBackgroundColor);

        findViewById(R.id.btn_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = SampleActivity.this;

                ColorPickerDialogBuilder
                        .with(context)
                        .setTitle(R.string.color_dialog_title)
                        .initialColor(currentBackgroundColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int selectedColor) {
                                // Handle on color change
                                Log.d("ColorPicker", "onColorChanged: 0x" + Integer.toHexString(selectedColor));
                            }
                        })
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                changeBackgroundColor(selectedColor);
                                if (allColors != null) {
                                    StringBuilder sb = null;

                                    for (Integer color : allColors) {
                                        if (color == null)
                                            continue;
                                        if (sb == null)
                                            sb = new StringBuilder("Color List:");
                                        sb.append("\r\n#" + Integer.toHexString(color).toUpperCase());
                                    }

                                    if (sb != null)
                                        Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_SHORT).show();
                                }


                                int r = (selectedColor >> 16) & 0xFF;
                                int g = (selectedColor >> 8) & 0xFF;
                                int b = (selectedColor >> 0) & 0xFF;
                                Log.d("RGB", "R [" + r + "] - G [" + g + "] - B [" + b + "]");
                                sendColor(r, g, b);
                            }

                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .showColorEdit(true)
                        .setColorEditTextColor(ContextCompat.getColor(SampleActivity.this, android.R.color.holo_blue_bright))
                        .build()
                        .show();
            }
        });
        findViewById(R.id.btn_prefs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SampleActivity.this, PrefsActivity.class));
            }
        });
        findViewById(R.id.btn_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SampleActivity.this, SampleActivity2.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/QuadFlask/colorpicker";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_fragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(SampleActivity.this, SampleActivity3.class);
                startActivity(intent);
            }
        });


    }

    private void changeBackgroundColor(int selectedColor) {
        currentBackgroundColor = selectedColor;
        root.setBackgroundColor(selectedColor);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    /**
     * Our main task is to send this color from Http connection.
     *
     * @param r
     * @param g
     * @param b
     */
    private void sendColor(int r, int g, int b) {
        OkHttpClient client = new OkHttpClient();

        Log.d("RGB", "Sending data to Arduino....");

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("192.168.1.177")
                .build();

//        String url = "http://192.168.1.177/";

        Log.d("url:",  url.toString());

        Request req = new Request.Builder()
                .url(url)
                .post(
                        RequestBody.create(JSON, createJSON(r, g, b))
                )
                .build();

//        TextView tv = (TextView) findViewById(R.id.btn_dialog);

//        tv.setText("Sending data to Arduino...");
//        System.out.print("finish!!!!!!!");
//        tv.clearComposingText();
//        tv.append("Sending data to Arduino...");

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
//                Log.i(TAG, e.getMessage());// Handle call failure
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                // OK Response...inform the user
            }


        });

//        System.out.print("finish!!!!!!!");
//        tv.setText("111111Sending data to Arduino...");
    }

    /**
     * create JSON from r, g, b components.
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    private String createJSON(int r, int g, int b) {
        return "{\"color\": [" + r + "," + g + "," + b + "]}";
    }

}
