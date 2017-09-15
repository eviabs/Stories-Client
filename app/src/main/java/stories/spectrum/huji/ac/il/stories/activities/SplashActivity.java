package stories.spectrum.huji.ac.il.stories.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import stories.spectrum.huji.ac.il.stories.R;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class SplashActivity extends BaseActivity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 3000;
    private final int STORIES_PERMISSIONS_REQUESTS = 1234;
    private boolean firstPermmisionsRequest = true;
    private String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CLEAR_APP_CACHE};

    @Override
    protected int getLayoutId(){
        return R.layout.activity_splash;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        crouton = null;

        ImageView splashImg = (ImageView) findViewById(R.id.imageViewSplash);
        splashImg.setScaleType(ImageView.ScaleType.FIT_XY);
        checkPermissions();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == STORIES_PERMISSIONS_REQUESTS) {
            firstPermmisionsRequest = false;
            checkPermissions();
        }
    }

    /**
     * Starts the Login activity
     */
    private void startNextActivity() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                mainIntent.putExtra(NAME_OF_PUT_EXTRA_PARAM_LOCATION, deviceLocation);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    /**
     * Checks what permissions are still needed.
     * If some permissions were not given, informs the user and doesn't start next activity.
     */
    private void checkPermissions() {
        String[] ungrantedPermissions = requiredPermissionsStillNeeded();
        Log.d("@@@@@@@@@@@ permissions", "Num of ungranted permissions:" + ungrantedPermissions.length);

        // Since android 7 CLEAR_APP_CACHE cannot be given anymore, so only older phones can use it.
        if (ungrantedPermissions.length == 0 || ungrantedPermissions[0].equals("android.permission.CLEAR_APP_CACHE"))  {
            startNextActivity();

        } else {
            if (!firstPermmisionsRequest) {
                final SplashActivity currentActivity = this;
                showSnack(getString(R.string.no_permissions), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {@Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(currentActivity, permissions, STORIES_PERMISSIONS_REQUESTS);
                    }
                });
            } else {
                firstPermmisionsRequest = false;
                ActivityCompat.requestPermissions(this,permissions, STORIES_PERMISSIONS_REQUESTS);
            }
        }
    }
}
