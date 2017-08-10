package stories.spectrum.huji.ac.il.stories.fragments;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Recording;
import stories.spectrum.huji.ac.il.stories.activities.MapsActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddRecordingMapFragment extends Fragment {

    public RecorderFragment recorderFragment = new RecorderFragment();
    View.OnClickListener listener = null;
    Button uploadButton = null;

    public AddRecordingMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_recording_map, container, false);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.recorder_container, recorderFragment).commit();

        uploadButton = (Button) view.findViewById(R.id.buttonUpload);
        uploadButton.setOnClickListener(this.listener);
        return view;
    }

    public void setUploadButtonListener(View.OnClickListener listener) {
        this.listener = listener;

        if (recorderFragment != null && uploadButton != null) {
            uploadButton.setOnClickListener(listener);
        }
    }
}