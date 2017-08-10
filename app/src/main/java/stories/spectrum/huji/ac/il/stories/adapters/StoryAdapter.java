package stories.spectrum.huji.ac.il.stories.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import me.gujun.android.taggroup.TagGroup;
import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Story;
import stories.spectrum.huji.ac.il.stories.activities.BaseActivity;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;


public class StoryAdapter extends ArrayAdapter<Story> {
    public StoryAdapter(Context context, ArrayList<Story> stories) {
        super(context, 0, stories);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.story_list_item, parent, false);
        }

        // Get the data item for this position
        Story story = getItem(position);

        // Story title/name
        TextView storyTitle = (TextView) convertView.findViewById(R.id.textViewStoryTitle);
        storyTitle.setText((story == null || story.storyName == null) ? "-" : story.storyName);

        // Story Image
        ImageView storyImage = (ImageView) convertView.findViewById(R.id.imageViewStoryImage);
        String imgURL = StoryServerURLs.getImageURL(story.storyFilePath);
        Picasso.with(context)
                .load(imgURL)
                .fit()
                .into(storyImage);

        // Story User
        TextView storyUser = (TextView) convertView.findViewById(R.id.textViewUser);
        storyUser.setText((story == null || story.storyUserName == null) ? "-" :  "" + story.storyUserName);


        // Story Date
        TextView storyDate = (TextView) convertView.findViewById(R.id.textViewDate);
        storyDate.setText((story == null || story.storyCreationDate == null) ? "-" :  "" + BaseActivity.serverDateToString(story.storyCreationDate));

        // Story Tags
        final TagGroup mTagGroup = (TagGroup) convertView.findViewById(R.id.tagGroupStoryTags);
        mTagGroup.setTags(story.storyTags);


        // Return the completed view to render on screen
        return convertView;
    }
}