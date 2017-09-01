package stories.spectrum.huji.ac.il.stories.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import me.gujun.android.taggroup.TagGroup;
import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Story;
import stories.spectrum.huji.ac.il.stories.net.AsyncGetRequest;
import stories.spectrum.huji.ac.il.stories.net.AsyncRequestListener;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;

public class StoryActivity extends BaseActivity {

    Story story = null;
    String storyID = null;
    Location locationFromSNearStories = null;

    @Override
    protected int getLayoutId(){
        return R.layout.activity_story;
    }

    @Override
    protected int getLayoutWidthHeight() { return ViewGroup.LayoutParams.MATCH_PARENT; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if tge user is not logged in (came from an outside link without logging in first) then start the splash screen
        if (!session.isLoggedIn()) {
            Intent mainIntent = new Intent(StoryActivity.this, SplashActivity.class);
            startActivity(mainIntent);
            finish();
        }

        // Check whether an we came from an external link, or from the near stories activity
        handleIntent(getIntent());

        if (locationFromSNearStories != null) {
            deviceLocation = locationFromSNearStories;
        }

    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();

        // If er came here from a link:
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null){
            storyID = appLinkData.getLastPathSegment();
//            Uri appData = Uri.parse("content://com.recipe_app/recipe/").buildUpon()
//                    .appendPath(recipeId).build();
//            showRecipe(appData);
        // Else, we came here from the near storied activity
        } else {
            Intent i = getIntent();
            story = (Story) i.getParcelableExtra(Story.NAME_OF_PUT_EXTRA_PARAM);
            locationFromSNearStories = (Location) i.getParcelableExtra(NAME_OF_PUT_EXTRA_PARAM_LOCATION);
        }

        // Get the story asynchronously (only if we came from outside the app)
        AsyncGetStoryByID(storyID);
    }

    private void renderActivity() {
        //Text Font
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/gisha.ttf");

        // Story title/name
        TextView storyTitle = (TextView) findViewById(R.id.textViewStoryTitleStoryActivity);
        storyTitle.setText((story == null || story.storyName == null) ? "Error lading name" : story.storyName);

        // Story Image
        ImageView storyImage = (ImageView) findViewById(R.id.imageViewStoryImageStoryActivity);
        String imgURL = StoryServerURLs.getImageURL(story.storyFilePath);
        Picasso.with(getApplicationContext())
                .load(imgURL)
                .fit()
                .into(storyImage);

        // Story Tags
        final TagGroup mTagGroup = (TagGroup) findViewById(R.id.tagGroupStoryTagsStoryActivity);
        mTagGroup.setTags(story.storyTags);

        // Static map
        ImageView staticMapImage = (ImageView) findViewById(R.id.imageViewStaticMap);
        String staticMapImageURL = StoryServerURLs.getStaticMapURL("600x300", story.storyCoordLatitude, story.storyCoordLongitude);
        Picasso.with(getApplicationContext())
                .load(staticMapImageURL)
                .fit()
                .into(staticMapImage);

        TextView txtAddress = (TextView) findViewById(R.id.TextViewAddress);

        if (txtAddress != null) {
            txtAddress.setText((story.getReadableStoryAddress() == "") ? "" : story.getReadableStoryAddress());
        }


        TextView txtDate = (TextView) findViewById(R.id.TextViewDate);
        txtDate.setText(serverDateToString(story.storyCreationDate));


        TextView textCreator = (TextView) findViewById(R.id.TextViewUser);
        if (textCreator != null) {
            textCreator.setText(story.storyUserName);
        }

        TextView textDistance = (TextView) findViewById(R.id.TextViewDistance);
        if (textDistance != null) {
            textDistance.setText(((distanceFromLocation(story) == -1) ? getString(R.string.cannot_get_distance) : metersToString(distanceFromLocation(story))));
        }

        Button btn = (Button) findViewById(R.id.buttonStartStory);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Send intent
                //Toast.makeText(getApplicationContext(), "index: " + ((ImageView) findViewById(R.id.imageViewStoryImage2)).getHeight() , Toast.LENGTH_SHORT).show();
                Intent i = new Intent(StoryActivity.this, MapsActivity.class);
                i.putExtra(Story.NAME_OF_PUT_EXTRA_PARAM, story);
                startActivity(i);
            }
        });

        Button btn2 = (Button) findViewById(R.id.buttonNavigate);


        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Send intent
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("http://maps.google.com/?q=" + story.storyCoordLatitude + "," + story.storyCoordLongitude));
                startActivity(myWebLink);
            }
        });
    }

    /**
     * Creates an AJAX call and get the selected story.
     * If no story ID is available, no AJAX call will be performed, and it means
     * that the story object was already passed from the previous activity.
     *
     * @param StoryID
     */
    private void AsyncGetStoryByID(String StoryID) {

        // AJAX call
        AsyncGetRequest client = new AsyncGetRequest(this, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String jsonFromNet) {


                JSONArray arr = null;
                try {
                    if (jsonFromNet != null) {
                        arr = new JSONArray(jsonFromNet);

                        for (int i = 0; i < arr.length(); i++) {
                            story = new Story(arr.getJSONObject(i));
                            renderActivity();
                        }

                    } else {

                        showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AsyncGetStoryByID(storyID);
                            }
                        });
                    }

                } catch (JSONException e) {
                    Log.e("MYAPP", "unexpected JSON exception", e);
                }
            }
        });

        // if we came from an external link, get the content asynchronously
        if (story == null) {
            client.execute(StoryServerURLs.getStoryByID(storyID));

            // otherwise, just render the activity useing the story from the previous activity
        } else {
            renderActivity();
        }
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
