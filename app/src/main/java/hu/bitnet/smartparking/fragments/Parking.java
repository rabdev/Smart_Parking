package hu.bitnet.smartparking.fragments;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import hu.bitnet.smartparking.R;
import hu.bitnet.smartparking.objects.Constants;

import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Parking extends Fragment {

    SharedPreferences pref;
    AppCompatButton btn_status;
    LinearLayout parkingtime_start, parkingtime_inprogress, status_start, status_inprogress, status_checkout;
    TextView header;

    public Parking() {
        // Required empty public constructor
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parking = inflater.inflate(R.layout.fragment_parking, container, false);
        pref = getActivity().getPreferences(0);

        parkingtime_start= (LinearLayout) parking.findViewById(R.id.parkingtime_start);
        status_start = (LinearLayout) parking.findViewById(R.id.status_start);
        status_inprogress = (LinearLayout) parking.findViewById(R.id.status_inprogress);
        status_checkout = (LinearLayout) parking.findViewById(R.id.status_checkout);
        header = (TextView) parking.findViewById(R.id.parking_header);
        btn_status = (AppCompatButton) parking.findViewById(R.id.btn_status);

        if (pref.getString(Constants.ParkingStatus,"").isEmpty()){
            btn_status.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent, getActivity().getTheme()));
            btn_status.setText("Parkolás megkezdése");
            header.setText("Parkolás megkezdése");
            status_start.setVisibility(View.VISIBLE);
            status_inprogress.setVisibility(View.GONE);
            status_checkout.setVisibility(View.GONE);
        } else {
            if (pref.getString(Constants.ParkingStatus,"").equals("1")) {
                btn_status.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent, getActivity().getTheme()));
                btn_status.setText("Parkolás megkezdése");
                header.setText("Parkolás megkezdése");
                status_start.setVisibility(View.VISIBLE);
                status_inprogress.setVisibility(View.GONE);
                status_checkout.setVisibility(View.GONE);
            } else if (pref.getString(Constants.ParkingStatus,"").equals("2")) {
                btn_status.setBackgroundTintList(getResources().getColorStateList(R.color.colorPurple, getActivity().getTheme()));
                btn_status.setText("Parkolás befejezése");
                header.setText("Parkolás folyamatban");
                status_start.setVisibility(View.GONE);
                status_inprogress.setVisibility(View.VISIBLE);
                status_checkout.setVisibility(View.GONE);
            } else if (pref.getString(Constants.ParkingStatus,"").equals("3")){
                btn_status.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark, getActivity().getTheme()));
                btn_status.setText("Fizetés");
                header.setText("Parkolás befejezése");
                status_start.setVisibility(View.GONE);
                status_inprogress.setVisibility(View.GONE);
                status_checkout.setVisibility(View.VISIBLE);
            }
        }


        //parkingtime_con.setVisibility(View.GONE);

        btn_status.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (pref.getString(Constants.ParkingStatus,"").equals("1")) {
                    btn_status.setBackgroundTintList(getResources().getColorStateList(R.color.colorPurple, getActivity().getTheme()));
                    btn_status.setText("Parkolás befejezése");
                    header.setText("Parkolás folyamatban");

                    status_start.setVisibility(View.GONE);
                    status_inprogress.setVisibility(View.VISIBLE);
                    status_checkout.setVisibility(View.GONE);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Constants.ParkingStatus, "2");
                    editor.apply();
                } else if (pref.getString(Constants.ParkingStatus,"").equals("2")) {
                    btn_status.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark, getActivity().getTheme()));
                    btn_status.setText("Fizetés");
                    header.setText("Parkolás befejezése");

                    status_start.setVisibility(View.GONE);
                    status_inprogress.setVisibility(View.GONE);
                    status_checkout.setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.btn_inprogress).setVisibility(View.GONE);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Constants.ParkingStatus, "3");
                    editor.apply();
                } else if (pref.getString(Constants.ParkingStatus,"").equals("3")){
                    btn_status.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent, getActivity().getTheme()));
                    btn_status.setText("Parkolás megkezdése");
                    header.setText("Parkolás megkezdése");

                    status_start.setVisibility(View.VISIBLE);
                    status_inprogress.setVisibility(View.GONE);
                    status_checkout.setVisibility(View.GONE);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Constants.ParkingStatus, "1");
                    editor.apply();
                } else if (pref.getString(Constants.ParkingStatus,"").isEmpty()){
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Constants.ParkingStatus, "1");
                    editor.apply();
                    btn_status.callOnClick();
                }
            }
        });

        return parking;
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
                    getActivity().findViewById(R.id.btn_myloc).setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });
    }

}
