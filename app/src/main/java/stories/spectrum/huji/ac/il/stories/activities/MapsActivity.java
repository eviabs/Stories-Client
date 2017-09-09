package stories.spectrum.huji.ac.il.stories.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import stories.spectrum.huji.ac.il.stories.Coord;
import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Recording;
import stories.spectrum.huji.ac.il.stories.Story;
import stories.spectrum.huji.ac.il.stories.fragments.AddCoordMapFragment;
import stories.spectrum.huji.ac.il.stories.fragments.AddRecordingMapFragment;
import stories.spectrum.huji.ac.il.stories.fragments.RecordingListMapFragment;
import stories.spectrum.huji.ac.il.stories.net.AsyncGetRequest;
import stories.spectrum.huji.ac.il.stories.net.AsyncPostRequest;
import stories.spectrum.huji.ac.il.stories.net.AsyncRequestListener;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;
import stories.spectrum.huji.ac.il.stories.views.StoriesTextView;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap = null;

    private Story story;
    private ArrayList<Coord> coords = new ArrayList<>();

    private boolean startingStory = true;

    final RecordingListMapFragment recordingListMapFragment = new RecordingListMapFragment();
    final AddRecordingMapFragment addRecordingMapFragment = new AddRecordingMapFragment();
    final AddCoordMapFragment addCoordMapFragment = new AddCoordMapFragment();

    int currentCoordArrIndex = -1;
    int currentCoordOrder = 1;

    private boolean cameraMoved = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_maps;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get story from intent
        story = getIntent().getParcelableExtra(Story.NAME_OF_PUT_EXTRA_PARAM);

        // Story title/name
        TextView storyTitle = (TextView) findViewById(R.id.textViewMapsStoryTitle);
        storyTitle.setText((story == null || story.storyName == null) ? "Error lading name" : story.storyName);

        // AJAX call for coords
        AsyncUpdateCoordsOfStory(story.storyID);

        addRecordingMapFragment.setUploadButtonListener(new View.OnClickListener() {
            public void onClick(View v) {

                // AJAX upload file
                if (currentCoordArrIndex >= 0 && coords.get(currentCoordArrIndex) != null && addRecordingMapFragment.recorderFragment.getLastRecordedFile() != null) {

                    // By default, we assume no previous recording exists
                    int lastPlayedRecordingID = StoryServerURLs.NO_PREVIOUS_RECORDING;
                    int currentOrder = coords.get(currentCoordArrIndex).coordOrder;

                    // If we hit the second coord, it's previous must be the first (and only!) recording on the first coord.
                    if (currentOrder == 2) {
                        lastPlayedRecordingID = coords.get(getCoordArrIDByCoordOrder(StoryServerURLs.FIRST_COORD_ORDER)).coordRecordings.get(0).recordingID;
                    }

                    // Otherwise, just get the last played recording from last coord.
                    if (currentOrder > 2) {
                        lastPlayedRecordingID = coords.get(getCoordArrIDByCoordOrder(currentOrder - 1)).lastPlayedRecordingID;

                    }

                    AsyncUploadRecordingToServer(session.getUserID(), currentCoordArrIndex, lastPlayedRecordingID);
                } else {
                    showSnack(getString(R.string.no_recorded_audio), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
                }
            }
        });


    }

    /**
     * ==================================================================
     * ====                   GPS Requests                           ====
     * ==================================================================
     */

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker arg0) {
                if (coords != null && !coords.isEmpty()) {

                    // Set currentCoordArrIndex
                    currentCoordArrIndex = Integer.parseInt(arg0.getId().replace("m", ""));
                    currentCoordOrder = coords.get(currentCoordArrIndex).coordOrder;

                    redrawAllMarkers();

                    // AJAX call for recordings
                    AsyncUpdateRecordingsAtCoord(currentCoordArrIndex);
                }
                return true;
            }
        });

