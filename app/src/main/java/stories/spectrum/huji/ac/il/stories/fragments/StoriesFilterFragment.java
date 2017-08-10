package stories.spectrum.huji.ac.il.stories.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;


import java.text.DecimalFormat;
import java.util.ArrayList;

import me.gujun.android.taggroup.TagGroup;
import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.activities.NearStoriesActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class StoriesFilterFragment extends Fragment  implements SeekBar.OnSeekBarChangeListener {

    private static final int METERS = 20000;
    ArrayList<String> AvailableTags = new ArrayList<>();
    ArrayList<String> ChosenTags =  new ArrayList<>();

    SeekBar Seekbar;
    int Meters = METERS;
    TextView TextViewKm;

    TagGroup TagGroupAll;
    TagGroup TagGroupChosen;

    View view = null;

    public StoriesFilterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_stories_filter, container, false);

        final TagGroup mTagGroupAll = (TagGroup) view.findViewById(R.id.tagGroupAll);
        TagGroupAll = mTagGroupAll;

        final TagGroup mTagGroupChosen = (TagGroup) view.findViewById(R.id.tagGroupChosen);
        TagGroupChosen = mTagGroupChosen;

        Seekbar = (SeekBar) view.findViewById(R.id.seekBarKm);
        Seekbar.setMax(100000);
        Seekbar.setProgress(Meters);
        Seekbar.refreshDrawableState();
        TextViewKm = (TextView) view.findViewById(R.id.textViewKm);
        TextViewKm.setText(new DecimalFormat("#.#").format((double)Meters/1000));

        // Set this class as listener
        Seekbar.setOnSeekBarChangeListener(this);


        mTagGroupAll.setTags(AvailableTags);
        mTagGroupChosen.setTags(ChosenTags);

        mTagGroupAll.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                AvailableTags.remove(tag);
                ChosenTags.add(tag);

                mTagGroupAll.setTags(AvailableTags);
                mTagGroupChosen.setTags(ChosenTags);
                ((NearStoriesActivity) getActivity()).filterStories(Meters, ChosenTags);
            }
        });

        mTagGroupChosen.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                ChosenTags.remove(tag);
                AvailableTags.add(tag);

                mTagGroupAll.setTags(AvailableTags);
                mTagGroupChosen.setTags(ChosenTags);
                ((NearStoriesActivity) getActivity()).filterStories(Meters, ChosenTags);
            }
        });

        return view;
    }

    public void setAllTags(ArrayList<String> availableTags) {
        this.AvailableTags = availableTags;
    }

    public void resetChosenTags() {
        this.ChosenTags = new ArrayList<>();
        if (TagGroupChosen != null) {
            TagGroupChosen.setTags(this.ChosenTags);
        }
    }

    public ArrayList<String> getChosenTags() {
        return ChosenTags;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Meters = progress;
        TextViewKm.setText(new DecimalFormat("#.#").format((double) Meters/1000));

    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        ((NearStoriesActivity) getActivity()).filterStories(Meters, ChosenTags);
    }
}
