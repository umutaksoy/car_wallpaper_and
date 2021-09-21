package lamborghini.wallpapers.CarWallpapers.CarSounds.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.Constant;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import lamborghini.wallpapers.CarWallpapers.CarSounds.Config;
import lamborghini.wallpapers.CarWallpapers.CarSounds.R;
import lamborghini.wallpapers.CarWallpapers.CarSounds.databases.DatabaseHandlerFavorite;
import lamborghini.wallpapers.CarWallpapers.CarSounds.fcm.NotificationUtils;
import lamborghini.wallpapers.CarWallpapers.CarSounds.models.Video;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.Tools;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

public class ActivityDetailVideo extends AppCompatActivity {

    String str_category, str_vid, str_title, str_url, str_video_id, str_thumbnail, str_duration, str_description, str_type, str_date_time;
    String str_cid;
    long long_total_views;
    CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    ImageView video_thumbnail;
    TextView txt_title, txt_category, txt_duration, txt_total_views, txt_date_time;
    WebView video_description;
    Snackbar snackbar;
    private AdView adView;
    ImageButton image_favorite;
    DatabaseHandlerFavorite databaseHandler;
    BroadcastReceiver broadcastReceiver;
    int position;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        view = findViewById(android.R.id.content);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        initAds();
        loadBannerAd();

        databaseHandler = new DatabaseHandlerFavorite(getApplicationContext());

        Intent intent = getIntent();
        if (null != intent) {
            position = intent.getIntExtra(Constant.POSITION, 0);
            str_cid = intent.getStringExtra(Constant.KEY_VIDEO_CATEGORY_ID);
            str_category = intent.getStringExtra(Constant.KEY_VIDEO_CATEGORY_NAME);
            str_vid = intent.getStringExtra(Constant.KEY_VID);
            str_title = intent.getStringExtra(Constant.KEY_VIDEO_TITLE);
            str_url = intent.getStringExtra(Constant.KEY_VIDEO_URL);
            str_video_id = intent.getStringExtra(Constant.KEY_VIDEO_ID);
            str_thumbnail = intent.getStringExtra(Constant.KEY_VIDEO_THUMBNAIL);
            str_duration = intent.getStringExtra(Constant.KEY_VIDEO_DURATION);
            str_description = intent.getStringExtra(Constant.KEY_VIDEO_DESCRIPTION);
            str_type = intent.getStringExtra(Constant.KEY_VIDEO_TYPE);
            long_total_views = intent.getLongExtra(Constant.KEY_TOTAL_VIEWS, 0);
            str_date_time = intent.getStringExtra(Constant.KEY_DATE_TIME);
        }

        setupToolbar();

        video_thumbnail = findViewById(R.id.video_thumbnail);
        txt_title = findViewById(R.id.video_title);
        txt_category = findViewById(R.id.category_name);
        txt_duration = findViewById(R.id.video_duration);
        video_description = findViewById(R.id.video_description);
        txt_total_views = findViewById(R.id.total_views);
        txt_date_time = findViewById(R.id.date_time);
        image_favorite = findViewById(R.id.img_favorite);

        if (Config.ENABLE_RTL_MODE) {
            rtlLayout();
        } else {
            normalLayout();
        }

        addFavorite();

