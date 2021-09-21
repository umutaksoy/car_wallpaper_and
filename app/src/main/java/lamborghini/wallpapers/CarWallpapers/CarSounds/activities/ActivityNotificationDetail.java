package lamborghini.wallpapers.CarWallpapers.CarSounds.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
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
import lamborghini.wallpapers.CarWallpapers.CarSounds.callbacks.CallbackVideoDetail;
import lamborghini.wallpapers.CarWallpapers.CarSounds.databases.DatabaseHandlerFavorite;
import lamborghini.wallpapers.CarWallpapers.CarSounds.models.Video;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.AppBarLayoutBehavior;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.Constant;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.Tools;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;

public class ActivityNotificationDetail extends AppCompatActivity {

    String str_vid;
    CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    ImageView video_thumbnail;
    TextView txt_title, txt_category, txt_duration, txt_total_views, txt_date_time;
    WebView video_description;
    Snackbar snackbar;
    private AdView adView;
    ImageButton image_favorite;
    DatabaseHandlerFavorite databaseHandler;
    View parent_view, lyt_progress;
    CoordinatorLayout lyt_content;
    Video post;
    private Call<CallbackVideoDetail> callbackCall = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);
        parent_view = findViewById(android.R.id.content);
        lyt_content = findViewById(R.id.lyt_content);
        lyt_progress = findViewById(R.id.lyt_progress);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        initAds();
        loadBannerAd();

        databaseHandler = new DatabaseHandlerFavorite(getApplicationContext());
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        setupToolbar();

        Intent intent = getIntent();
        str_vid = intent.getStringExtra("id");

        video_thumbnail = findViewById(R.id.video_thumbnail);
        txt_title = findViewById(R.id.video_title);
        txt_category = findViewById(R.id.category_name);
        txt_duration = findViewById(R.id.video_duration);
        video_description = findViewById(R.id.video_description);
        txt_total_views = findViewById(R.id.total_views);
        txt_date_time = findViewById(R.id.date_time);
        image_favorite = findViewById(R.id.img_favorite);

        requestAction();
        addFavorite();

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
                    collapsingToolbarLayout.setTitle(post.category_name);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });

    }

    private void requestAction() {
        showFailedView(false, "");
        requestDetailsPostApi();
    }

    private void requestDetailsPostApi() {
        //TODO:UMUT all api and callbacks will be removed
/*
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getPostDetail(str_vid);
        callbackCall.enqueue(new Callback<CallbackVideoDetail>() {
            @Override
            public void onResponse(Call<CallbackVideoDetail> call, Response<CallbackVideoDetail> response) {
                CallbackVideoDetail resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post = resp.post;
                    displayData();
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackVideoDetail> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
*/

    }

    private void onFailRequest() {
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_content.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
            lyt_progress.setVisibility(View.GONE);
        } else {
            lyt_content.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    lyt_progress.setVisibility(View.GONE);
                }
            }, 1500);
        }
        findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction();
            }
        });
    }

    public void displayData() {
        txt_title.setText(post.video_title);
        txt_category.setText(post.category_name);

        if (Config.ENABLE_VIEW_COUNT) {
            txt_total_views.setText("");
        } else {
            txt_total_views.setVisibility(View.GONE);
        }

        if (post.video_type != null && post.video_type.equals("youtube")) {
            Picasso.with(this)
                    .load(Constant.YOUTUBE_IMAGE_FRONT + post.video_id + Constant.YOUTUBE_IMAGE_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(video_thumbnail);
        } else {
            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.video_thumbnail)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(video_thumbnail);
        }

        txt_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("umut izleme", "ActivityNotificationDetail tctcategory.setonclicklistener calisti");

                Intent intent = new Intent(ActivityNotificationDetail.this, ActivityRelatedCategory.class);
                intent.putExtra(Constant.KEY_VIDEO_CATEGORY_ID, post.cat_id);
                intent.putExtra(Constant.KEY_VIDEO_CATEGORY_NAME, post.category_name);
                startActivity(intent);
            }
        });

        video_thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("umut izleme", "ActivityNotificationDetail videothumbnail.setonclicklistener calisti");

                if (Tools.isNetworkAvailable(ActivityNotificationDetail.this)) {

                    if (post.video_type != null && post.video_type.equals("youtube")) {
                        Intent intent = new Intent(ActivityNotificationDetail.this, ActivityYoutubePlayer.class);
                        intent.putExtra(Constant.KEY_VIDEO_ID, post.video_id);
                        startActivity(intent);
                    }

                    loadViewed();

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_required), Toast.LENGTH_SHORT).show();
                }

            }
        });

        video_description.setBackgroundColor(Color.parseColor("#ffffff"));
        video_description.setFocusableInTouchMode(false);
        video_description.setFocusable(false);
        video_description.getSettings().setDefaultTextEncodingName("UTF-8");

        WebSettings webSettings = video_description.getSettings();
        Resources res = getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);
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
                String share_title = android.text.Html.fromHtml(post.video_title).toString();
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
                            post.category_name,
                            post.vid,
                            post.video_title,
                            post.video_url,
                            post.video_id,
                            post.video_thumbnail,
                            post.video_type
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
