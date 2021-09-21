package lamborghini.wallpapers.CarWallpapers.CarSounds.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import lamborghini.wallpapers.CarWallpapers.CarSounds.Config;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.AnalyticLogs;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.Tools;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import lamborghini.wallpapers.CarWallpapers.CarSounds.R;
import lamborghini.wallpapers.CarWallpapers.CarSounds.databases.DatabaseHandlerFavorite;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ActivityWallpaperDetail extends AppCompatActivity {
    ImageView img_news, img_fav, img_next, img_prev, img_download, img_setwp;
    DatabaseHandlerFavorite db;
    final Context context = this;
    CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    ProgressBar progressBar;
    static final String TAG = "ActDetail";
    CoordinatorLayout coordinatorLayout;
    private AdView adView;
    private InterstitialAd interstitialAd;
    public String tmpImage;

    private static final int WRITE_EXTERNAL_STORAGE = 0;
    String wallpapername;
    Activity act;
    int tmpSize;
    int tmpPos;
    int tmpIsFav;
    long tmpAdTime, cntAdTime;
    String tmpAllImages;
    String tmpAllCatId;
    String tmpCategoryName;
    String[] tmpImageList;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wallpaper_details);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Intent tmpIntent = getIntent();

        try {
            tmpImage = tmpIntent.getStringExtra("IMAGE");
        }catch (Exception e){
            tmpImage = "";
        }

        try {
            tmpSize = tmpIntent.getIntExtra("SIZE", 0);
        }catch (Exception e){
            tmpSize = 0;
        }

        try {
            tmpPos = tmpIntent.getIntExtra("POSITION", 0);
        }catch (Exception e){
            tmpPos = 0;
        }

        try {
            if (tmpIntent.getStringExtra("FROMFAV").equals("Y")){
                tmpIsFav = 1;
            }else {
                tmpIsFav = 0;
            }
        }catch (Exception e){
            tmpIsFav = 0;
        }

        tmpAllImages = "";

        try {
            tmpAllImages = tmpIntent.getStringExtra("ALLIMAGES");
        }catch (Exception e){
            tmpAllImages = "";
        }

        try {
            tmpAllCatId = tmpIntent.getStringExtra("ALLCATID");
        }catch (Exception e){
            tmpAllCatId = "";
        }

        try {
            tmpCategoryName = tmpIntent.getStringExtra("CATEGORYNAME");
        }catch (Exception e){
            tmpCategoryName = "";
        }


        if (tmpAllImages.length() > 0) {
            tmpImageList = tmpAllImages.split("XÜÜÜX");

            if (tmpImage.length() > 0){

                for (int i = 0; i < tmpImageList.length; i++) {
                    if (tmpImageList[i].equals(tmpImage)){
                        tmpPos = i;
                    }

                }

            }

        }

        try {
            tmpAdTime = tmpIntent.getLongExtra("ADTIME", System.currentTimeMillis());
        }catch (Exception e){
            tmpAdTime = System.currentTimeMillis();
        }

        cntAdTime = 45000;

        wallpapername = "BestWallpapers";

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        } else {
            Log.d("RTL Mode", "Working in Normal Mode, RTL Mode is Disabled");
        }

        loadBannerAd();

        loadInterstitialAd();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("");

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                collapsingToolbarLayout.setTitle("");
                isShow = false;

            }
        });

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        img_news = findViewById(R.id.image);
        img_fav = (FloatingActionButton) findViewById(R.id.img_fav);
        img_download = (FloatingActionButton) findViewById(R.id.img_download);
        img_setwp = (FloatingActionButton) findViewById(R.id.img_setwp);
        img_next = (FloatingActionButton) findViewById(R.id.img_next);
        img_prev = (FloatingActionButton) findViewById(R.id.img_prev);

        if (tmpPos <= 0){
            img_prev.setEnabled(false);
            img_prev.setVisibility(View.INVISIBLE);
        }else {
            img_prev.setEnabled(true);
            img_prev.setVisibility(View.VISIBLE);
        }

        if (tmpSize <= 0 || tmpSize <= tmpPos + 1){
            img_next.setEnabled(false);
            img_next.setVisibility(View.INVISIBLE);
        }else {
            img_next.setEnabled(true);
            img_next.setVisibility(View.VISIBLE);
        }

        if (tmpIsFav == 1){
            img_next.setEnabled(false);
            img_next.setVisibility(View.INVISIBLE);
            img_prev.setEnabled(false);
            img_prev.setVisibility(View.INVISIBLE);
        }

        db = new DatabaseHandlerFavorite(ActivityWallpaperDetail.this);

        if (Tools.isNetworkAvailable(ActivityWallpaperDetail.this)) {

            if (tmpAllImages.length() <= 0){
                Log.e("umut izleme", "ActivityWallpaperDetail tmpAllImages size is 0");
            }else {

                progressBar.setVisibility(View.GONE);
                coordinatorLayout.setVisibility(View.VISIBLE);
                setAdapterToRecyclerView();

            }

        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed_connect_network), Toast.LENGTH_SHORT).show();
        }

    }

    public void setAdapterToRecyclerView() {

        if (Config.ENABLE_RTL_MODE) {

            if (tmpImage.length() > 0) {
                Picasso
                        .with(context)
                        .load(tmpImage)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(img_news);
            }

            List<String> itemFavorites = db.getWallpaperFavRow(tmpImage);

            if (itemFavorites.size() == 0) {
                img_fav.setImageResource(R.drawable.ic_favorite_outline_white);
            } else {
                if (itemFavorites.get(0).equals(tmpImage)) {
                    img_fav.setImageResource(R.drawable.ic_favorite_white);
                }
            }

            img_download.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT > 22) {
                        int permissionCheck = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(
                                    act, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
                        } else {
                            saveImageToSDCard(convertImageViewToBitmap(img_news), wallpapername, context, tmpCategoryName, tmpImage);
                        }
                    }else{
                        saveImageToSDCard(convertImageViewToBitmap(img_news), wallpapername, context, tmpCategoryName, tmpImage);
                    }

                }
            });

            img_setwp.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    setAsWallpaper(convertImageViewToBitmap(img_news),tmpCategoryName, tmpImage);
                }
            });

            img_prev.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    goToOtherScreen(tmpPos - 1);
                }
            });

            img_next.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    goToOtherScreen(tmpPos + 1);
                }
            });

            img_fav.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    List<String> itemFavorites = db.getWallpaperFavRow(tmpImage);

                    if (itemFavorites.size() == 0) {

                        LogEventFav(1, tmpCategoryName, tmpImage);
                        LogEventFavNumSegWall();
                        db.AddWallpapertoFavorite(tmpImage);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();
                        img_fav.setImageResource(R.drawable.ic_favorite_white);

                    } else {
                        if (itemFavorites.get(0).equals(tmpImage)) {

                            db.RemoveWallpaperFav(tmpImage);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                            img_fav.setImageResource(R.drawable.ic_favorite_outline_white);
                        }
                    }
                }
            });

        } else {

            if (tmpImage.length() > 0){

                Picasso.with(context)
                        .load(tmpImage)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(img_news);
            }

            List<String> itemFavorites = db.getWallpaperFavRow(tmpImage);
            if (itemFavorites.size() == 0) {
                img_fav.setImageResource(R.drawable.ic_favorite_outline_white);
            } else {

                if (itemFavorites.get(0).equals(tmpImage)) {
                    img_fav.setImageResource(R.drawable.ic_favorite_white);
                }
            }

            img_download.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT > 22) {
                        int permissionCheck = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(
                                    act, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
                        } else {
                            saveImageToSDCard(convertImageViewToBitmap(img_news), wallpapername, context, tmpCategoryName, tmpImage);
                        }
                    }else{
                        saveImageToSDCard(convertImageViewToBitmap(img_news), wallpapername, context, tmpCategoryName, tmpImage);
                    }

                }
            });

            img_setwp.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    setAsWallpaper(convertImageViewToBitmap(img_news),tmpCategoryName, tmpImage);
                }
            });

            img_prev.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    goToOtherScreen(tmpPos - 1);
                }
            });

            img_next.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    goToOtherScreen(tmpPos + 1);

                }
            });


            img_fav.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    List<String> itemFavorites = db.getWallpaperFavRow(tmpImage);
                    if (itemFavorites.size() == 0) {

                        LogEventFav(1, tmpCategoryName, tmpImage);
                        LogEventFavNumSegWall();
                        db.AddWallpapertoFavorite(tmpImage);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();
                        img_fav.setImageResource(R.drawable.ic_favorite_white);

                    } else {
                        if (itemFavorites.get(0).equals(tmpImage)) {

                            db.RemoveWallpaperFav(tmpImage);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                            img_fav.setImageResource(R.drawable.ic_favorite_outline_white);
                        }
                    }
                }
            });

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                break;

            case R.id.menu_share:
                LogEventShare(1, tmpCategoryName, tmpImage);
                LogEventShareNumSegWall();

                String share_title = android.text.Html.fromHtml(tmpCategoryName).toString();
                String share_content = android.text.Html.fromHtml(context.getResources().getString(R.string.share_text)).toString();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + context.getPackageName());
                sendIntent.setType("text/plain");
                context.startActivity(sendIntent);

                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();  // optional depending on your needs
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

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            interstitialAd = new InterstitialAd(getApplicationContext());
            interstitialAd.setAdUnitId(getResources().getString(R.string.new_admob_interstitial_unit_id));
            interstitialAd.loadAd(Tools.getAdRequest(this));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(Tools.getAdRequest(ActivityWallpaperDetail.this));
                }
            });
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {

            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
            }

        }
    }

    public void goToOtherScreen(int nPos){
        int isShowAd;

        isShowAd = 0;
        if ((System.currentTimeMillis() - tmpAdTime) > cntAdTime){
            tmpAdTime = System.currentTimeMillis();
            isShowAd = 1;
        }

        Intent intent = new Intent(context, ActivityWallpaperDetail.class);
        intent.putExtra("IMAGE", tmpImageList[nPos]);
        intent.putExtra("SIZE", tmpSize);
        intent.putExtra("POSITION", nPos);
        intent.putExtra("FROMFAV", "N");
        intent.putExtra("ALLIMAGES", tmpAllImages);
        intent.putExtra("ALLCATID", tmpAllCatId);
        intent.putExtra("CATEGORYNAME", tmpCategoryName);
        intent.putExtra("ADTIME", tmpAdTime);

        context.startActivity(intent);
        finish();

        if (isShowAd == 1){
            showInterstitialAd();
        }
    }

    public void setAsWallpaper(Bitmap bitmap, String strCategory, String strUrl) {
        LogEventSetWp(strCategory, strUrl);
        LogEventSetNumSegWall();

        if (Build.VERSION.SDK_INT > 22) {
            int permissionCheck = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        act, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
            } else {
                String fname = "CarWallpapers-" + Long.toString(System.currentTimeMillis()) + ".jpg";

                String savedImageStatus = insertImage(getContentResolver(), bitmap, fname , fname);

                if (savedImageStatus != null){
                    Uri picUri = Uri.parse(savedImageStatus);
                    Intent setAs = new Intent(Intent.ACTION_ATTACH_DATA);
                    setAs.setDataAndType(picUri, "image/*");
                    //setAs.putExtra(Intent.EXTRA_STREAM,picUri);
                    startActivityForResult(Intent.createChooser(setAs, "Set As"), 0);

                }
            }
        }else{
            String fname = "CarWallpapers-" + Long.toString(System.currentTimeMillis()) + ".jpg";

            String savedImageStatus = insertImage(getContentResolver(), bitmap, fname , fname);

            if (savedImageStatus != null){
                Uri picUri = Uri.parse(savedImageStatus);
                Intent setAs = new Intent(Intent.ACTION_ATTACH_DATA);
                setAs.setDataAndType(picUri, "image/*");
                //setAs.putExtra(Intent.EXTRA_STREAM,picUri);
                startActivityForResult(Intent.createChooser(setAs, "Set As"), 0);

            }
        }

    }

    public void saveImageToSDCard(Bitmap bitmap, String wpname, Context c, String strCategory, String strUrl) {
        LogEventDownload(strCategory, strUrl);
        LogEventDowNumSegWall();

        String fname = "BestWallpapers-" + Long.toString(System.currentTimeMillis()) + ".jpg";

        String savedImageStatus = insertImage(getContentResolver(), bitmap, fname , fname);
        if (savedImageStatus == null){
            Toast.makeText(c,
                    c.getString(R.string.toast_saved_failed),
                    Toast.LENGTH_SHORT).show();

        }else {
            Toast.makeText(c,
                    c.getString(R.string.toast_saved).replace("#",
                            "\"" + savedImageStatus + "\""),
                    Toast.LENGTH_LONG).show();
        };

    }

    String insertImage(ContentResolver cr,
                       Bitmap source,
                       String title,
                       String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 90, imageOut);
                }catch (Exception e){
                    Log.e("umutwallpaper", "ActivityWallpaperDetail image download error : " + e.toString());
                }finally {
                    imageOut.close();
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {

            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND,kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID,(int)id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT,thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH,thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            Log.e("umutwallpaper", "ActivityWallpaperDetail image download error : " + ex.toString());
            return null;
        } catch (IOException ex) {
            Log.e("umutwallpaper", "ActivityWallpaperDetail image download error : " + ex.toString());
            return null;
        }
    }

    //permission dialog for marshmello and above
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case WRITE_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    saveImageToSDCard(convertImageViewToBitmap(img_news), wallpapername, context, tmpCategoryName, tmpImage);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onDestroy() {  // could be in onPause or onStop
        super.onDestroy();
    }

    private Bitmap convertImageViewToBitmap(ImageView v){

        Bitmap bm=((BitmapDrawable)v.getDrawable()).getBitmap();
        return bm;
    }

    public void LogEventFav(int iType, String strCategory, String strUrl){
        Bundle bundle = new Bundle();
        bundle.putInt(context.getString(R.string.fire_param_type), iType);
        bundle.putString(context.getString(R.string.fire_param_category), strCategory);
        bundle.putString(context.getString(R.string.fire_param_url), strUrl);
        mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_fav), bundle);
    }

    public void LogEventShare(int iType, String strCategory, String strUrl){
        Bundle bundle = new Bundle();
        bundle.putInt(context.getString(R.string.fire_param_type), iType);
        bundle.putString(context.getString(R.string.fire_param_category), strCategory);
        bundle.putString(context.getString(R.string.fire_param_url), strUrl);
        mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_share), bundle);
    }

    public void LogEventDownload(String strCategory, String strUrl){
        Bundle bundle = new Bundle();
        bundle.putString(context.getString(R.string.fire_param_category), strCategory);
        bundle.putString(context.getString(R.string.fire_param_url), strUrl);
        mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_download), bundle);
    }

    public void LogEventSetWp(String strCategory, String strUrl){
        Bundle bundle = new Bundle();
        bundle.putString(context.getString(R.string.fire_param_category), strCategory);
        bundle.putString(context.getString(R.string.fire_param_url), strUrl);
        mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_setwp), bundle);
    }

    public void LogEventFavNumSegWall(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int iFavNum = preferences.getInt(context.getString(R.string.fire_event_favnumsegwall), 0);

        int iNextNumSeg = AnalyticLogs.getNumSeg(iFavNum + 1);

        if (iNextNumSeg > AnalyticLogs.getNumSeg(iFavNum)){
            Bundle bundle = new Bundle();
            bundle.putInt(context.getString(R.string.fire_param_numseg), iNextNumSeg);
            mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_favnumsegwall), bundle);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.fire_event_favnumsegwall), (iNextNumSeg + 1));
        editor.commit();

    }

    public void LogEventShareNumSegWall(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int iFavNum = preferences.getInt(context.getString(R.string.fire_event_sharenumsegwall), 0);

        int iNextNumSeg = AnalyticLogs.getNumSeg(iFavNum + 1);

        if (iNextNumSeg > AnalyticLogs.getNumSeg(iFavNum)){
            Bundle bundle = new Bundle();
            bundle.putInt(context.getString(R.string.fire_param_numseg), iNextNumSeg);
            mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_sharenumsegwall), bundle);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.fire_event_sharenumsegwall), (iNextNumSeg + 1));
        editor.commit();

    }

    public void LogEventDowNumSegWall(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int iDownNum = preferences.getInt(context.getString(R.string.fire_event_downumsegwall), 0);

        int iNextNumSeg = AnalyticLogs.getNumSeg(iDownNum + 1);

        if (iNextNumSeg > AnalyticLogs.getNumSeg(iDownNum)){
            Bundle bundle = new Bundle();
            bundle.putInt(context.getString(R.string.fire_param_numseg), iNextNumSeg);
            mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_downumsegwall), bundle);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.fire_event_downumsegwall), (iDownNum + 1));
        editor.commit();

    }

    public void LogEventSetNumSegWall(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int iSetNum = preferences.getInt(context.getString(R.string.fire_event_setnumsegwall), 0);

        int iNextNumSeg = AnalyticLogs.getNumSeg(iSetNum + 1);

        if (iNextNumSeg > AnalyticLogs.getNumSeg(iSetNum)){
            Bundle bundle = new Bundle();
            bundle.putInt(context.getString(R.string.fire_param_numseg), iNextNumSeg);
            mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_setnumsegwall), bundle);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.fire_event_setnumsegwall), (iNextNumSeg + 1));
        editor.commit();

    }

}
