package lamborghini.wallpapers.CarWallpapers.CarSounds.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.Constant;

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import lamborghini.wallpapers.CarWallpapers.CarSounds.R;
import lamborghini.wallpapers.CarWallpapers.CarSounds.adapters.AdapterRecent;
import lamborghini.wallpapers.CarWallpapers.CarSounds.models.Video;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.EqualSpacingItemDecoration;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.Tools;

import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static lamborghini.wallpapers.CarWallpapers.CarSounds.utils.AnalyticLogs.getNumSeg;

public class FragmentRecent extends Fragment {

    View root_view, parent_view;
    private RecyclerView recyclerView;
    private AdapterRecent adapterRecent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int post_total = 0;
    private int failed_page = 0;
    Context cont;
    Activity act;
    String strWallpaperUrl = "";
    String strWallpaperCategory = "";
    private static final int WRITE_EXTERNAL_STORAGE = 0;
    boolean bJobIdAfterPermission = false;
    private List<Integer> listTotalPages = new ArrayList<>();
    private int currentPage = 0;
    String intentCategoryName;
    String intentVid;
    String intentVideoId;
    String intentVideoThumbnail;
    String intentVideoTitle;
    String intentVideoUrl;
    String intentVideoType;
    private FirebaseAnalytics mFirebaseAnalytics;

