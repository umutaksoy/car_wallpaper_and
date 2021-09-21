package lamborghini.wallpapers.CarWallpapers.CarSounds.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import lamborghini.wallpapers.CarWallpapers.CarSounds.adapters.AdapterWallpaperCategory;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import lamborghini.wallpapers.CarWallpapers.CarSounds.Config;
import lamborghini.wallpapers.CarWallpapers.CarSounds.R;
import lamborghini.wallpapers.CarWallpapers.CarSounds.callbacks.CallbackCategoryDetails;
import lamborghini.wallpapers.CarWallpapers.CarSounds.fcm.NotificationUtils;
import lamborghini.wallpapers.CarWallpapers.CarSounds.models.CategoryWallpaper;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;

public class ActivityWallpaperByCategory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterWallpaperCategory adapterWallpaperCategory;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackCategoryDetails> callbackCall = null;
    private int post_total = 0;
    private int failed_page = 0;
    private CategoryWallpaper categoryWallpaper;
    private AdView adView;
    View view;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category_details);
        view = findViewById(android.R.id.content);

        AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        } else {
            Log.d("Log", "Working in Normal Mode, RTL Mode is Disabled");
        }

        initAds();
        loadBannerAd();

        categoryWallpaper = (CategoryWallpaper) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorOrange, R.color.colorGreen, R.color.colorBlue, R.color.colorRed);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, Config.NUM_OF_COLUMNS));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        adapterWallpaperCategory = new AdapterWallpaperCategory(this, recyclerView, new ArrayList<Video>(), 1);
        recyclerView.setAdapter(adapterWallpaperCategory);

        // on item list clicked
        adapterWallpaperCategory.setOnItemClickListener(new AdapterWallpaperCategory.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Video obj, int position) {
                Intent intent = new Intent(getApplicationContext(), ActivityWallpaperDetail.class);
                intent.putExtra(Constant.POSITION, position);
                intent.putExtra(Constant.KEY_VIDEO_CATEGORY_ID, obj.cat_id);
                intent.putExtra(Constant.KEY_VIDEO_CATEGORY_NAME, obj.category_name);
                intent.putExtra(Constant.KEY_VID, obj.vid);
                intent.putExtra(Constant.KEY_VIDEO_TITLE, obj.video_title);
                intent.putExtra(Constant.KEY_VIDEO_URL, obj.video_url);
                intent.putExtra(Constant.KEY_VIDEO_ID, obj.video_id);
                intent.putExtra(Constant.KEY_VIDEO_THUMBNAIL, obj.video_thumbnail);
                intent.putExtra(Constant.KEY_VIDEO_TYPE, obj.video_type);
                startActivity(intent);
            }
        });

        // detect when scroll reach bottom
        adapterWallpaperCategory.setOnLoadMoreListener(new AdapterWallpaperCategory.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (post_total > adapterWallpaperCategory.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    adapterWallpaperCategory.setLoaded();
                }
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (callbackCall != null && callbackCall.isExecuted()) {
                    callbackCall.cancel();
                }
                adapterWallpaperCategory.resetListData();
                requestAction(1);
            }
        });

        requestAction(1);

        setupToolbar();

        onReceiveNotification();
        handleNotification();

    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(categoryWallpaper.category_name);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void displayApiResult(final List<Video> videos) {
        adapterWallpaperCategory.insertData(videos);
        swipeProgress(false);
        if (videos.size() == 0) {
            showNoItemView(true);
        }
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        adapterWallpaperCategory.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(getApplicationContext())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterWallpaperCategory.setLoading();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new MyTask().execute("https://storage.googleapis.com/mythical-ace-4987/yt-project/wallpaper-category-detail" + categoryWallpaper.cid + ".json");
            }
        }, Constant.DELAY_TIME);
    }

    private void showFailedView(boolean show, String message) {
        View view = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(failed_page);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View view = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
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
                Intent action = new Intent(ActivityWallpaperByCategory.this, ActivityNotificationDetail.class);
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

                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ActivityWallpaperByCategory.this);
                    View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog, null);

                    final AlertDialog.Builder alert = new AlertDialog.Builder(ActivityWallpaperByCategory.this);
                    alert.setView(mView);

                    final TextView notification_title = mView.findViewById(R.id.title);
                    final TextView notification_message = mView.findViewById(R.id.message);
                    final ImageView notification_image = mView.findViewById(R.id.big_image);

                    if (id != null) {
                        if (id.equals("0")) {
                            if (!url.equals("")) {
                                notification_title.setText(title);
                                notification_message.setText(Html.fromHtml(message));
                                Picasso.with(ActivityWallpaperByCategory.this)
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
                                Picasso.with(ActivityWallpaperByCategory.this)
                                        .load(image_url.replace(" ", "%20"))
                                        .placeholder(R.drawable.ic_thumbnail)
                                        .into(notification_image);
                                alert.setPositiveButton(getResources().getString(R.string.dialog_ok), null);
                            }
                        } else {
                            notification_title.setText(title);
                            notification_message.setText(Html.fromHtml(message));
                            Picasso.with(ActivityWallpaperByCategory.this)
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
        if (Config.ENABLE_ADMOB_BANNER_ADS || Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
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

    private class MyTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
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
        swipeRefreshLayout.setRefreshing(false);

        if (tmpMainJson == null){
            onFailRequest(1);
        }else{

            if (tmpMainJson.length() <= 0){
                onFailRequest(1);
            }else{
                List<Video> videoCategoryList = new ArrayList<>();

                try {
                    JSONObject mainJson = new JSONObject(tmpMainJson);
                    JSONArray jsonArray = mainJson.getJSONArray("posts");
                    JSONObject objJson = null;

                    for (int i = 0; i < jsonArray.length(); i++) {

                        objJson = jsonArray.getJSONObject(i);
                        Video tmpVideoCategory = new Video();
                        tmpVideoCategory.setVid(objJson.getString("vid"));
                        tmpVideoCategory.setId(objJson.getInt("cat_id"));
                        tmpVideoCategory.setVideo_title(objJson.getString("video_title"));
                        tmpVideoCategory.setVideo_url(objJson.getString("video_url"));
                        tmpVideoCategory.setVideo_id(objJson.getString("video_id"));
                        tmpVideoCategory.setVideo_thumbnail(objJson.getString("video_thumbnail"));
                        tmpVideoCategory.setCategory_name(objJson.getString("category_name"));
                        tmpVideoCategory.setVideo_type(objJson.getString("video_type"));
                        videoCategoryList.add(tmpVideoCategory);
                    }

                    Collections.shuffle(videoCategoryList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                displayApiResult(videoCategoryList);

            }

        }

    }

}
