package lamborghini.wallpapers.CarWallpapers.CarSounds.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import lamborghini.wallpapers.CarWallpapers.CarSounds.activities.ActivitySearch;
import lamborghini.wallpapers.CarWallpapers.CarSounds.activities.MainActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lamborghini.wallpapers.CarWallpapers.CarSounds.R;

import lamborghini.wallpapers.CarWallpapers.CarSounds.adapters.AdapterAbout;
import lamborghini.wallpapers.CarWallpapers.CarSounds.utils.AppBarLayoutBehavior;

import java.util.ArrayList;
import java.util.List;

public class FragmentAbout extends Fragment {

    View root_view, parent_view;
    RecyclerView recyclerView;
    private Toolbar toolbar;
    AdapterAbout adapterAbout;
    private MainActivity mainActivity;

    public FragmentAbout() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_about, null);
        parent_view = getActivity().findViewById(R.id.lyt_content);

        AppBarLayout appBarLayout = root_view.findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        toolbar = root_view.findViewById(R.id.toolbar);
        setupToolbar();

        recyclerView = root_view.findViewById(R.id.rvAllUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapterAbout = new AdapterAbout(getDataInformation(), getActivity());
        recyclerView.setAdapter(adapterAbout);

        return root_view;
    }

    private List<Data> getDataInformation() {
        List<Data> data = new ArrayList<>();

        data.add(new Data(
                R.drawable.ic_other_appname,
                getResources().getString(R.string.about_app_name),
                getResources().getString(R.string.app_name)
        ));

        data.add(new Data(
                R.drawable.ic_other_build,
                getResources().getString(R.string.about_app_version),
                getResources().getString(R.string.sub_about_app_version)
        ));

        data.add(new Data(
                R.drawable.ic_other_email,
                getResources().getString(R.string.about_app_email),
                getResources().getString(R.string.sub_about_app_email)
        ));

        data.add(new Data(
                R.drawable.ic_other_copyright,
                getResources().getString(R.string.about_app_copyright),
                getResources().getString(R.string.sub_about_app_copyright)
        ));

        data.add(new Data(
                R.drawable.ic_other_rate,
                getResources().getString(R.string.about_app_rate),
                getResources().getString(R.string.sub_about_app_rate)
        ));

        data.add(new Data(
                R.drawable.ic_other_more,
                getResources().getString(R.string.about_app_more),
                getResources().getString(R.string.sub_about_app_more)
        ));

        data.add(new Data(
                R.drawable.ic_other_privacy,
                getResources().getString(R.string.about_app_privacy_policy),
                getResources().getString(R.string.sub_about_app_privacy_policy)
        ));

        return data;
    }

    public class Data {
        private int image;
        private String title;
        private String sub_title;

        public int getImage() {
            return image;
        }

        public String getTitle() {
            return title;
        }

        public String getSub_title() {
            return sub_title;
        }

        public Data(int image, String title, String sub_title) {
            this.image = image;
            this.title = title;
            this.sub_title = sub_title;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setSubtitle(getString(R.string.drawer_about));
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivitySearch.class);
                startActivity(intent);
            }
        });
        mainActivity.setSupportActionBar(toolbar);
    }

}