    public FragmentRecent(String intentCategoryName, String intentVid, String intentVideoId, String intentVideoThumbnail, String intentVideoTitle, String intentVideoUrl, String intentVideoType){
        this.intentCategoryName = intentCategoryName;
        this.intentVid = intentVid;
        this.intentVideoId = intentVideoId;
        this.intentVideoThumbnail = intentVideoThumbnail;
        this.intentVideoTitle = intentVideoTitle;
        this.intentVideoUrl = intentVideoUrl;
        this.intentVideoType = intentVideoType;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_recent, null);
        parent_view = getActivity().findViewById(R.id.lyt_content);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        cont = getActivity();
        act = getActivity();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cont);
        int iTmpTotalPageCount = preferences.getInt(getString(R.string.feed_post_total_pages), 40);

        for (int i= 0; i < iTmpTotalPageCount; i++){
            listTotalPages.add(i);
        }

        Collections.shuffle(listTotalPages);

        setHasOptionsMenu(true);

        swipeRefreshLayout = root_view.findViewById(R.id.swipe_refresh_layout_home);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorOrange, R.color.colorGreen, R.color.colorBlue, R.color.colorRed);

        recyclerView = root_view.findViewById(R.id.recyclerView);

        recyclerView.addItemDecoration(new EqualSpacingItemDecoration(0));
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        adapterRecent = new AdapterRecent(getActivity(), recyclerView, new ArrayList<Video>(), 0);
        recyclerView.setAdapter(adapterRecent);

        // on item list clicked
        adapterRecent.setOnItemClickListener(new AdapterRecent.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Video obj, int position) {

                switch (v.getId()){
                    case R.id.feed_setwp:
                        bJobIdAfterPermission = true;
                        strWallpaperUrl = obj.video_url;
                        strWallpaperCategory = obj.category_name;
                        wpSetOrDownload(strWallpaperCategory, strWallpaperUrl, cont, bJobIdAfterPermission);
                        break;
                    case R.id.feed_download:
                        bJobIdAfterPermission = false;
                        strWallpaperUrl = obj.video_url;
                        strWallpaperCategory = obj.category_name;
                        wpSetOrDownload(strWallpaperCategory, strWallpaperUrl, cont, bJobIdAfterPermission);
                        break;
                    default:
                }

            }
        });

        // detect when scroll reach bottom
        adapterRecent.setOnLoadMoreListener(new AdapterRecent.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int notUsed) {

                if (currentPage != 0 && currentPage < listTotalPages.size()) {
                    currentPage = currentPage + 1;
                    requestAction(currentPage);
                } else {
                    adapterRecent.setLoaded();
                }

            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapterRecent.resetListData();
                requestAction(1);
            }
        });

        requestAction(1);

        return root_view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    private void displayApiResult(final List<Video> videos) {
        adapterRecent.insertData(videos);
        swipeProgress(false);
        if (videos.size() == 0) {
            showNoItemView(true);
        }
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        adapterRecent.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(getActivity())) {
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
            adapterRecent.setLoading();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                currentPage = page_no;
                new MyTask().execute("https://storage.googleapis.com/mythical-ace-4987/yt-project/newposts" + listTotalPages.get(page_no - 1) + ".json");
            }
        }, Constant.DELAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
    }

    //permission dialog for marshmello and above
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case WRITE_EXTERNAL_STORAGE:

                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    wpSetOrDownload(strWallpaperCategory, strWallpaperUrl, cont, bJobIdAfterPermission);
                }else{
                    setFeedBtnDefaults(bJobIdAfterPermission);
                }

                break;

            default:
                break;
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed_home);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        root_view.findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(failed_page);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_home);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
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

    public interface AsyncResponse {
        void processFinish(Bitmap output);
    }

    public class MyTaskWPDownload extends AsyncTask<String, Void, Bitmap> {

        public AsyncResponse delegate = null;

        public MyTaskWPDownload(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return getBitmapFromUrl(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            delegate.processFinish(result);
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
                List<Video> posts = new ArrayList<>();
                List<Video> newposts = new ArrayList<>();

                try {
                    JSONObject mainJson = new JSONObject(tmpMainJson);
                    JSONArray jsonArray = mainJson.getJSONArray("posts");
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {

                        objJson = jsonArray.getJSONObject(i);
                        Video tmpVideo = new Video();
                        tmpVideo.setCategory_name(objJson.getString("category_name"));
                        tmpVideo.setVid(objJson.getString("vid"));
                        tmpVideo.setVideo_id(objJson.getString("video_id"));
                        tmpVideo.setVideo_thumbnail(objJson.getString("video_thumbnail"));
                        tmpVideo.setVideo_title(objJson.getString("video_title"));
                        tmpVideo.setVideo_url(objJson.getString("video_url"));
                        tmpVideo.setVideo_type(objJson.getString("video_type"));
                        posts.add(tmpVideo);
                    }

                    Collections.shuffle(posts);

                    if (!intentCategoryName.equals("")){
                        Video tmpVideo = new Video();
                        tmpVideo.setCategory_name(intentCategoryName);
                        tmpVideo.setVid(intentVid);
                        tmpVideo.setVideo_id(intentVideoId);
                        tmpVideo.setVideo_thumbnail(intentVideoThumbnail);
                        tmpVideo.setVideo_title(intentVideoTitle);
                        tmpVideo.setVideo_url(intentVideoUrl);
                        tmpVideo.setVideo_type(intentVideoType);
                        newposts.add(tmpVideo);
                    }

                    newposts.addAll(posts);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                displayApiResult(newposts);

            }

        }

    }

    public void wpSetOrDownload(String imageCategory, String imageUrl, Context c, boolean bJobType){
        //bJobType 0:download, 1:set wallpaper

        if (imageUrl == null){
            return;
        }

        if (c == null){
            return;
        }

        if (imageUrl.length() <= 0){
            return;
        }

        if (bJobType){
            LogEventDownload(imageCategory, imageUrl);
            LogEventDowNumSegWall();
        }else{
            LogEventSetWp(imageCategory, imageUrl);
            LogEventSetNumSegWall();
        }

        boolean bPerNeeded = false;

        if (Build.VERSION.SDK_INT > 22) {

            int permissionCheck = ContextCompat.checkSelfPermission(c, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                bPerNeeded = true;

                ActivityCompat.requestPermissions(
                        act, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
            }

        }

        if (bPerNeeded == false){

            String fname = "CarWallpapers-" + System.currentTimeMillis() + ".jpg";

            new MyTaskWPDownload(new AsyncResponse() {

                @Override
                public void processFinish(Bitmap bitmap) {

                    if (bitmap == null){

                        String strFailedNetwork = cont.getString(R.string.error_download);

                        if (bJobType){
                            strFailedNetwork = cont.getString(R.string.error_setwp);
                        }

                        Toast.makeText(cont, strFailedNetwork, Toast.LENGTH_LONG).show();

                    }else {
                        String savedImageStatus = insertImage(getActivity().getContentResolver(), bitmap, fname , fname);

                        if (savedImageStatus != null){

                            if (bJobType){
                                Uri picUri = Uri.parse(savedImageStatus);
                                Intent setAs = new Intent(Intent.ACTION_ATTACH_DATA);
                                setAs.setDataAndType(picUri, "image/*");
                                startActivityForResult(Intent.createChooser(setAs, "Set As"), 0);
                            }else{
                                Toast.makeText(c,
                                        c.getString(R.string.toast_saved).replace("#",
                                                "\"" + savedImageStatus + "\""),
                                        Toast.LENGTH_LONG).show();

                                Log.d("Success", "Wallpaper saved to: " + savedImageStatus);
                            }

                        }else {

                            if (bJobType){
                                Toast.makeText(cont, cont.getString(R.string.toast_wallpaper_set_failed), Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(c,
                                        c.getString(R.string.toast_saved_failed),
                                        Toast.LENGTH_SHORT).show();
                            }

                        }

                        setFeedBtnDefaults(bJobType);
                    }

                }

            }).execute(imageUrl);

        }

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
                    Log.e("umutrecent", "FragmentRecent image download error : " + e.toString());
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
            Log.e("umutrecent", "FragmentRecent file not found error : " + ex.toString());
            return null;
        } catch (IOException ex) {
            Log.e("umutrecent", "FragmentRecent io error : " + ex.toString());
            return null;
        }
    }

    public Bitmap getBitmapFromUrl(String url) {
        Bitmap retBmp = null;

        HttpURLConnection linkConnection = null;
        try {
            URL linkurl = new URL(url);
            linkConnection = (HttpURLConnection) linkurl.openConnection();
            InputStream linkinStream = linkConnection.getInputStream();
            retBmp = BitmapFactory.decodeStream(linkinStream);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (linkConnection != null) {
                linkConnection.disconnect();
            }
        }

        return retBmp;

    }

    public void setFeedBtnDefaults(Boolean pBJobType){
        Button btn_tmp;

        if (pBJobType){
            btn_tmp = (Button) getView().findViewById(R.id.feed_setwp);
            btn_tmp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_drawer_wp, 0, 0, 0);
            btn_tmp.setText(R.string.feed_button_setwp);
        }else {
            btn_tmp = (Button) getView().findViewById(R.id.feed_download);
            btn_tmp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_drawer_download, 0, 0, 0);
            btn_tmp.setText(R.string.feed_button_download);
        }

    }

    public void LogEventDownload(String strCategory, String strUrl){
        Bundle bundle = new Bundle();
        bundle.putString(getActivity().getString(R.string.fire_param_category), strCategory);
        bundle.putString(getActivity().getString(R.string.fire_param_url), strUrl);
        mFirebaseAnalytics.logEvent(getActivity().getString(R.string.fire_event_download), bundle);
    }

    public void LogEventSetWp(String strCategory, String strUrl){
        Bundle bundle = new Bundle();
        bundle.putString(getActivity().getString(R.string.fire_param_category), strCategory);
        bundle.putString(getActivity().getString(R.string.fire_param_url), strUrl);
        mFirebaseAnalytics.logEvent(getActivity().getString(R.string.fire_event_setwp), bundle);
    }

    public void LogEventDowNumSegWall(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int iDownNum = preferences.getInt(getActivity().getString(R.string.fire_event_downumsegwall), 0);

        int iNextNumSeg = getNumSeg(iDownNum + 1);

        if (iNextNumSeg > getNumSeg(iDownNum)){
            Bundle bundle = new Bundle();
            bundle.putInt(getActivity().getString(R.string.fire_param_numseg), iNextNumSeg);
            mFirebaseAnalytics.logEvent(getActivity().getString(R.string.fire_event_downumsegwall), bundle);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.fire_event_downumsegwall), (iDownNum + 1));
        editor.commit();

    }

    public void LogEventSetNumSegWall(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int iSetNum = preferences.getInt(getActivity().getString(R.string.fire_event_setnumsegwall), 0);

        int iNextNumSeg = getNumSeg(iSetNum + 1);

        if (iNextNumSeg > getNumSeg(iSetNum)){
            Bundle bundle = new Bundle();
            bundle.putInt(getActivity().getString(R.string.fire_param_numseg), iNextNumSeg);
            mFirebaseAnalytics.logEvent(getActivity().getString(R.string.fire_event_setnumsegwall), bundle);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.fire_event_setnumsegwall), (iNextNumSeg + 1));
        editor.commit();

    }

}

