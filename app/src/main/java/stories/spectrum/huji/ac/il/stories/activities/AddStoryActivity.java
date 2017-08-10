package stories.spectrum.huji.ac.il.stories.activities;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mvc.imagepicker.ImagePicker;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import me.gujun.android.taggroup.TagGroup;
import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Recording;
import stories.spectrum.huji.ac.il.stories.Story;
import stories.spectrum.huji.ac.il.stories.fragments.RecorderFragment;
import stories.spectrum.huji.ac.il.stories.net.AsyncGetRequest;
import stories.spectrum.huji.ac.il.stories.net.AsyncPostRequest;
import stories.spectrum.huji.ac.il.stories.net.AsyncRequestListener;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;
import stories.spectrum.huji.ac.il.stories.views.StoriesEditText;

import static android.R.attr.data;

public class AddStoryActivity extends BaseActivity {

    public RecorderFragment recorderFragment = new RecorderFragment();
    ArrayList<String> address = new ArrayList<>();
    TextView txtAddress = null;
    TextView txtDate = null;
    EditText storyTitle = null;
    ImageView storyImage = null;
    TagGroup mTagGroup = null;

    String imageFilePath = null;
    File imageFile = null;
    Context context = this;

    @Override
    protected int getLayoutId(){
        return R.layout.activity_add_story;
    }

