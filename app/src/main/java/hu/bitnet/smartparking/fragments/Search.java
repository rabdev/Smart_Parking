package hu.bitnet.smartparking.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;

import hu.bitnet.smartparking.Adapters.SearchAdapter;
import hu.bitnet.smartparking.R;
import hu.bitnet.smartparking.objects.Parking_places;

import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Search extends Fragment {

    RecyclerView search_rv;
    public ArrayList<Parking_places> data;
    public SearchAdapter mAdapter;

    public Search() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View search = inflater.inflate(R.layout.fragment_search, container, false);


        search_rv= (RecyclerView) search.findViewById(R.id.search_rv);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        search_rv.setLayoutManager(layoutManager);

        EditText upsearch= (EditText) getActivity().findViewById(R.id.upsearch);

        upsearch.setActivated(true);
        upsearch.hasFocus();

        //Toast.makeText(getContext(), upsearch.getText().toString(),Toast.LENGTH_LONG).show();

        return search;
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
                    getFragmentManager().popBackStackImmediate();
                    return true;
                }
                return false;
            }
        });
    }

}
