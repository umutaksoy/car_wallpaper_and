package lamborghini.wallpapers.CarWallpapers.CarSounds.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import lamborghini.wallpapers.CarWallpapers.CarSounds.Config;
import lamborghini.wallpapers.CarWallpapers.CarSounds.databases.DatabaseHandlerFavorite;
import lamborghini.wallpapers.CarWallpapers.CarSounds.models.Video;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.AnalyticLogs;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.Constant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import lamborghini.wallpapers.CarWallpapers.CarSounds.R;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pierfrancescosoffritti.youtubeplayer.player.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterFavorite extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;

    private List<Video> items = new ArrayList<>();
    private List<String> favItems = new ArrayList<>();

    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemClickListener mOnItemOverflowClickListener;
    private Video pos;
    private CharSequence charSequence = null;
    private DatabaseHandlerFavorite databaseHandler;
    private FirebaseAnalytics mFirebaseAnalytics;

    public interface OnItemClickListener {
        void onItemClick(View view, Video obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterFavorite(Context context, RecyclerView view, List<Video> items) {
        this.items = items;
        this.context = context;

        try {
            this.databaseHandler = new DatabaseHandlerFavorite(context);
            List<String> tmpList = databaseHandler.getAllDataUrl();

            if (tmpList == null){
                this.favItems.add("");
            }else{
                this.favItems = tmpList;
            }

        }catch (Exception e){
            e.printStackTrace();
            this.favItems.add("");
        }

    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView category_name;
        public TextView video_title;
        public TextView video_duration;
        public TextView total_views;
        public TextView date_time;
        public TextView space;
        public ImageView video_thumbnail;
        public MaterialRippleLayout lyt_parent;
        public MaterialRippleLayout overflow;
        public YouTubePlayerView youTubePlayerView;
        public ImageView video_play_button;
        public Button btn_fav;
        public Button btn_share;
        public Button btn_setwp;
        public Button btn_download;

        public OriginalViewHolder(View v) {
            super(v);
            category_name = v.findViewById(R.id.category_name);
            video_title = v.findViewById(R.id.video_title);
            video_duration = v.findViewById(R.id.video_duration);
            date_time = v.findViewById(R.id.date_time);
            total_views = v.findViewById(R.id.total_views);
            space = v.findViewById(R.id.space);
            video_thumbnail = v.findViewById(R.id.video_thumbnail);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            overflow = v.findViewById(R.id.ripple_overflow);
            youTubePlayerView = v.findViewById(R.id.youtube_view);
            video_play_button = v.findViewById(R.id.video_play_button);
            btn_fav = v.findViewById(R.id.feed_fav);
            btn_share = v.findViewById(R.id.feed_share);
            btn_setwp = v.findViewById(R.id.feed_setwp);
            btn_download = v.findViewById(R.id.feed_download);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (!Config.DISPLAY_DATE_AS_TIME_AGO && !Config.ENABLE_VIEW_COUNT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_no_date_views, parent, false);
            vh = new OriginalViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
            vh = new OriginalViewHolder(v);
        }

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final Video p = items.get(position);
        final OriginalViewHolder vItem = (OriginalViewHolder) holder;

        vItem.category_name.setText(p.category_name);
        vItem.video_title.setText(p.video_title);

        if (p.video_type.equals("wallpaper")){

            vItem.video_title.setText(p.category_name + " Wallpapers");
            vItem.category_name.setText(p.category_name);

            if (Config.ENABLE_VIEW_COUNT) {
//                vItem.total_views.setText(Tools.withSuffix(p.total_views) + " " + context.getResources().getString(R.string.views_count));
            } else {
                vItem.total_views.setVisibility(View.GONE);
            }

            vItem.youTubePlayerView.setVisibility(View.GONE);
            vItem.video_play_button.setVisibility(View.GONE);
            vItem.video_duration.setVisibility(View.GONE);

            if (favItems != null){

                if (favItems.contains(p.video_url)){
                    vItem.btn_fav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_white, 0, 0, 0);
                }else{
                    vItem.btn_fav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_outline_white, 0, 0, 0);
                }

            }else{
                Log.e("umutfav", "AdapterFavorite onBindViewHolder: favitems is null");
            }

            Picasso.with(context)
                    .load(p.video_thumbnail)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(vItem.video_thumbnail);

            vItem.btn_fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int iFavStat = addToFav(p.category_name, p.vid, p.video_title, p.video_url, p.video_id, p.video_thumbnail, p.video_type);

                    if (iFavStat == 0){
                        vItem.btn_fav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_outline_white, 0, 0, 0);
                    }

                    if (iFavStat == 1){
                        vItem.btn_fav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_white, 0, 0, 0);
                    }

                }
            });

            vItem.btn_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    share(p.video_title, p.video_type, p.category_name, p.video_url);
                }
            });

            vItem.btn_setwp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    vItem.btn_setwp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.load, 0, 0, 0);
                    vItem.btn_setwp.setText(R.string.feed_button_wait);

                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, p, position);
                    }

                }
            });

            vItem.btn_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    vItem.btn_download.setCompoundDrawablesWithIntrinsicBounds(R.drawable.load, 0, 0, 0);
                    vItem.btn_download.setText(R.string.feed_button_wait);

                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, p, position);
                    }
                }
            });

        }else{
            vItem.btn_setwp.setVisibility(View.GONE);
            vItem.btn_download.setVisibility(View.GONE);

            vItem.category_name.setText(p.category_name);
            vItem.video_title.setText(p.video_title);

            vItem.youTubePlayerView.setVisibility(View.GONE);

            if (Config.ENABLE_DATE_DISPLAY && Config.DISPLAY_DATE_AS_TIME_AGO) {
            } else if (Config.ENABLE_DATE_DISPLAY && !Config.DISPLAY_DATE_AS_TIME_AGO) {
            } else {
                vItem.date_time.setVisibility(View.GONE);
                vItem.space.setVisibility(View.GONE);
            }

            if (favItems != null){

                if (favItems.contains(p.video_url)){
                    vItem.btn_fav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_white, 0, 0, 0);
                }else{
                    vItem.btn_fav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_outline_white, 0, 0, 0);
                }

            }else{
                Log.e("umutfav", "AdapterFavorite onBindViewHolder: favitems is null");
            }

            if (p.video_type != null && p.video_type.equals("youtube")) {
                Picasso.with(context)
                        .load(Constant.YOUTUBE_IMAGE_FRONT + p.video_id + Constant.YOUTUBE_IMAGE_BACK)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.video_thumbnail);
            } else {

                if (p.video_type.equals("wallpaper")){
                    Picasso.with(context)
                            .load(p.video_thumbnail)
                            .placeholder(R.drawable.ic_thumbnail)
                            .into(vItem.video_thumbnail);
                }else{
                    Picasso.with(context)
                            .load(Config.ADMIN_PANEL_URL + "/upload/" + p.video_thumbnail)
                            .placeholder(R.drawable.ic_thumbnail)
                            .into(vItem.video_thumbnail);
                }

            }

                vItem.lyt_parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        vItem.youTubePlayerView.setVisibility(View.VISIBLE);
                        vItem.video_thumbnail.setVisibility(View.GONE);
                        vItem.video_play_button.setVisibility(View.GONE);

                        LogEventPlay(p.category_name, p.video_url);
                        LogEventPlayNumSegVid();

                        final String strYtId = p.video_id;
                        vItem.youTubePlayerView.initialize(
                                initializedYouTubePlayer -> initializedYouTubePlayer.addListener(
                                        new AbstractYouTubePlayerListener() {
                                            @Override
                                            public void onReady() {
                                                initializedYouTubePlayer.loadVideo(strYtId, 0);

                                            }
                                        }), true);
                        vItem.youTubePlayerView.enterFullScreen();

                    }
                });

                vItem.btn_fav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int iFavStat = addToFav(p.category_name, p.vid, p.video_title, p.video_url, p.video_id, p.video_thumbnail, p.video_type);

                        if (iFavStat == 0){
                            vItem.btn_fav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_outline_white, 0, 0, 0);
                        }

                        if (iFavStat == 1){
                            vItem.btn_fav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_white, 0, 0, 0);
                        }

                    }

                });

                vItem.btn_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        share(p.video_title, p.video_type, p.category_name, p.video_url);
                    }
                });

        }


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_ITEM;
    }

    public int addToFav(String strCategoryName, String strVID, String strVideoTitle, String strVideoUrl, String strVideoId, String strVideoThumbnail, String strVideoType){
        if (strVideoType.equals("wallpaper")){
            LogEventFav(1, strCategoryName, strVideoUrl);
            LogEventFavNumSegWall();
        }else{
            LogEventFav(0, strCategoryName, strVideoUrl);
            LogEventFavNumSegVid();
        }

        int iTmpRet = -1;

        try {

            Boolean bIsInDb = databaseHandler.getFavRowURL(strVideoUrl);

            if (bIsInDb == false){
                //the data is not in favorite db table, add it into the table
                databaseHandler.AddtoFavorite(new Video(
                        strCategoryName,
                        strVID,
                        strVideoTitle,
                        strVideoUrl,
                        strVideoId,
                        strVideoThumbnail,
                        strVideoType
                ));
                Toast.makeText(context, context.getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();

                iTmpRet = 1;
            }else{
                //the data is in favorite db table, remove it from the table
                databaseHandler.RemoveFavUrl(strVideoUrl);
                Toast.makeText(context, context.getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();

                iTmpRet = 0;
            }

        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }

        return iTmpRet;
    }

    public void share(String strTitle, String strType, String strCategory, String strUrl){
        if (strType.equals("wallpaper")){
            LogEventShare(1, strCategory, strUrl);
            LogEventShareNumSegWall();
        }else{
            LogEventShare(0, strCategory, strUrl);
            LogEventShareNumSegVid();
        }

        String share_title = android.text.Html.fromHtml(strTitle).toString();
        String share_content = android.text.Html.fromHtml(context.getResources().getString(R.string.share_text)).toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + context.getPackageName());
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);

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

    public void LogEventPlay(String strCategory, String strUrl){
        Bundle bundle = new Bundle();
        bundle.putString(context.getString(R.string.fire_param_category), strCategory);
        bundle.putString(context.getString(R.string.fire_param_url), strUrl);
        mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_play), bundle);
    }

    public void LogEventFavNumSegVid(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int iFavNum = preferences.getInt(context.getString(R.string.fire_event_favnumsegvid), 0);

        int iNextNumSeg = AnalyticLogs.getNumSeg(iFavNum + 1);

        if (iNextNumSeg > AnalyticLogs.getNumSeg(iFavNum)){
            Bundle bundle = new Bundle();
            bundle.putInt(context.getString(R.string.fire_param_numseg), iNextNumSeg);
            mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_favnumsegvid), bundle);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.fire_event_favnumsegvid), (iNextNumSeg + 1));
        editor.commit();
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

    public void LogEventShareNumSegVid(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int iFavNum = preferences.getInt(context.getString(R.string.fire_event_sharenumsegvid), 0);

        int iNextNumSeg = AnalyticLogs.getNumSeg(iFavNum + 1);

        if (iNextNumSeg > AnalyticLogs.getNumSeg(iFavNum)){
            Bundle bundle = new Bundle();
            bundle.putInt(context.getString(R.string.fire_param_numseg), iNextNumSeg);
            mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_sharenumsegvid), bundle);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.fire_event_sharenumsegvid), (iNextNumSeg + 1));
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

    public void LogEventPlayNumSegVid(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int iFavNum = preferences.getInt(context.getString(R.string.fire_event_playnumsegvid), 0);

        int iNextNumSeg = AnalyticLogs.getNumSeg(iFavNum + 1);

        if (iNextNumSeg > AnalyticLogs.getNumSeg(iFavNum)){
            Bundle bundle = new Bundle();
            bundle.putInt(context.getString(R.string.fire_param_numseg), iNextNumSeg);
            mFirebaseAnalytics.logEvent(context.getString(R.string.fire_event_playnumsegvid), bundle);
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.fire_event_playnumsegvid), (iNextNumSeg + 1));
        editor.commit();
    }

}