        onReceiveNotification();
        handleNotification();
    }

    private void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }

        appBarLayout = findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("");

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(str_category);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });

    }

    public void normalLayout() {

        txt_title.setText(str_title);
        txt_category.setText(str_category);
        txt_duration.setText(str_duration);

        if (Config.ENABLE_VIEW_COUNT) {
            txt_total_views.setText(Tools.withSuffix(long_total_views) + " " + getResources().getString(R.string.views_count));
        } else {
            txt_total_views.setVisibility(View.GONE);
        }

        if (Config.ENABLE_DATE_DISPLAY && Config.DISPLAY_DATE_AS_TIME_AGO) {
            PrettyTime prettyTime = new PrettyTime();
            long timeAgo = Tools.timeStringtoMilis(str_date_time);
            txt_date_time.setText(prettyTime.format(new Date(timeAgo)));
        } else if (Config.ENABLE_DATE_DISPLAY && !Config.DISPLAY_DATE_AS_TIME_AGO) {
            txt_date_time.setText(Tools.getFormatedDateSimple(str_date_time));
        } else {
            txt_date_time.setVisibility(View.GONE);
        }


        if (str_type != null && str_type.equals("youtube")) {
            Picasso.with(this)
                    .load(Constant.YOUTUBE_IMAGE_FRONT + str_video_id + Constant.YOUTUBE_IMAGE_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(video_thumbnail);
        } else {
            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + str_thumbnail)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(video_thumbnail);
        }

        txt_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityDetailVideo.this, ActivityRelatedCategory.class);
                intent.putExtra(Constant.KEY_VIDEO_CATEGORY_ID, str_cid);
                intent.putExtra(Constant.KEY_VIDEO_CATEGORY_NAME, str_category);
                startActivity(intent);
            }
        });

        video_thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Tools.isNetworkAvailable(ActivityDetailVideo.this)) {

                    if (str_type != null && str_type.equals("youtube")) {
                        Intent intent = new Intent(ActivityDetailVideo.this, ActivityYoutubePlayer.class);
                        intent.putExtra(Constant.KEY_VIDEO_ID, str_video_id);
                        startActivity(intent);
                    }

                    loadViewed();

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_required), Toast.LENGTH_SHORT).show();
                }

            }
        });

        video_description.setBackgroundColor(Color.TRANSPARENT);
        video_description.setFocusableInTouchMode(false);
        video_description.setFocusable(false);
        video_description.getSettings().setDefaultTextEncodingName("UTF-8");

        WebSettings webSettings = video_description.getSettings();
        Resources res = getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = str_description;

        String text = "<html><head>"
                + "<style type=\"text/css\">body{color: #525252;}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        video_description.loadDataWithBaseURL(null, text, mimeType, encoding, null);
    }

    public void rtlLayout() {

        txt_title.setText(str_title);
        txt_category.setText(str_category);
        txt_duration.setText(str_duration);

        if (Config.ENABLE_VIEW_COUNT) {
            txt_total_views.setText(Tools.withSuffix(long_total_views) + " " + getResources().getString(R.string.views_count));
        } else {
            txt_total_views.setVisibility(View.GONE);
        }

        if (Config.ENABLE_DATE_DISPLAY && Config.DISPLAY_DATE_AS_TIME_AGO) {
            PrettyTime prettyTime = new PrettyTime();
            long timeAgo = Tools.timeStringtoMilis(str_date_time);
            txt_date_time.setText(prettyTime.format(new Date(timeAgo)));
        } else if (Config.ENABLE_DATE_DISPLAY && !Config.DISPLAY_DATE_AS_TIME_AGO) {
            txt_date_time.setText(Tools.getFormatedDateSimple(str_date_time));
        } else {
            txt_date_time.setVisibility(View.GONE);
        }


        if (str_type != null && str_type.equals("youtube")) {
            Picasso.with(this)
                    .load(Constant.YOUTUBE_IMAGE_FRONT + str_video_id + Constant.YOUTUBE_IMAGE_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(video_thumbnail);
        } else {
            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + str_thumbnail)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(video_thumbnail);
        }

        txt_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityDetailVideo.this, ActivityRelatedCategory.class);
                intent.putExtra(Constant.KEY_VIDEO_CATEGORY_ID, str_cid);
                intent.putExtra(Constant.KEY_VIDEO_CATEGORY_NAME, str_category);
                startActivity(intent);
            }
        });

        video_thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Tools.isNetworkAvailable(ActivityDetailVideo.this)) {

                    if (str_type != null && str_type.equals("youtube")) {
                        Intent intent = new Intent(ActivityDetailVideo.this, ActivityYoutubePlayer.class);
                        intent.putExtra(Constant.KEY_VIDEO_ID, str_video_id);
                        startActivity(intent);
                    }

                    loadViewed();

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_required), Toast.LENGTH_SHORT).show();
                }

            }
        });

        video_description.setBackgroundColor(Color.TRANSPARENT);
        video_description.setFocusableInTouchMode(false);
        video_description.setFocusable(false);
        video_description.getSettings().setDefaultTextEncodingName("UTF-8");

        WebSettings webSettings = video_description.getSettings();
        Resources res = getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = str_description;

        String text = "<html dir='rtl'><head>"
                + "<style type=\"text/css\">body{color: #525252;}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        video_description.loadDataWithBaseURL(null, text, mimeType, encoding, null);
    }

    private void loadViewed() {
        new MyTask().execute(Config.ADMIN_PANEL_URL + "/api/get_total_views/?id=" + str_vid);
    }

    private static class MyTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return Tools.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null == result || result.length() == 0) {
                Log.d("TAG", "no data found!");
            } else {

                try {

                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray("result");
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.share:
                String share_title = android.text.Html.fromHtml(str_title).toString();
                String share_content = android.text.Html.fromHtml(getResources().getString(R.string.share_text)).toString();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void addFavorite() {

        List<Video> data = databaseHandler.getFavRow(str_vid);
        if (data.size() == 0) {
            image_favorite.setImageResource(R.drawable.ic_fav_outline);
        } else {
            if (data.get(0).getVid().equals(str_vid)) {
                image_favorite.setImageResource(R.drawable.ic_fav);
            }
        }

        image_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Video> data = databaseHandler.getFavRow(str_vid);
                if (data.size() == 0) {
                    databaseHandler.AddtoFavorite(new Video(
                            str_category,
                            str_vid,
                            str_title,
                            str_url,
                            str_video_id,
                            str_thumbnail,
                            str_type
                    ));
                    snackbar = Snackbar.make(view, getResources().getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
                    snackbar.show();

                    image_favorite.setImageResource(R.drawable.ic_fav);

                } else {
                    if (data.get(0).getVid().equals(str_vid)) {
                        databaseHandler.RemoveFav(new Video(str_vid));
                        snackbar = Snackbar.make(view, getResources().getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        image_favorite.setImageResource(R.drawable.ic_fav_outline);
                    }
                }
            }
        });

    }

    public void handleNotification() {
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String url = intent.getStringExtra("link");
        if (id != null) {
            if (id.equals("0")) {
                if (!url.equals("")) {
                    Intent e = new Intent(Intent.ACTION_VIEW);
                    e.setData(Uri.parse(url));
                    startActivity(e);
                }
                Log.d("FCM_INFO", " id : " + id);
            } else {
                Intent action = new Intent(ActivityDetailVideo.this, ActivityNotificationDetail.class);
                action.putExtra("id", id);
                startActivity(action);
                Log.d("FCM_INFO", "id : " + id);
            }
        }
    }

    public void onReceiveNotification() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals(Constant.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    final String id = intent.getStringExtra("id");
                    final String title = intent.getStringExtra("title");
                    final String message = intent.getStringExtra("message");
                    final String image_url = intent.getStringExtra("image_url");
                    final String url = intent.getStringExtra("link");

                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ActivityDetailVideo.this);
                    View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog, null);

                    final AlertDialog.Builder alert = new AlertDialog.Builder(ActivityDetailVideo.this);
                    alert.setView(mView);

                    final TextView notification_title = mView.findViewById(R.id.title);
                    final TextView notification_message = mView.findViewById(R.id.message);
                    final ImageView notification_image = mView.findViewById(R.id.big_image);

                    if (id != null) {
                        if (id.equals("0")) {
                            if (!url.equals("")) {
                                notification_title.setText(title);
                                notification_message.setText(Html.fromHtml(message));
                                Picasso.with(ActivityDetailVideo.this)
                                        .load(image_url.replace(" ", "%20"))
                                        .placeholder(R.drawable.ic_thumbnail)
                                        .into(notification_image);
                                alert.setPositiveButton("Open link", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(url));
                                        startActivity(intent);

                                    }
                                });
                                alert.setNegativeButton(getResources().getString(R.string.dialog_dismiss), null);
                            } else {
                                notification_title.setText(title);
                                notification_message.setText(Html.fromHtml(message));
                                Picasso.with(ActivityDetailVideo.this)
                                        .load(image_url.replace(" ", "%20"))
                                        .placeholder(R.drawable.ic_thumbnail)
                                        .into(notification_image);
                                alert.setPositiveButton(getResources().getString(R.string.dialog_ok), null);
                            }
                        } else {
                            notification_title.setText(title);
                            notification_message.setText(Html.fromHtml(message));
                            Picasso.with(ActivityDetailVideo.this)
                                    .load(image_url.replace(" ", "%20"))
                                    .placeholder(R.drawable.ic_thumbnail)
                                    .into(notification_image);

                            alert.setPositiveButton(getResources().getString(R.string.dialog_read_more), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getApplicationContext(), ActivityNotificationDetail.class);
                                    intent.putExtra("id", id);
                                    startActivity(intent);
                                }
                            });
                            alert.setNegativeButton(getResources().getString(R.string.dialog_dismiss), null);
                        }
                        alert.setCancelable(false);
                        alert.show();
                    }

                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Constant.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Constant.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    public void initAds() {
        if (Config.ENABLE_ADMOB_BANNER_ADS || Config.ENABLE_ADMOB_INTERSTITIAL_ADS_ON_CLICK_VIDEO) {
            MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));
        }
    }

    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            adView = findViewById(R.id.adView);
            adView.loadAd(Tools.getAdRequest(this));
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdFailedToLoad(int error) {
                    adView.setVisibility(View.GONE);
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }
            });

        }
    }
}