//        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
//            @Override
//            public void onMapLongClick(final LatLng latLng) {
//                showAddCoordFragmentAtCoord(latLng);
//            }
//        });

        // Set my location
        try {
            mMap.setMyLocationEnabled(true);

        } catch (SecurityException e) {
            // TODO: handle
        }
    }

    /**
     * Moves the camera to the target.
     *
     * @param target
     */
    private void moveCamera(LatLng target) {
        if (!cameraMoved) {
            if (mMap != null) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(target)
                        .zoom(17)                   // Sets the zoom
                        .bearing(310)                // Sets the orientation of the camera to east
                        .tilt(90)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                cameraMoved = true;
            }
        }
    }

    public void showAddCoordFragmentAtCoord(final LatLng latLng) {
        addCoordMapFragment.setAddCoordButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("To add coord?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AsyncAddNewCoord(session.getUserID(), coords.get(0).coordRouteID, latLng.latitude, latLng.longitude);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        showAddCoordFragment();
    }

    private enum MarkerType {Current, Visited, NotVisited}

    ;

    private Bitmap getMarkerBitmapFromView(MarkerType markerType, int index) {

        // Set custom Image
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.custom_marker_pin);
        switch (markerType) {
            case Current:
                markerImageView.setImageResource(R.drawable.map_book_pin_icon_current);
                break;
            case Visited:
                markerImageView.setImageResource(R.drawable.map_book_pin_icon_visited);
                break;
            default:
                markerImageView.setImageResource(R.drawable.map_book_pin_icon_not_visited);
                break;

        }

        // Set Text
        StoriesTextView markerTextView = (StoriesTextView) customMarkerView.findViewById(R.id.custom_marker_text);
        markerTextView.setText(index + "");

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    /**
     * @param index
     * @param point
     * @param title
     * @return
     */
    private Marker createMarker(int index, LatLng point, MarkerType markerType) {
        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting latitude and longitude for the marker
        markerOptions.position(point);


        markerOptions.title(index + "");


        // Change icon
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(markerType, index)));

        // Add marker on the Google Map
        Marker newMarker = mMap.addMarker(markerOptions);
        newMarker.setTag(index);

        // Return it
        return newMarker;
    }

    //// TODO: 02/06/2017 DOC ME!
    public void redrawAllMarkers() {
        for (int i = 0; i < coords.size(); ++i) {
            if (coords.get(i).coordOrder == currentCoordOrder) {
                coords.get(i).marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(MarkerType.Current, coords.get(i).coordOrder)));
            } else {
                coords.get(i).marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(coords.get(i).isVisited ? MarkerType.Visited : MarkerType.NotVisited, coords.get(i).coordOrder)));
            }
        }
    }

    /**
     * @return
     */
    private int getClosestCoordArrID() {

        if (coords.isEmpty()) {
            return StoryServerURLs.EMPTY_COORD_LIST;
        }

        int closestCoordID = 0;
        double minDistance = distanceFromLocation(coords.get(0).coordLatitude, coords.get(0).coordLongitude);

        for (int i = 1; i < coords.size(); ++i) {
            double newMinDistance = distanceFromLocation(coords.get(i).coordLatitude, coords.get(i).coordLongitude);
            if (newMinDistance < minDistance) {
                minDistance = newMinDistance;
                closestCoordID = i;
            }
        }

        //txt.setText(minDistance + " from " + ((minDistance >= 0 && minDistance <= StoryServerURLs.MAX_DISTANCE_METERS_FROM_ACTIVE_COORD) ? closest : -1));
        return (minDistance >= 0 && minDistance <= StoryServerURLs.MAX_DISTANCE_METERS_FROM_ACTIVE_COORD) ? closestCoordID : StoryServerURLs.TOO_FAR_FROM_COORDS;
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

        // if manual navigation is not set
        if (!session.getSettingsNavigation()) {
            // if there is no playing sound
            if ((recordingListMapFragment != null && recordingListMapFragment.isPlaying())) {
                int newCurrentCoordArrIndex = getClosestCoordArrID();

                // if coords empty, return and finish
                if (newCurrentCoordArrIndex == StoryServerURLs.EMPTY_COORD_LIST) {
                    return;
                }

                // if too far for any coord, show add new coord
                if (newCurrentCoordArrIndex == StoryServerURLs.TOO_FAR_FROM_COORDS) {

                    currentCoordOrder = StoryServerURLs.NOT_EXIST;
                    currentCoordArrIndex = StoryServerURLs.NOT_EXIST;

                    // return and do nothing if not all coords visited and were listed to
                    for (Coord coord : coords) {
                        if (coord.lastPlayedRecordingID == StoryServerURLs.NO_PREVIOUS_RECORDING) {
                            redrawAllMarkers();
                            return;
                        }
                    }

                    showAddCoordFragmentAtCoord(new LatLng(location.getLatitude(), location.getLongitude()));
                    return;

                }

                // get new order
                int newCurrentCoordOrder = coords.get(newCurrentCoordArrIndex).coordOrder;


                //

                // if we moved, both currentCoordOrder and currentCoordArrIndex needs to be updated
                if (currentCoordOrder != newCurrentCoordOrder) {

                    currentCoordOrder = newCurrentCoordOrder;
                    currentCoordArrIndex = newCurrentCoordArrIndex;

                    AsyncUpdateRecordingsAtCoord(currentCoordArrIndex);

                }
            }
        }
    }

    /***
     * Sets the current coord as visited
     */
    public void setCurrentCoordAsVisited() {
        if (recordingListMapFragment.coordArrIndexOfRecordingList >= 0 && recordingListMapFragment.coordArrIndexOfRecordingList < coords.size()) {
            coords.get(recordingListMapFragment.coordArrIndexOfRecordingList).isVisited = true;
        }
    }

    public void updateLastPlayedRecordingID(int lastPlayedRecordingID) {
        if (recordingListMapFragment.coordArrIndexOfRecordingList >= 0 && recordingListMapFragment.coordArrIndexOfRecordingList < coords.size()) {
            coords.get(recordingListMapFragment.coordArrIndexOfRecordingList).lastPlayedRecordingID = lastPlayedRecordingID;
        }
    }

    public boolean isNextRecordingOf(int recordingPreviousRecordingID) {
        if (currentCoordOrder > 1) {
            return coords.get(getCoordArrIDByCoordOrder(currentCoordOrder - 1)).lastPlayedRecordingID == recordingPreviousRecordingID;
        }

        return false;
    }

    /***
     * Checks if the current coord is the first one
     *
     * @return true/false
     */
    public boolean isFirstCoord() {
        return currentCoordOrder == 1;
    }
    /**
     * ==================================================================
     * ====                   Net Requests                           ====
     * ==================================================================
     */

    /**
     * Create an AJAX call and get the coords o story coordID, and update the map markers.
     * Each marker click event will result listing its recordings.
     *
     * @param storyID
     */
    private void AsyncUpdateCoordsOfStory(final int storyID) {

        // Create the client, and instantiate an inner class as listener
        AsyncGetRequest client = new AsyncGetRequest(context, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String jsonFromNet) {

                JSONArray arr = null;
                try {

                    if (jsonFromNet != null) {
                        arr = new JSONArray(jsonFromNet);
                        coords.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            Coord newCord = new Coord(arr.getJSONObject(i));
                            coords.add(newCord);
                            if (newCord.coordOrder == currentCoordOrder) {
                                newCord.marker = createMarker(newCord.coordOrder, new LatLng(newCord.coordLatitude, newCord.coordLongitude), MarkerType.Current);
                            } else {
                                newCord.marker = createMarker(newCord.coordOrder, new LatLng(newCord.coordLatitude, newCord.coordLongitude), newCord.isVisited ? MarkerType.Visited : MarkerType.NotVisited);
                            }
                        }

                        redrawAllMarkers();
                        moveCamera(new LatLng(coords.get(0).coordLatitude, coords.get(0).coordLongitude));

                        if (startingStory) {
                            startingStory = false;
                            currentCoordArrIndex = getCoordArrIDByCoordOrder(StoryServerURLs.FIRST_COORD_ORDER);
                            AsyncUpdateRecordingsAtCoord(currentCoordArrIndex);

                        }
                    } else {
                        showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AsyncUpdateCoordsOfStory(storyID);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("MYAPP", "unexpected JSON exception", e);
                }
            }
        });

        // Execute the call
        client.execute(StoryServerURLs.getAllCoordsByStoryIDURL(storyID));

    }

    /**
     * Create an AJAX call and get the recordings of coord coordID, and update the list of recordings.
     * Each list item click event will result playing the sound.
     *
     * @param coordArrIndex
     */
    private void AsyncUpdateRecordingsAtCoord(final int coordArrIndex) {

        // Create the client, and instantiate an inner class as listener
        AsyncGetRequest client = new AsyncGetRequest(context, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String jsonFromNet) {

                JSONArray arr = null;
                try {

                    if (jsonFromNet != null) {
                        arr = new JSONArray(jsonFromNet);


                        coords.get(coordArrIndex).coordRecordings.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            coords.get(coordArrIndex).coordRecordings.add(new Recording(arr.getJSONObject(i)));
                        }

                        showRecordingListFragment(coordArrIndex);
                        redrawAllMarkers();

                    } else {
                        showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AsyncUpdateRecordingsAtCoord(coordArrIndex);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("MYAPP", "unexpected JSON exception", e);
                }
            }
        });

        // Execute the call
        client.execute(StoryServerURLs.getAllRecordingByCoordIDURL(coords.get(coordArrIndex).coordID));
    }

    /**
     * Create an AJAX call and upload the recording to the server.
     *
     * @param file
     * @param userID
     * @param CoordArrIndex
     */
    private void AsyncUploadRecordingToServer(final int userID, final int CoordArrIndex, final int lastPlayedRecording) {

        // AJAX upload file
        AsyncPostRequest client = new AsyncPostRequest(context, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String response) {

                if (response != null) {
                    AsyncUpdateRecordingsAtCoord(CoordArrIndex); // @@@@@@@@@@@@@@@@@@@@@@@@@@
                    //Toast.makeText(getApplicationContext(), "Up: " + response, Toast.LENGTH_SHORT).show();
                    addRecordingMapFragment.recorderFragment.emptyLastRecordedFile();
                    showSnack(getString(R.string.recording_added), Snackbar.LENGTH_LONG, getString(R.string.dismiss));
                } else {
                    showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AsyncUploadRecordingToServer(userID, coords.get(CoordArrIndex).coordID, lastPlayedRecording);
                        }
                    });
                }
            }
        }, new ArrayList<File>(Arrays.asList(new File[] {addRecordingMapFragment.recorderFragment.getLastRecordedFile()})));

        // Execute call
        client.execute(StoryServerURLs.getUploadSoundFileURL(userID, coords.get(CoordArrIndex).coordID, lastPlayedRecording, addRecordingMapFragment.recorderFragment.getLastRecordedFileDuration()));
    }



    /**
     * Create an AJAX call and add new the coord to the server.
     *
     * @param userID
     * @param routeID
     * @param latitude
     * @param longitude
     */
    private void AsyncAddNewCoord(final int userID, final int routeID, final double latitude, final double longitude) {

        // Create the client, and instantiate an inner class as listener
        AsyncGetRequest client = new AsyncGetRequest(context, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String jsonFromNet) {

                JSONArray arr = null;
                try {

                    if (jsonFromNet != null) {
                        arr = new JSONArray(jsonFromNet);
                        if (arr != null && arr.length() > 0) {
                            AsyncUpdateCoordsOfStory(story.storyID);
                            showSnack(getString(R.string.coord_added), Snackbar.LENGTH_LONG, getString(R.string.dismiss));
                        } else {
                            showSnack(getString(R.string.error), Snackbar.LENGTH_INDEFINITE, getString(R.string.dismiss));
                        }

                    } else {
                        showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AsyncAddNewCoord(userID, routeID, latitude, longitude);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("MYAPP", "unexpected JSON exception", e);
                }
            }
        });

        // Execute the call
        client.execute(StoryServerURLs.getAddNewCoordURL(userID, routeID, latitude, longitude));
    }

    /**
     * Create an AJAX call and add new the rating to the server.
     *
     * @param userID
     * @param recordingID
     * @param rating
     */
    private void AsyncAddNewRating(final int userID, final int recordingID, final float rating) {

        // Create the client, and instantiate an inner class as listener
        AsyncGetRequest client = new AsyncGetRequest(context, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String jsonFromNet) {

                if (jsonFromNet != null) {
                    showSnack(getString(R.string.rating_added), Snackbar.LENGTH_LONG, getString(R.string.dismiss));
                    AsyncUpdateRecordingsAtCoord(currentCoordArrIndex);

                } else {
                    showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AsyncAddNewRating(userID, recordingID, rating);
                        }
                    });
                }
            }
        });

        // Execute the call
        client.execute(StoryServerURLs.getAddRatingURL(userID, recordingID, rating));
    }

    /**
     * Create an AJAX call and add new the rating to the server by the current user.
     * This method is public, unlike AsyncAddNewRating.
     *
     * @param recordingID
     * @param rating
     */
    public void AsyncAddNewRatingCurrentUser(final int recordingID, final float rating) {
        AsyncAddNewRating(this.session.getUserID(), recordingID, rating);
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Fragments tras

    public void showRecordingListFragment(int coordArrIndex) {
        recordingListMapFragment.coordArrIndexOfRecordingList = coordArrIndex;
        recordingListMapFragment.coordOrderOfRecordingList = coords.get(coordArrIndex).coordOrder;
        recordingListMapFragment.killPlayer();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentManager.popBackStack("", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        recordingListMapFragment.recordings = coords.get(coordArrIndex).coordRecordings;
        recordingListMapFragment.showAllRecordings = session.getSettingsShowAllRecordings();
        recordingListMapFragment.updateRecordingList();

        fragmentTransaction.replace(R.id.container, recordingListMapFragment);
        //fragmentTransaction.addToBackStack("");
        fragmentTransaction.commit();
    }

    public void showAddRecordingFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.container, addRecordingMapFragment);
        fragmentTransaction.addToBackStack("");
        fragmentTransaction.commit();
    }

    public void showAddCoordFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.container, addCoordMapFragment);
        //fragmentTransaction.addToBackStack("");
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Gets the coord's Arr ID by it's order
     *
     * @param order the coord order
     * @return the Arr ID
     */
    private int getCoordArrIDByCoordOrder(int order) {

        if (!coords.isEmpty()) {
            for (int i = 0; i < coords.size(); ++i) {
                if (order == coords.get(i).coordOrder) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    protected void refresh() {
        Intent i = new Intent(MapsActivity.this, MapsActivity.class);
        i.putExtra(Story.NAME_OF_PUT_EXTRA_PARAM, story);
        i.putExtra(NAME_OF_PUT_EXTRA_PARAM_LOCATION, deviceLocation);
        startActivity(i);
        finish();
    }

    @Override
    protected void share() {

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_story_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_story_body) + story.storyID);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
    }
}