    @Override
    protected int getLayoutWidthHeight() { return ViewGroup.LayoutParams.MATCH_PARENT; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageFilePath = this.getExternalCacheDir().getAbsolutePath() + "/";

        Intent i = getIntent();
        Location locationFromSNearStories = i.getParcelableExtra(NAME_OF_PUT_EXTRA_PARAM_LOCATION);

        if (locationFromSNearStories != null) {
            deviceLocation = locationFromSNearStories;
        }

        // Set address
        AsyncGetAddressOfDeviceLocation();

        // Story title/name
        storyTitle = (EditText) findViewById(R.id.editTextStoryTitleAddStoryActivity);
        storyTitle.clearFocus();

        // Story Image
        storyImage = (ImageView) findViewById(R.id.imageViewStoryImageStoryActivity);
        storyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickImage(v);
            }
        });

        // Story Tags
        mTagGroup = (TagGroup) findViewById(R.id.tagGroupAddStoryTags);
        mTagGroup.clearFocus();

        txtDate = (TextView) findViewById(R.id.TextViewDate);

        if (txtDate != null) {
            Calendar c = Calendar.getInstance();

            String date = String.format("%1$d/%2$d/%3$d %4$d:%5$d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
            txtDate.setText(date);
        }

        txtAddress = (TextView) findViewById(R.id.TextViewAddress);

        if (txtAddress != null) {
            txtAddress.setText("מחפש כתובת...");
        }

        TextView textCreator = (TextView) findViewById(R.id.TextViewUser);
        if (textCreator != null) {
            textCreator.setText(session.getUserName());
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.storyRecorderContainer, recorderFragment).commit();

        Button btn = (Button) findViewById(R.id.buttonAddStory);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AsyncUploadStoryToServer();
            }
        });
    }

    /**
     * Validated the story before uploading it
     * @return
     */
    private boolean validateStory() {

        if (deviceLocation == null) {
            showSnack(getString(R.string.no_location), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            return false;
        }

        if (storyTitle == null || mTagGroup == null || recorderFragment == null) {
            showSnack(getString(R.string.error), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            return false;
        }

        if (imageFile == null) {
            showSnack(getString(R.string.no_image), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            return false;
        }

        if (storyTitle.getText().toString().equals("")) {
            showSnack(getString(R.string.no_story_title), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            return false;
        }

        if (mTagGroup.getTags().length == 0) {
            showSnack(getString(R.string.no_tags), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            return false;
        }

        if (recorderFragment.getLastRecordedFile() == null) {
            showSnack(getString(R.string.no_recording), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            return false;
        }

        return true;
    }

    /**
     * Create an AJAX call and get the human readable address of the device's location.
     *
     */
    private void AsyncGetAddressOfDeviceLocation() {

        if (deviceLocation == null) {
            return;
        }

        // Static map
        ImageView staticMapImage = (ImageView) findViewById(R.id.imageViewStaticMap);
        String staticMapImageURL = StoryServerURLs.getStaticMapURL("600x300", deviceLocation.getLatitude(), deviceLocation.getLongitude());
        Picasso.with(getApplicationContext())
                .load(staticMapImageURL)
                .fit()
                .into(staticMapImage);

        // Create the client, and instantiate an inner class as listener
        AsyncGetRequest client = new AsyncGetRequest(context, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String jsonFromNet) {

                try {

                    if (jsonFromNet != null) {
                        JSONObject obj = new JSONObject(jsonFromNet);

                        if (obj.optString("status", "").equals("OK")) {
                            JSONArray arr = obj.optJSONArray("results");
                            JSONArray firstAddress = arr.getJSONObject(0).getJSONArray("address_components");
                            for (int i = 0; i < firstAddress.length() ; i++) {
                                address.add(firstAddress.getJSONObject(i).optString("long_name", ""));
                            }

                            if (txtAddress != null) {
                                //TODO:: edit this
                                txtAddress.setText(address.get(1) + " " + address.get(0));
                            }
                        } else {
                            if (txtAddress != null) {
                                txtAddress.setText("כתובת לא נמצאה");
                            }
                        }

                    } else {
                        showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AsyncGetAddressOfDeviceLocation();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("MYAPP", "unexpected JSON exception", e);
                }
            }
        });
        // Execute the call
        client.execute(StoryServerURLs.getAddressOfLocation(deviceLocation.getLatitude(), deviceLocation.getLongitude()));
    }

    private void AsyncUploadStoryToServer() {

        if (!validateStory()) {
            return;
        }

        // AJAX upload file
        AsyncPostRequest client = new AsyncPostRequest(context, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String response) {

                if (response != null) {
                    try {
                        JSONArray arr = new JSONArray(response);

                        ArrayList<Story> stories = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {
                            Story story = new Story(arr.getJSONObject(i));
                            stories.add(story);
                        }

                        // Start intent
                        NearStoriesActivity.AddedNewStory = true;
                        Intent i = new Intent(AddStoryActivity.this, StoryActivity.class);
                        i.putExtra(Story.NAME_OF_PUT_EXTRA_PARAM, stories.get(0));
                        startActivity(i);
                        finish();

                    } catch (JSONException e) {
                        Log.e("MYAPP", "unexpected JSON exception", e);

                        showSnack(getString(R.string.error), Snackbar.LENGTH_LONG, getString(R.string.retry));
                    }
                } else {
                    showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AsyncUploadStoryToServer();
                        }
                    });
                }


            }


        }, new ArrayList<File>(Arrays.asList(new File[]{imageFile, recorderFragment.getLastRecordedFile()})));

        // Execute call
        //int userID, String storyAddress, String storyName, String storyTags, int recordingFileDuration, double storyLatitude, double storyLongitude
        client.execute(StoryServerURLs.getUploadStoryURL(session.getUserID(),
                TextUtils.join(";", address),
                storyTitle.getText().toString(),
                TextUtils.join(";", mTagGroup.getTags()),
                recorderFragment.getLastRecordedFileDuration(),
                deviceLocation.getLatitude(),
                deviceLocation.getLongitude())
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
        if (bitmap != null) {

            try {
                //create a file to write bitmap data
                imageFile = new File(imageFilePath + "image" + System.currentTimeMillis() + ".jpg");
                imageFile.createNewFile();

                //Convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = new FileOutputStream(imageFile, false);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Log.e("IO: ", e.getMessage());
            }

            if (imageFile != null) {
                Picasso.with(context)
                        .load(imageFile)
                        .fit()
                        .into(storyImage);
            }
        }
    }

    public void onPickImage(View view) {
        ImagePicker.pickImage(this, getString(R.string.choose_image));
    }

    @Override
    protected void refresh() {

        AsyncGetAddressOfDeviceLocation();
    }
}
