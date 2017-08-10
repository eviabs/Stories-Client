package stories.spectrum.huji.ac.il.stories.fragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import jp.co.recruit_lifestyle.android.widget.PlayPauseButton;
import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Recording;
import stories.spectrum.huji.ac.il.stories.activities.MapsActivity;
import stories.spectrum.huji.ac.il.stories.adapters.RecordingAdapter;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordingListMapFragment extends Fragment {

    public ArrayList<Recording> recordings = new ArrayList<>();
    RecordingAdapter recordingAdapter;
    LinearLayout noRecordings = null;
    ListView listViewRecordings = null;
    Context context = null;
    public Button addRecordingButton = null;
    public int coordArrIndexOfRecordingList = -1;
    public int coordOrderOfRecordingList = -1;

    public RecordingListMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recordings_map, container, false);

        context = getActivity();
        listViewRecordings = (ListView) view.findViewById(R.id.listViewRecordings);
        noRecordings = (LinearLayout) view.findViewById(R.id.noRecordings);
        updateRecordingList();

        addRecordingButton = (Button) view.findViewById(R.id.moveToAddRecordingFragment);

        final MapsActivity activity = (MapsActivity) getActivity();

        addRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.showAddRecordingFragment();
            }
        });

        if (coordOrderOfRecordingList == StoryServerURLs.FIRST_COORD_ORDER) {
            addRecordingButton.setVisibility(View.GONE);
        } else {
            addRecordingButton.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        recordingAdapter.pausePlayer();
        Log.e("@@@@@@@@@@@@@@@@", "PAUSE");
    }

    @Override
    public void onResume() {
        super.onResume();
        recordingAdapter.resumePlayer();
        Log.e("@@@@@@@@@@@@@@@@", "RESUME");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recordingAdapter.killPlayer();
        Log.e("@@@@@@@@@@@@@@@@", "DEST");
    }

    public void killPlayer() {
        if (recordingAdapter != null) {
            recordingAdapter.killPlayer();
        }
    }

    public void updateRecordingList() {

        final ArrayList<Recording> recordings = this.recordings;

        if (context != null) {
            recordingAdapter = new RecordingAdapter(context, this.recordings);
            listViewRecordings.setAdapter(recordingAdapter);

            listViewRecordings.setVisibility((recordingAdapter.isEmpty()) ? View.GONE : View.VISIBLE);
            noRecordings.setVisibility((recordingAdapter.isEmpty())?View.VISIBLE:View.GONE);


            if (addRecordingButton != null) {
                if (coordOrderOfRecordingList == StoryServerURLs.FIRST_COORD_ORDER) {
                    addRecordingButton.setVisibility(View.GONE);
                } else {
                    addRecordingButton.setVisibility(View.VISIBLE);
                }
            }

            listViewRecordings.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> arg0, View view, final int index, long arg3) {
                    //playSound(StoryServerURLs.getRecordByPathURL(recordings.get(index).recordingFilePath));
                }
            });

            listViewRecordings.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id)  {


                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle(getString(R.string.add_rating));
                        final View viewInflated = LayoutInflater.from(context).inflate(R.layout.dialog_recording_rate, (ViewGroup) arg1.findViewById(android.R.id.content), false);
                        final RatingBar ratingBar = (RatingBar) viewInflated.findViewById(R.id.ratingBar);

                        builder.setView(viewInflated);

                        // Set up the buttons
                        builder.setPositiveButton(getString(R.string.ok_rating), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MapsActivity) getActivity()).AsyncAddNewRatingCurrentUser(recordings.get(pos).recordingID, ratingBar.getRating());
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                    builder.show();

                    return true;
                }
            });
        }
    }
}