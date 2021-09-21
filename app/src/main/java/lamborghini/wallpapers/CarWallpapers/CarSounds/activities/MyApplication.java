package lamborghini.wallpapers.CarWallpapers.CarSounds.activities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;
import android.util.Log;

import com.onesignal.OneSignal;

public class MyApplication extends Application {

    Activity activity;
    public static final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        OneSignal.startInit(this)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

}
