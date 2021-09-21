package lamborghini.wallpapers.CarWallpapers.CarSounds.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import lamborghini.wallpapers.CarWallpapers.CarSounds.Config;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import lamborghini.wallpapers.CarWallpapers.CarSounds.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ActivitySplash extends AppCompatActivity {

    Boolean isCancelled = false;
    private ProgressBar progressBar;
    Activity act;
    String intentCategoryName = "";
    String intentVid = "";
    String intentVideoId = "";
    String intentVideoThumbnail = "";
    String intentVideoTitle = "";
    String intentVideoUrl = "";
    String intentVideoType = "";
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        act = this;
        new MyTask().execute("https://storage.googleapis.com/mythical-ace-4987/yt-project/feedposttotalpage.txt");

        if(getIntent().hasExtra("vid")) {
            Intent intent = getIntent();
            intentCategoryName = intent.getStringExtra("category_name");
            intentVid = intent.getStringExtra("vid");
            intentVideoId = intent.getStringExtra("video_id");
            intentVideoThumbnail = intent.getStringExtra("video_thumbnail");
            intentVideoTitle = intent.getStringExtra("video_title");
            intentVideoUrl = intent.getStringExtra("video_url");
            intentVideoType = intent.getStringExtra("video_type");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(!isCancelled) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("category_name", intentCategoryName);
                    intent.putExtra("vid", intentVid);
                    intent.putExtra("video_id", intentVideoId);
                    intent.putExtra("video_thumbnail", intentVideoThumbnail);
                    intent.putExtra("video_title", intentVideoTitle);
                    intent.putExtra("video_url", intentVideoUrl);
                    intent.putExtra("video_type", intentVideoType);
                    startActivity(intent);
                    finish();
                }

            }
        }, Config.SPLASH_TIME);

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
            setFeedTotalPage(result);
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

    public void setFeedTotalPage(String tmpMainJson){

        if (tmpMainJson == null){
            Log.e("umutfeed", "ActivitySplash setMainCatImages: json data is null");
        }else{

            if (tmpMainJson.length() <= 0){
                Log.e("umutfeed", "ActivitySplash setMainCatImages: json data size is 0");
            }else{
                try {
                    int iFeedTotalPage = Integer.parseInt(tmpMainJson);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt(getString(R.string.feed_post_total_pages), iFeedTotalPage);
                    editor.commit();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

    }

}
