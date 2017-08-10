package stories.spectrum.huji.ac.il.stories.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import stories.spectrum.huji.ac.il.stories.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddCoordMapFragment extends Fragment {

    View.OnClickListener listener = null;
    Button addCordButton = null;

    public AddCoordMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_coord_map, container, false);

        addCordButton = (Button) view.findViewById(R.id.buttonAddCoord);
        addCordButton.setOnClickListener(this.listener);
        return view;
    }

    public void setAddCoordButtonListener(View.OnClickListener listener) {
        this.listener = listener;

        if (addCordButton != null) {
            addCordButton.setOnClickListener(listener);
        }
    }
}