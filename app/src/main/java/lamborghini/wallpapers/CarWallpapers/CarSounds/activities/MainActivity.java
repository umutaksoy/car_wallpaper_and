package lamborghini.wallpapers.CarWallpapers.CarSounds.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.annotation.NonNull;

import lamborghini.wallpapers.CarWallpapers.CarSounds.tab.FragmentTabWallpaper;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import lamborghini.wallpapers.CarWallpapers.CarSounds.BuildConfig;
import lamborghini.wallpapers.CarWallpapers.CarSounds.Config;
import lamborghini.wallpapers.CarWallpapers.CarSounds.R;

import lamborghini.wallpapers.CarWallpapers.CarSounds.fcm.NotificationUtils;
import lamborghini.wallpapers.CarWallpapers.CarSounds.fragments.FragmentAbout;
import lamborghini.wallpapers.CarWallpapers.CarSounds.models.User;
import lamborghini.wallpapers.CarWallpapers.CarSounds.tab.FragmentTabCategory;
import lamborghini.wallpapers.CarWallpapers.CarSounds.tab.FragmentTabFavorite;
import lamborghini.wallpapers.CarWallpapers.CarSounds.tab.FragmentTabRecent;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.Constant;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.GDPR;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.HttpTask;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.Tools;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final static String COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar";
    private final static String SELECTED_TAG = "selected_index";
    private static int selectedIndex;
    private final static int COLLAPSING_TOOLBAR = 0;
    ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    BroadcastReceiver broadcastReceiver;
    NavigationView navigationView;
    private long exitTime = 0;
    private AdView adView;
    private InterstitialAd interstitialAd;
    View view;
    User user;
    String androidId;
    SharedPreferences preferences;
    String intentCategoryName = "";
    String intentVid = "";
    String intentVideoId = "";
    String intentVideoThumbnail = "";
    String intentVideoTitle = "";
    String intentVideoUrl = "";
    String intentVideoType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(android.R.id.content);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        GDPR.updateConsentStatus(this);

        loadInterstitialAd();

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.drawer_layout);

        if (savedInstanceState != null) {
            navigationView.getMenu().getItem(savedInstanceState.getInt(SELECTED_TAG)).setChecked(true);
            return;
        }

        handleNotification();

        selectedIndex = COLLAPSING_TOOLBAR;
        Bundle bundle = new Bundle();
        bundle.putString("category_name", intentCategoryName);
        bundle.putString("vid", intentVid);
        bundle.putString("video_id", intentVideoId);
        bundle.putString("video_thumbnail", intentVideoThumbnail);
        bundle.putString("video_title", intentVideoTitle);
        bundle.putString("video_url", intentVideoUrl);
        bundle.putString("video_type", intentVideoType);

        Fragment fragmentTabRecent = new FragmentTabRecent();
        fragmentTabRecent.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragmentTabRecent, COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                .commit();

        loadBannerAd();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG, selectedIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.drawer_recent:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentTabRecent(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                            .commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_category:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentTabCategory(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                            .commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_cat_wallpapers:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentTabWallpaper(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                            .commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_favorite:
                menuItem.setChecked(true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FragmentTabFavorite(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                        .commit();

                drawerLayout.closeDrawer(GravityCompat.START);
                showInterstitialAd();
                return true;

            case R.id.drawer_rate:
                final String appName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                }

                return true;

            case R.id.drawer_more:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));

                return true;

            case R.id.drawer_share:
                String app_name = android.text.Html.fromHtml(getResources().getString(R.string.app_name)).toString();
                String share_text = android.text.Html.fromHtml(getResources().getString(R.string.share_text)).toString();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, app_name + "\n\n" + share_text + "\n\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                return true;

            case R.id.drawer_about:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new FragmentAbout(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

        }
        return false;
    }

    public void setupNavigationDrawer(Toolbar toolbar) {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void onFailRequest() {
        if (Tools.isConnect(this)) {
            sendRegistrationIdToBackend();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.failed_text), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRegistrationIdToBackend() {
        Log.d("FCM_TOKEN", "Send data to server...");

        String token = preferences.getString("fcm_token", null);
        String appVersion = BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")";
        String osVersion = currentVersion() + " " + Build.VERSION.RELEASE;
        String model = android.os.Build.MODEL;
        String manufacturer = android.os.Build.MANUFACTURER;

        if (token != null) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("user_android_token", token));
            nameValuePairs.add(new BasicNameValuePair("user_unique_id", androidId));
            nameValuePairs.add(new BasicNameValuePair("user_app_version", appVersion));
            nameValuePairs.add(new BasicNameValuePair("user_os_version", osVersion));
            nameValuePairs.add(new BasicNameValuePair("user_device_model", model));
            nameValuePairs.add(new BasicNameValuePair("user_device_manufacturer", manufacturer));
            new HttpTask(null, MainActivity.this, Config.ADMIN_PANEL_URL + "/token-register.php", nameValuePairs, false).execute();
            Log.d("FCM_TOKEN_VALUE", token + " " + androidId);
        }

    }

    private void updateRegistrationIdToBackend() {
        Log.d("FCM_TOKEN", "Update data to server...");
        String token = preferences.getString("fcm_token", null);
        if (token != null) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("user_android_token", token));
            nameValuePairs.add(new BasicNameValuePair("user_unique_id", androidId));
            new HttpTask(null, MainActivity.this, Config.ADMIN_PANEL_URL + "/token-update.php", nameValuePairs, false).execute();
            Log.d("FCM_TOKEN_VALUE", token + " " + androidId);
        }

    }

    public static String currentVersion() {
        double release = Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)", "$1"));
        String codeName = "Unsupported";
        if (release >= 4.1 && release < 4.4) codeName = "Jelly Bean";
        else if (release < 5) codeName = "Kit Kat";
        else if (release < 6) codeName = "Lollipop";
        else if (release < 7) codeName = "Marshmallow";
        else if (release < 8) codeName = "Nougat";
        else if (release < 9) codeName = "Oreo";
        return codeName;
    }

    public void handleNotification() {
        Intent intent = getIntent();

        if(getIntent().hasExtra("vid")) {
            intentCategoryName = intent.getStringExtra("category_name");
            intentVid = intent.getStringExtra("vid");
            intentVideoId = intent.getStringExtra("video_id");
            intentVideoThumbnail = intent.getStringExtra("video_thumbnail");
            intentVideoTitle = intent.getStringExtra("video_title");
            intentVideoUrl = intent.getStringExtra("video_url");
            intentVideoType = intent.getStringExtra("video_type");
        }

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            exitApp();
        }
    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));
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

    private void loadInterstitialAd() {

        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));
            interstitialAd = new InterstitialAd(getApplicationContext());
            interstitialAd.setAdUnitId(getResources().getString(R.string.new_admob_interstitial_unit_id));
            interstitialAd.loadAd(Tools.getAdRequest(this));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(Tools.getAdRequest(MainActivity.this));
                }
            });
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
            }else{
                Log.e("umutadmob", "MainActivity showInterstitialAd: interstitial ad not shown");
            }
        }
    }

}
