package hu.bitnet.smartparking.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.bitnet.smartparking.R;

import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Zones extends Fragment {

    RecyclerView zones_rv;

    public Zones() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View zones = inflater.inflate(R.layout.fragment_zones, container, false);
        zones_rv = (RecyclerView) zones.findViewById(R.id.zones_rv);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        zones_rv.setLayoutManager(layoutManager);


        return zones;
    }

    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == ACTION_UP && keyCode == KEYCODE_BACK) {
                    // handle back button's click listener
                    getFragmentManager().popBackStack();
                    getActivity().findViewById(R.id.btn_parking_places).setBackgroundResource(R.drawable.button_background);
                    return true;
                }
                return false;
            }
        });
    }

}