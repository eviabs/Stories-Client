package stories.spectrum.huji.ac.il.stories.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.co.recruit_lifestyle.android.widget.PlayPauseButton;
import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Recording;
import stories.spectrum.huji.ac.il.stories.Story;
import stories.spectrum.huji.ac.il.stories.activities.BaseActivity;
import stories.spectrum.huji.ac.il.stories.activities.MapsActivity;
import stories.spectrum.huji.ac.il.stories.fragments.RecordingListMapFragment;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;


public class RecordingAdapter extends ArrayAdapter<Recording> {

    PlayPauseButton previousPlayPauseButton = null;
    PlayPauseButton currentPlayPauseButton = null;
    MediaPlayer player = new MediaPlayer();
    String currentPlayingRecording = "";
    boolean toLoad = true;

    public RecordingAdapter(Context context, ArrayList<Recording> recordings) {
        super(context, 0, recordings);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final Context context = parent.getContext();

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.recording_list_item, parent, false);
        }

        // Get the data item for this position
        final Recording recording = getItem(position);

        // Recording creator user
        final TextView recordingCreator = (TextView) convertView.findViewById(R.id.textViewRecordingCreator);
        recordingCreator.setText(recording.recordingUserName);

        // Recording date
        final TextView recordingDate = (TextView) convertView.findViewById(R.id.textViewRecordingDate);
        recordingDate.setText(BaseActivity.serverDateToString(recording.recordCreationDate));

        // Recording Length
        final TextView recordingLength = (TextView) convertView.findViewById(R.id.textViewRecordingLength);

        String time = String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(recording.recordingFileDuration),
                TimeUnit.SECONDS.toSeconds(recording.recordingFileDuration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(recording.recordingFileDuration))
        );

        recordingLength.setText(time);

        // Recording Rating
        final TextView recordingRating = (TextView) convertView.findViewById(R.id.textViewRecordingRating);
        recordingRating.setText(recording.recordingRating == -1 ? "-" : new DecimalFormat("#.#").format(recording.recordingRating));

        // Is next recording of last played?
        final LinearLayout cornerContainer = (LinearLayout) convertView.findViewById(R.id.list_item_corner_container);
        cornerContainer.setBackground(ContextCompat.getDrawable(context, ((MapsActivity)context).isNextRecordingOf(recording.recordingPreviousRecordingID) ? R.drawable.recording_list_item_selected_fill_corner :  R.drawable.recording_list_item_corner));

        final PlayPauseButton playPauseButton = (PlayPauseButton) convertView.findViewById(R.id.main_play_pause_button);
        currentPlayPauseButton = playPauseButton;
        playPauseButton.setColor(Color.WHITE);

        final ProgressBar loadingProgressBar = (ProgressBar) convertView.findViewById(R.id.loadingProgressBar);
        loadingProgressBar.setVisibility(View.GONE);

        playPauseButton.setOnControlStatusChangeListener(
                new PlayPauseButton.OnControlStatusChangeListener() {
                    @Override public void onStatusChange(View view, boolean status) {

                        ((MapsActivity)context).setCurrentCoordAsVisited();
                        ((MapsActivity)context).updateLastPlayedRecordingID(recording.recordingID);

                        if (previousPlayPauseButton != null) {

                            if (previousPlayPauseButton != playPauseButton) {
                                if (previousPlayPauseButton.isPlayed()) {
                                    previousPlayPauseButton.setPlayed(false);
                                    previousPlayPauseButton.startAnimation();
                                }
                            }
                        }

                        previousPlayPauseButton = playPauseButton;

                        if (status) {

                            setPlayer(StoryServerURLs.getRecordByPathURL(recording.recordingFilePath), playPauseButton, loadingProgressBar);

                            //player.start();

                        } else {
                            if (player != null) {
                                player.pause();
                            }

                        }
                    }
                });


        // Return the completed view to render on screen
        return convertView;
    }

    /**
     * ==================================================================
     * ====                   Media Player                           ====
     * ==================================================================
     */

    private void setPlayer(String url, final PlayPauseButton currentPlayPauseButton, final ProgressBar loadingProgressBar) {
        try {
            if (!currentPlayingRecording.equals(url)) {
                killPlayer();
                player = new MediaPlayer();
                toLoad = true;

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (currentPlayPauseButton.isPlayed()) {
                            currentPlayPauseButton.setPlayed(false);
                            currentPlayPauseButton.startAnimation();
                        }
                    }
                });

                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer player) {
                        toLoad = false;
                        currentPlayPauseButton.setVisibility(View.VISIBLE);
                        loadingProgressBar.setVisibility(View.GONE);
                        currentPlayPauseButton.setPlayed(true);
                        currentPlayPauseButton.startAnimation();
                        player.start();
                    }

                });

                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(url);
                currentPlayPauseButton.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.VISIBLE);
                player.prepareAsync();

            } else {
                if (!toLoad) {
                    currentPlayPauseButton.setVisibility(View.VISIBLE);
                    loadingProgressBar.setVisibility(View.GONE);
                    player.start();
                }
            }

            currentPlayingRecording = url;

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public boolean isPlaying() {
        return (player != null && player.isPlaying());
    }
    public void killPlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }

        resetCurrentPlayPauseButton();

    }

    public void resumePlayer() {
        if (player != null && currentPlayPauseButton != null && previousPlayPauseButton != null) {
            previousPlayPauseButton.setPlayed(false);
            previousPlayPauseButton.startAnimation();

            resetCurrentPlayPauseButton();
        }
    }

    public void pausePlayer() {
        if (player != null) {
            player.pause();
        }

        resetCurrentPlayPauseButton();

    }

    public void resetCurrentPlayPauseButton() {
        if (currentPlayPauseButton != null) {
            currentPlayPauseButton.setPlayed(false);
//            currentPlayPauseButton.startAnimation();

        }
    }
}