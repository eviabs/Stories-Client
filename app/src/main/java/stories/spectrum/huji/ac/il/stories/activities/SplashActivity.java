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
    private String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CLEAR_APP_CACHE};

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


    public String[] getRequiredPermissions() {
        String[] permissions = null;
        try {
            permissions = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (permissions == null) {
            return new String[0];
        } else {
            return permissions.clone();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == STORIES_PERMISSIONS_REQUESTS) {
            firstPermmisionsRequest = false;
            checkPermissions();
        }
    }

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


    private void checkPermissions() {

        String[] ungrantedPermissions = requiredPermissionsStillNeeded();
        if (ungrantedPermissions.length == 0) {
            startNextActivity();

        } else {

            if (!firstPermmisionsRequest) {
                final SplashActivity currentActivity = this;
                showSnack(getString(R.string.no_permissions), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(currentActivity, permissions, STORIES_PERMISSIONS_REQUESTS);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this,permissions, STORIES_PERMISSIONS_REQUESTS);
            }
        }
    }

    @NonNull
    private String[] requiredPermissionsStillNeeded() {

        Set<String> permissions = new HashSet<String>();
        for (String permission : getRequiredPermissions()) {
            permissions.add(permission);
        }
        for (Iterator<String> i = permissions.iterator(); i.hasNext();) {
            String permission = i.next();

            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(SplashActivity.class.getSimpleName(), "Permission: " + permission + " already granted.");
                i.remove();
            } else {
                Log.d(SplashActivity.class.getSimpleName(), "Permission: " + permission + " not yet granted.");
            }
        }
        return permissions.toArray(new String[permissions.size()]);
    }
}
