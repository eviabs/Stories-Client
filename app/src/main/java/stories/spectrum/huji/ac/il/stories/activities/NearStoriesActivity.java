package stories.spectrum.huji.ac.il.stories.activities;

import android.content.Intent;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Story;
import stories.spectrum.huji.ac.il.stories.adapters.StoryAdapter;
import stories.spectrum.huji.ac.il.stories.fragments.StoriesFilterFragment;
import stories.spectrum.huji.ac.il.stories.net.AsyncRequestListener;
import stories.spectrum.huji.ac.il.stories.net.AsyncGetRequest;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;

public class NearStoriesActivity extends BaseActivity {

    ArrayList<Story> stories = new ArrayList<>();
    ArrayList<Story> filteredStories = new ArrayList<>();
    ListView listViewStories;
    StoryAdapter storyAdapter;
    NearStoriesActivity me = this;

    private ArrayList<String> AvailableTags = new ArrayList<>();
    private ArrayList<String> ChosenTags = new ArrayList<>();

    public static boolean AddedNewStory = false;

    private StoriesFilterFragment storiesFilterFragment = new StoriesFilterFragment();

    @Override
    protected int getLayoutId(){
        return R.layout.activity_near_stories;
    }

    @Override
    protected int getLayoutWidthHeight() { return ViewGroup.LayoutParams.MATCH_PARENT; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       //////////////////////////////

        //Toast.makeText(context, "Name:" + session.getUserName() + "ID:" + session.getUserID(), Toast.LENGTH_LONG).show();
        ////////////////////////////

        Intent i = getIntent();
        Location locationFromSplash = (Location) i.getParcelableExtra(NAME_OF_PUT_EXTRA_PARAM_LOCATION);

        if (locationFromSplash != null) {
            deviceLocation = locationFromSplash;
        }

        listViewStories = (ListView) findViewById(R.id.listViewStories);

        storyAdapter = new StoryAdapter(this, filteredStories);
        listViewStories.setAdapter(storyAdapter);

        listViewStories.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View view, final int index, long arg3) {

                // Send intent
                Intent i = new Intent(NearStoriesActivity.this, StoryActivity.class);
                i.putExtra(Story.NAME_OF_PUT_EXTRA_PARAM, filteredStories.get(index));
                i.putExtra(NAME_OF_PUT_EXTRA_PARAM_LOCATION, deviceLocation);
                startActivityForResult(i, OPEN_NEW_ACTIVITY);
            }

        });

        AsyncUpdateStoryList();

        (findViewById(R.id.imageViewFilter)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCloseFiltersFragment();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddStory);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Send intent
                Intent i = new Intent(NearStoriesActivity.this, AddStoryActivity.class);
                i.putExtra(NAME_OF_PUT_EXTRA_PARAM_LOCATION, deviceLocation);
                startActivityForResult(i, OPEN_NEW_ACTIVITY);
            }
        });

    }

    private void openCloseFiltersFragment() {
        UpdateFilters();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim, R.anim.enter_anim, R.anim.exit_anim);

        if (fragmentManager.getBackStackEntryCount() == 0) {
            // open
            fragmentTransaction.add(R.id.filters_container, storiesFilterFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            (findViewById(R.id.listViewFiltersImageContainer)).setBackgroundColor(ContextCompat.getColor(context, R.color.colorGray));
        }
        else {
            // close
            // The animation
            Animation animation = AnimationUtils.loadAnimation(me, R.anim.exit_anim);
            animation.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));

            // AnimationListener
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    //This is the key, when the animation is finished, remove the fragment.
                    try {
                        FragmentTransaction ft = me.getSupportFragmentManager().beginTransaction();
                        ft.remove(storiesFilterFragment);
                        ft.commitAllowingStateLoss();
                        me.getSupportFragmentManager().popBackStack();
                        (findViewById(R.id.listViewFiltersImageContainer)).setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // must implement
                }

                @Override
                public void onAnimationStart(Animation animation) {
                    // must implement
                }

            });

            //Start the animation.
            me.findViewById(R.id.filters_container).startAnimation(animation);

        }
    }
    /**
     * Creates an AJAX call and get the stories.
     */
    private void AsyncUpdateStoryList() {

        // AJAX call
        AsyncGetRequest client = new AsyncGetRequest(this, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String jsonFromNet) {


                JSONArray arr = null;
                try {
                    if (jsonFromNet != null) {
                        arr = new JSONArray(jsonFromNet);

                        storyAdapter.clear();
                        stories.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            Story story = new Story(arr.getJSONObject(i));
                            storyAdapter.add(story);
                            stories.add(story);
                            AvailableTags.addAll(new ArrayList<>(Arrays.asList(story.storyTags)));
                        }

                        // Remove duplicate tags
                        Set<String> hs = new HashSet<>();
                        hs.addAll(AvailableTags);
                        AvailableTags.clear();
                        AvailableTags.addAll(hs);

                        UpdateFilters();
                        storiesFilterFragment.resetChosenTags();
                        //swipeRefreshLayout.setRefreshing(false);


                    } else {

                        showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AsyncUpdateStoryList();
                            }
                        });
                    }

                } catch (JSONException e) {
                    Log.e("MYAPP", "unexpected JSON exception", e);
                }
            }
        });

        client.execute(StoryServerURLs.getAllStories());
    }

    /**
     * Update filters
     */
    private void UpdateFilters() {
        storiesFilterFragment.setAllTags(AvailableTags);
    }

    /**
     * @param distance
     * @param tags
     */
    public void filterStories(double distance, ArrayList<String> tags) {

        storyAdapter.clear();
        filteredStories.clear();

        for (Story story : stories) {
            if (Arrays.asList(story.storyTags).containsAll(tags) && distanceFromLocation(story.storyCoordLatitude, story.storyCoordLongitude) <= distance) {
                filteredStories.add(story);
            }
        }
    }


    @Override
    protected void refresh() {
//        startActivity(new Intent(NearStoriesActivity.this, NearStoriesActivity.class));
//        finish();
        AsyncUpdateStoryList();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            openCloseFiltersFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (AddedNewStory) {
            if (requestCode == OPEN_NEW_ACTIVITY) {
                if (resultCode == RESULT_CANCELED) {
                    AsyncUpdateStoryList();
                }
            }

            AddedNewStory = false;
        }
    }
}
