package stories.spectrum.huji.ac.il.stories.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Session;
import stories.spectrum.huji.ac.il.stories.Story;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;
import stories.spectrum.huji.ac.il.stories.views.StoriesTextView;

public abstract class BaseActivity extends AppCompatActivity implements
        LocationListener, NavigationView.OnNavigationItemSelectedListener {

    public static final String NAME_OF_PUT_EXTRA_PARAM_LOCATION = "location";

    public static final int OPEN_NEW_ACTIVITY = 999;

    protected static final int NO_LOCATION_SET = -1;
    protected final Context context = this;

    protected Snackbar snackbar = null;

    protected static Session session = null;

    protected Location deviceLocation = null;

    protected LocationManager locationManager;

    protected Timer timer = null;

    protected long lastGPSCheckTime = -1;

    protected static boolean croutonShown = false;

    protected Crouton crouton = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Inject the layout that getLayoutId() returns into the activity_container element
        LayoutInflater inflater = LayoutInflater.from(context);
        View inflatedLayout = inflater.inflate(getLayoutId(), null, false);

        // Set the correct width and height retrieved by the getLayoutWidthHeight method
        CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(new ViewGroup.LayoutParams(getLayoutWidthHeight(), getLayoutWidthHeight()));
        (findViewById(R.id.activity_container)).setLayoutParams(lp);
        ((LinearLayout)(findViewById(R.id.activity_container))).addView(inflatedLayout);


        // Set session
        if (session == null) {
            session = new Session(context);
        }

        // Set name
        View navHeader = navigationView.getHeaderView(0);
        ((StoriesTextView)navHeader.findViewById(R.id.menuUserName)).setText(session.getUserName());

        crouton = Crouton.makeText(this, getString(R.string.gps_no_signal), Style.ALERT);
        crouton.setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build());

        if (lastGPSCheckTime != -1) {
            lastGPSCheckTime = System.currentTimeMillis();
        }

        // Show Crouton from previous activity
        if (croutonShown) {
            showCrouton();
        }

        //setTimer();

        if (timer == null) {
            timer = new Timer();

            // Handler that handles the UI manipulations (shows the crouton)
            final Handler mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
//                    if (!croutonShown) {
                    showCrouton();
                    croutonShown = true;
//                    }
                }
            };

            // Set timer for GPS check
            timer.schedule(new TimerTask() {
                public void run() {
                    if (lastGPSCheckTime + StoryServerURLs.GPS_CHECK_INTERVAL_MILLISECONDS <= System.currentTimeMillis()) {
                        //mHandler.obtainMessage(0, 0).sendToTarget();
                    }
                }
            }, 0, StoryServerURLs.GPS_CHECK_INTERVAL_MILLISECONDS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (croutonShown) {
            showCrouton();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        dismissCrouton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissCrouton();
    }

    @Override
    public void onProviderDisabled(String provider) {
        showSnack(getString(R.string.gps_turned_off), Snackbar.LENGTH_INDEFINITE, getString(R.string.gps_turn_on), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onProviderEnabled(String provider) {
        dismissSnack();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLocationChanged(Location location) {
        deviceLocation = location;
        lastGPSCheckTime = System.currentTimeMillis();
        dismissCrouton();
        croutonShown = false;

//        String msg = "New Latitude: " + deviceLocation.getLatitude()
//                + "\nNew Longitude: " + deviceLocation.getLongitude();
//       Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
    }

    protected void setTimer() {

        if (timer == null) {
            timer = new Timer();

            // Handler that handles the UI manipulations (shows the crouton)
            final Handler mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
//                    if (!croutonShown) {
                    showCrouton();
                    croutonShown = true;
//                    }
                }
            };

            // Set timer for GPS check
            timer.schedule(new TimerTask() {
                public void run() {
                    if (lastGPSCheckTime + StoryServerURLs.GPS_CHECK_INTERVAL_MILLISECONDS <= System.currentTimeMillis()) {
                        mHandler.obtainMessage(0, 0).sendToTarget();
                    }
                }
            }, 0, StoryServerURLs.GPS_CHECK_INTERVAL_MILLISECONDS);
        }
    }

    /**
     * Returns the distance from the given location to the phone.
     *
     * @param locationLatitude  The location's latitude
     * @param locationLongitude The location's longitude
     * @return The distance in meters
     */
    protected double distanceFromLocation(double locationLatitude, double locationLongitude) {

        if (deviceLocation == null) {
            return NO_LOCATION_SET;
        }

        Location loc2 = new Location("");
        loc2.setLatitude(locationLatitude);
        loc2.setLongitude(locationLongitude);

        return deviceLocation.distanceTo(loc2); // * (float)1.6;
    }

    /**
     * Returns the distance from the given Story to the phone.
     *
     * @param story The Story
     * @return The distance in meters
     */
    protected double distanceFromLocation(Story story) {
        return distanceFromLocation(story.storyCoordLatitude, story.storyCoordLongitude);
    }

    /**
     * Get the textual representation of distance value in meters
     *
     * @param metersDouble Distance in meters
     * @return The textual representation
     */
    protected String metersToString(double metersDouble) {
        int meters;
        String suffix;

        if (metersDouble <= 1000) {
            meters = Double.valueOf(metersDouble).intValue();
            suffix = "מטרים";
        } else {
            meters = Double.valueOf(metersDouble / 1000).intValue();
            suffix = "ק\"מ";
        }

        return meters + " " + suffix;
    }

    /**
     * Show as Snackbar with OnClickListener binding
     * Dismisses older Snackbars.
     *
     * @param text           The text to display
     * @param length         the length
     * @param buttonTxt      The text on button
     * @param buttonListener The on OnClickListener
     */
    protected void showSnack(String text, int length, String buttonTxt, View.OnClickListener buttonListener) {
        dismissSnack();
        View parentLayout = findViewById(android.R.id.content);
        snackbar = Snackbar.make(parentLayout, text, length)
                .setAction(buttonTxt, buttonListener)
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light));
        snackbar.show();
    }

    /**
     * Show as Snackbar without OnClickListener binding
     * Dismisses older Snackbars.
     *
     * @param text      The text to display
     * @param length    the length
     * @param buttonTxt The text on button
     */
    protected void showSnack(String text, int length, String buttonTxt) {
        dismissSnack();
        View parentLayout = findViewById(android.R.id.content);
        snackbar = Snackbar.make(parentLayout, text, length)
                .setAction(buttonTxt, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do nothing
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light));
        snackbar.show();
    }

    /**
     * Dismiss the active Snackbar
     */
    protected void dismissSnack() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    /**
     * Show a crouton with the givrn text
     *
     * @param text the text show inside the crouton
     */
    protected void showCrouton() {
        if (crouton != null) {
            dismissCrouton();
            crouton.show();
        }

    }

    /**
     * Cancel all scheduled Croutons.
     */
    protected void dismissCrouton() {
        //Crouton.cancelAllCroutons();

        if (crouton != null) {
            crouton.cancel();
        }
    }

    /**
     * Converts server date string format to readable string
     *
     * @param date the date
     * @return a readable string format
     */
    public static String serverDateToString(String date) {
        String year = date.substring(2, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);
        String hour = date.substring(11, 16);
        return String.format("%s/%s/%s %s", day, month, year, hour);
    }

    /**
     * Get the layout of the activity.
     *
     * @return the activity's layout
     */
    abstract protected int getLayoutId();

    /**
     * Sets the layout Width/Height of the "activity container"
     * Default is WRAP_CONTENT
     * @return
     */
    protected int getLayoutWidthHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    /**
     * Shows a long Toast
     *
     * @param msg the Toast's message
     */
    protected void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(BaseActivity.this, NearStoriesActivity.class));
            finish();

        } else if (id == R.id.nav_logout) {
            session.logOut();
            startActivity(new Intent(BaseActivity.this, LoginActivity.class));


        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(BaseActivity.this, SettingsActivity.class));

        } else if (id == R.id.nav_share) {
            share();

        } else if (id == R.id.nav_send) {
            send();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_app_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_app_body));
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
    }

    protected void send() {
        share();
    }

    protected void refresh() {
        showToast("refresh");
    }
}
