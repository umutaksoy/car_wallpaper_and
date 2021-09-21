package lamborghini.wallpapers.CarWallpapers.CarSounds.tab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lamborghini.wallpapers.CarWallpapers.CarSounds.Config;
import lamborghini.wallpapers.CarWallpapers.CarSounds.R;
import lamborghini.wallpapers.CarWallpapers.CarSounds.activities.ActivitySearch;
import lamborghini.wallpapers.CarWallpapers.CarSounds.activities.MainActivity;
import lamborghini.wallpapers.CarWallpapers.CarSounds.fragments.FragmentCategory;
import lamborghini.wallpapers.CarWallpapers.CarSounds.fragments.FragmentRecent;
import lamborghini.wallpapers.CarWallpapers.CarSounds.fragments.FragmentWallpaper;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.AppBarLayoutBehavior;

public class FragmentTabRecent extends Fragment {

    private MainActivity mainActivity;
    private Toolbar toolbar;
    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    private View view;

    public static int single_tab = 1;
    public static int double_tab = 3;

    String intentCategoryName = "";
    String intentVid = "";
    String intentVideoId = "";
    String intentVideoThumbnail = "";
    String intentVideoTitle = "";
    String intentVideoUrl = "";
    String intentVideoType = "";

    private int[] tabIcons = {
            R.drawable.ic_drawer_recent,
            R.drawable.ic_drawer_category
    };

    public FragmentTabRecent() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Config.ENABLE_TAB_LAYOUT) {
            view = inflater.inflate(R.layout.tab_layout, container, false);
        } else {
            view = inflater.inflate(R.layout.tab_layout_fav, container, false);
        }

        AppBarLayout appBarLayout = view.findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(double_tab);
        toolbar = view.findViewById(R.id.toolbar);
        setupToolbar();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            intentCategoryName = bundle.getString("category_name");
            intentVid = bundle.getString("vid");
            intentVideoId = bundle.getString("video_id");
            intentVideoThumbnail = bundle.getString("video_thumbnail");
            intentVideoTitle = bundle.getString("video_title");
            intentVideoUrl = bundle.getString("video_url");
            intentVideoType = bundle.getString("video_type");
        }

        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));

        if (Config.ENABLE_TAB_LAYOUT) {
            tabLayout.post(new Runnable() {
                @Override
                public void run() {
                    tabLayout.setupWithViewPager(viewPager);
                    //setupTabIcon();
                }
            });
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        return view;

    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (Config.ENABLE_TAB_LAYOUT) {
                switch (position) {
                    case 0:
                        return new FragmentRecent(intentCategoryName, intentVid, intentVideoId, intentVideoThumbnail, intentVideoTitle, intentVideoUrl, intentVideoType);
                    case 1:
                        return new FragmentCategory();
                    case 2:
                        return  new FragmentWallpaper();
                }
            } else {
                switch (position) {
                    case 0:
                        return new FragmentRecent(intentCategoryName, intentVid, intentVideoId, intentVideoThumbnail, intentVideoTitle, intentVideoUrl, intentVideoType);
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            if (Config.ENABLE_TAB_LAYOUT) {
                return double_tab;
            } else {
                return single_tab;
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (Config.ENABLE_TAB_LAYOUT) {
                switch (position) {
                    case 0:
                        return getResources().getString(R.string.tab_recent);
                    case 1:
                        return getResources().getString(R.string.tab_category);
                    case 2:
                        return  getResources().getString(R.string.tab_wallpaper);
                }
            } else {
                switch (position) {
                    case 0:
                        return getResources().getString(R.string.tab_recent);
                }
            }

            return null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivitySearch.class);
                startActivity(intent);
            }
        });
        if (Config.ENABLE_TAB_LAYOUT) {
            Log.d("Log", "Tab Layout is Enabled");
        } else {
            toolbar.setSubtitle(getString(R.string.tab_recent));
        }
            mainActivity.setSupportActionBar(toolbar);
    }

}

