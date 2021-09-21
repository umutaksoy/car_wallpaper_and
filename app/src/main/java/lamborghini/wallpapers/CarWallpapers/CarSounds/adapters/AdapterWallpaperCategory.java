package lamborghini.wallpapers.CarWallpapers.CarSounds.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import lamborghini.wallpapers.CarWallpapers.CarSounds.Config;
import lamborghini.wallpapers.CarWallpapers.CarSounds.R;
import lamborghini.wallpapers.CarWallpapers.CarSounds.activities.ActivityWallpaperDetail;
import lamborghini.wallpapers.CarWallpapers.CarSounds.databases.DatabaseHandlerFavorite;
import lamborghini.wallpapers.CarWallpapers.CarSounds.models.Video;

import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterWallpaperCategory extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private List<Video> items = new ArrayList<>();

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private Video pos;
    private CharSequence charSequence = null;
    private DatabaseHandlerFavorite databaseHandler;

    private int layoutType;

    public interface OnItemClickListener {
        void onItemClick(View view, Video obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterWallpaperCategory(Context context, RecyclerView view, List<Video> items, int layoutType) {
        this.items = items;
        this.context = context;
        this.layoutType = layoutType;
        lastItemViewDetector(view);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView wallpaper_title;
        public ImageView wallpaper_thumbnail;
        public MaterialRippleLayout lyt_parent;
        public MaterialRippleLayout overflow;

        public OriginalViewHolder(View v) {
            super(v);
            wallpaper_title = v.findViewById(R.id.wallpaper_title);
            wallpaper_thumbnail = v.findViewById(R.id.wallpaper_thumbnail);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            overflow = v.findViewById(R.id.ripple_overflow);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.load_more);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            //layouttype 0:video, 1:wallpaper, 2:sound
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallpaper, parent, false);
                vh = new OriginalViewHolder(v);

        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Video p = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            try {
                Picasso.Builder picassoBuilder = new Picasso.Builder(context);

                Picasso picasso = picassoBuilder.listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        // log errors
                        Log.e("umutpicasso", "AdapterWallpaperCategory onImageLoadFailed picasso error : " + exception.toString());
                    }
                }).build();

                picasso.load(p.video_thumbnail).placeholder(R.drawable.ic_thumbnail).into(vItem.wallpaper_thumbnail);

            }catch (Exception e){
                Log.e("umutpicasso", "AdapterWallpaperCategory onBindViewHolder picasso error : " + e.toString());
            }


            vItem.wallpaper_thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (items.size() <= 0 || items.size() <= position){
                        String strFailedNetwork = context.getString(R.string.failed_unknown_err);
                        Toast.makeText(context, strFailedNetwork, Toast.LENGTH_SHORT).show();
                    }else {

                        String tmpAllImages;

                        tmpAllImages = "";

                        for (int i = 0; i < items.size(); i++) {
                            tmpAllImages = tmpAllImages + items.get(i).getVideo_url() + "XÜÜÜX";
                        }

                        tmpAllImages = tmpAllImages.substring(0, tmpAllImages.length() - 5);

                        String strImageUrl = "";

                        try {
                            strImageUrl = items.get(position).getVideo_url();
                        }catch (Exception e){
                            Log.e("umut", "AdapterWallpaperCategory onClick: strImageUrl could not get, detail : " + e.toString());
                        }

                        if (getItemCount() > 0 && position <= getItemCount() && strImageUrl != ""){

                            Intent intent = new Intent(context, ActivityWallpaperDetail.class);
                            intent.putExtra("IMAGE", items.get(position).getVideo_url());
                            intent.putExtra("SIZE", getItemCount());
                            intent.putExtra("POSITION", position);
                            intent.putExtra("FROMFAV", "N");
                            intent.putExtra("ALLIMAGES", tmpAllImages);
                            intent.putExtra("ALLCATID", items.get(position).cat_id);
                            intent.putExtra("CATEGORYNAME", items.get(position).getCategory_name());
                            intent.putExtra("ADTIME", System.currentTimeMillis());
                            context.startActivity(intent);
                        }

                    }

                }
            });

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) != null) {
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }

    public void insertData(List<Video> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (onLoadMoreListener != null) {
                            int current_page = getItemCount() / Config.LOAD_MORE;
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    public class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        private MyMenuItemClickListener() {

        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case R.id.menu_context_favorite:
                    if (charSequence.equals(context.getString(R.string.favorite_add))) {
                        databaseHandler.AddtoFavorite(new Video(
                                pos.category_name,
                                pos.vid,
                                pos.video_title,
                                pos.video_url,
                                pos.video_id,
                                pos.video_thumbnail,
                                pos.video_type
                        ));
                        Toast.makeText(context, context.getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();

                    } else if (charSequence.equals(context.getString(R.string.favorite_remove))) {
                        databaseHandler.RemoveFav(new Video(pos.vid));
                        Toast.makeText(context, context.getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                    }
                    return true;

                case R.id.menu_context_share:
                    String share_title = android.text.Html.fromHtml(pos.video_title).toString();
                    String share_content = android.text.Html.fromHtml(context.getResources().getString(R.string.share_text)).toString();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + context.getPackageName());
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                    return true;

                default:
            }
            return false;
        }
    }
}