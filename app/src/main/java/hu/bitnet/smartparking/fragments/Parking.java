package hu.bitnet.smartparking.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.bitnet.smartparking.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Parking extends Fragment {


    public Parking() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parking = inflater.inflate(R.layout.fragment_parking, container, false);
        return parking;
    }

}
