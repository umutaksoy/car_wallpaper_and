package lamborghini.wallpapers.CarWallpapers.CarSounds.activities;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import lamborghini.wallpapers.CarWallpapers.CarSounds.Config;
import lamborghini.wallpapers.CarWallpapers.CarSounds.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ActivityPrivacyPolicy extends AppCompatActivity {

    WebView wv_privacy_policy;
    ProgressBar progressBar;
    Button btn_failed_retry;
    View lyt_failed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        setupToolbar();
        wv_privacy_policy = findViewById(R.id.privacy_policy);
        progressBar = findViewById(R.id.progressBar);
        btn_failed_retry = findViewById(R.id.failed_retry);
        lyt_failed = findViewById(R.id.lyt_failed);

        displayData();

        btn_failed_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lyt_failed.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                displayData();
            }
        });
    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(R.string.about_app_privacy_policy);
        }
    }

    public void displayData() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 1000);
    }

    public void loadData() {
        new MyTask().execute("https://storage.googleapis.com/mythical-ace-4987/yt-project/privacy-policy.json");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private class MyTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            setJsonToObj(result);
        }
    }

    public static String getJSONString(String url) {
        String jsonString = null;
        HttpURLConnection linkConnection = null;
        try {
            URL linkurl = new URL(url);
            linkConnection = (HttpURLConnection) linkurl.openConnection();
            int responseCode = linkConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream linkinStream = linkConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int j = 0;
                while ((j = linkinStream.read()) != -1) {
                    baos.write(j);
                }
                byte[] data = baos.toByteArray();
                jsonString = new String(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (linkConnection != null) {
                linkConnection.disconnect();
            }
        }
        return jsonString;
    }

    public void setJsonToObj(String tmpMainJson){

        if (tmpMainJson == null){
            Log.e("umut", "ActivityPrivacyPolicy setJsonToObj: json data is null");
        }else{

            if (tmpMainJson.length() <= 0){
                Log.e("umut", "ActivityPrivacyPolicy setJsonToObj: json data size is 0");
            }else{
                try {
                    JSONObject mainJson = new JSONObject(tmpMainJson);

                    String privacy_policy = mainJson.getString("privacy_policy");
                    try {

                        if (Config.ENABLE_RTL_MODE) {
                            wv_privacy_policy.setBackgroundColor(Color.parseColor("#ffffff"));
                            wv_privacy_policy.setFocusableInTouchMode(false);
                            wv_privacy_policy.setFocusable(false);
                            wv_privacy_policy.getSettings().setDefaultTextEncodingName("UTF-8");

                            WebSettings webSettings = wv_privacy_policy.getSettings();
                            Resources res = getResources();
                            int fontSize = res.getInteger(R.integer.font_size);
                            webSettings.setDefaultFontSize(fontSize);

                            String mimeType = "text/html; charset=UTF-8";
                            String encoding = "utf-8";
                            String htmlText = privacy_policy;

                            String text = "<html dir='rtl'><head>"
                                    + "<style type=\"text/css\">body{color: #525252;}"
                                    + "</style></head>"
                                    + "<body>"
                                    + htmlText
                                    + "</body></html>";

                            wv_privacy_policy.loadDataWithBaseURL(null, text, mimeType, encoding, null);

                            progressBar.setVisibility(View.GONE);
                            lyt_failed.setVisibility(View.GONE);
                        } else {
                            wv_privacy_policy.setBackgroundColor(Color.parseColor("#ffffff"));
                            wv_privacy_policy.setFocusableInTouchMode(false);
                            wv_privacy_policy.setFocusable(false);
                            wv_privacy_policy.getSettings().setDefaultTextEncodingName("UTF-8");

                            WebSettings webSettings = wv_privacy_policy.getSettings();
                            Resources res = getResources();
                            int fontSize = res.getInteger(R.integer.font_size);
                            webSettings.setDefaultFontSize(fontSize);

                            String mimeType = "text/html; charset=UTF-8";
                            String encoding = "utf-8";
                            String htmlText = privacy_policy;

                            String text = "<html><head>"
                                    + "<style type=\"text/css\">body{color: #525252;}"
                                    + "</style></head>"
                                    + "<body>"
                                    + htmlText
                                    + "</body></html>";

                            wv_privacy_policy.loadDataWithBaseURL(null, text, mimeType, encoding, null);

                            progressBar.setVisibility(View.GONE);
                            lyt_failed.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        Log.e("umut izleme", "ActivityPrivacyPolicy onResponse error, detail : " + e.toString());
                        e.printStackTrace();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }

}
