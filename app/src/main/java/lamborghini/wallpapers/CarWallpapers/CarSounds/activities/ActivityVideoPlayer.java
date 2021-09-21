package lamborghini.wallpapers.CarWallpapers.CarSounds.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;

import lamborghini.wallpapers.CarWallpapers.CarSounds.R;

public class ActivityVideoPlayer extends AppCompatActivity {

    private static final String TAG = "ActivityStreamPlayer";
    private Handler mainHandler;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);

        progressBar = findViewById(R.id.progressBar);

        mainHandler = new Handler();

        Log.d("INFO", "ActivityVideoPlayer");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void errorDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Oops!")
                .setCancelable(false)
                .setMessage("Failed to load stream, probably the stream server currently down!")
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        retryLoad();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    public void retryLoad() {
        Log.e("umutvideo", "ActivityVideoPlayer retryload method is running");
    }

}
