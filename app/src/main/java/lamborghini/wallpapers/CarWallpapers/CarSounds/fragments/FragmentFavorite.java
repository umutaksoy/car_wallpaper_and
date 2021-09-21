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
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import lamborghini.wallpapers.CarWallpapers.CarSounds.R;
import lamborghini.wallpapers.CarWallpapers.CarSounds.adapters.AdapterFavorite;
import lamborghini.wallpapers.CarWallpapers.CarSounds.databases.DatabaseHandlerFavorite;
import lamborghini.wallpapers.CarWallpapers.CarSounds.models.Video;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static lamborghini.wallpapers.CarWallpapers.CarSounds.utils.AnalyticLogs.getNumSeg;

public class FragmentFavorite extends Fragment {

    private List<Video> data = new ArrayList<Video>();
    View root_view, parent_view;
    AdapterFavorite mAdapterFavorite;
    DatabaseHandlerFavorite databaseHandler;
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    String strWallpaperUrl = "";
    String strWallpaperCategory = "";
    boolean bJobIdAfterPermission = false;
    Context cont;
    Activity act;
    private static final int WRITE_EXTERNAL_STORAGE = 0;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_favorite, null);
        parent_view = getActivity().findViewById(R.id.lyt_content);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        linearLayout = root_view.findViewById(R.id.lyt_no_favorite);
        recyclerView = root_view.findViewById(R.id.recyclerView);

        cont = getActivity();
        act = getActivity();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        databaseHandler = new DatabaseHandlerFavorite(getActivity());
        data = databaseHandler.getAllData();

        mAdapterFavorite = new AdapterFavorite(getActivity(), recyclerView, data);
        recyclerView.setAdapter(mAdapterFavorite);

        if (data.size() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.INVISIBLE);
        }

        return root_view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

    @Override
    public void onResume() {
        super.onResume();

        data = databaseHandler.getAllData();
        mAdapterFavorite = new AdapterFavorite(getActivity(), recyclerView, data);
        recyclerView.setAdapter(mAdapterFavorite);

        mAdapterFavorite.setOnItemClickListener(new AdapterFavorite.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Video obj, int position) {
                switch (v.getId()){
                    case R.id.feed_setwp:
                        bJobIdAfterPermission = true;
                        strWallpaperCategory = obj.category_name;
                        strWallpaperUrl = obj.video_url;
                        wpSetOrDownload(strWallpaperCategory, strWallpaperUrl, cont, bJobIdAfterPermission);
                        break;
                    case R.id.feed_download:
                        bJobIdAfterPermission = false;
                        strWallpaperCategory = obj.category_name;
                        strWallpaperUrl = obj.video_url;
                        wpSetOrDownload(strWallpaperCategory, strWallpaperUrl, cont, bJobIdAfterPermission);
                        break;
                    default:
                }

            }
        });

        if (data.size() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void refreshFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.detach(this).attach(this).commit();
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
                    Log.e("umutfav", "FragmentFavorite image download error : " + e.toString());
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
            Log.e("umutfav", "FragmentFavorite file not found : " + ex.toString());
            return null;
        } catch (IOException ex) {
            Log.e("umutfav", "FragmentFavorite io error : " + ex.toString());
            return null;
        }
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
