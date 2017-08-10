package stories.spectrum.huji.ac.il.stories.fragments;


import android.media.MediaMetadataRetriever;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.io.File;

import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.recorder.RecorderComponent;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecorderFragment extends Fragment {

    ImageButton audioSendButton = null;
    RecorderComponent recorderComponent = null;

    public RecorderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recorder, container, false);

        // Set Recorder
        this.audioSendButton = (ImageButton) view.findViewById(R.id.chat_audio_send_button);
        this.recorderComponent = new RecorderComponent(getActivity(), view);
        this.audioSendButton.setOnTouchListener(recorderComponent.onTouchListener);

        return view;
    }

    public File getLastRecordedFile() {
        return recorderComponent.lastRecordedFile;
    }

    public int getLastRecordedFileDuration() {
        if (recorderComponent.lastRecordedFile != null) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(recorderComponent.lastRecordedFile.getAbsolutePath());
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Integer.parseInt(durationStr) / 1000;
        }

        return -1;
    }

    public void emptyLastRecordedFile() {
        recorderComponent.lastRecordedFile = null;
    }
}