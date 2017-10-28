package hu.bitnet.smartparking.fragments;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import hu.bitnet.smartparking.R;
import hu.bitnet.smartparking.RequestInterfaces.RequestInterfaceParkingStart;
import hu.bitnet.smartparking.RequestInterfaces.RequestInterfaceParkingStatus;
import hu.bitnet.smartparking.RequestInterfaces.RequestInterfaceParkingStop;
import hu.bitnet.smartparking.ServerResponses.ServerResponse;
import hu.bitnet.smartparking.objects.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_BACK;
import static com.google.android.gms.internal.zzagz.runOnUiThread;
import static com.google.android.gms.wearable.DataMap.TAG;
import static java.lang.Long.parseLong;

/**
 * A simple {@link Fragment} subclass.
 */
public class Parking extends Fragment {

    SharedPreferences pref;
    AppCompatButton btn_status;
    LinearLayout parkingtime_start, parkingtime_inprogress, status_start, status_inprogress, status_checkout;
    TextView header, parking_start_address, parking_start_priceper, parking_start_time;
    TextView parking_timeCount, parking_inprogressPrice, parking_inprogressAddress, parking_maxTime;
    TextView parking_total_time, parking_checkout_address, parking_checkout_price;
    public String zoneId, parking_start_address_text, parking_start_priceper_text, parking_start_time_text;
    Timer T, T2;
    double timeHour, timeHour2;
    double timeMin, timeMin2;
    double timeSec, timeSec2;
    String timeHourString, timeHourString2;
    String timeMinString, timeMinString2;
    String timeSecString, timeSecString2;
    String price;
    double priceDouble;
    double priceDouble2;
    long count = 0;
    long count2 = 0;
    String longitude, latitude;
    Long parking_limit;

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

        parking_start_address = (TextView) parking.findViewById(R.id.parking_start_address);
        parking_start_priceper = (TextView) parking.findViewById(R.id.parking_start_priceper);
        parking_start_time = (TextView) parking.findViewById(R.id.parking_start_time);

        parking_inprogressAddress = (TextView) parking.findViewById(R.id.parking_inprogress_address);
        parking_timeCount = (TextView) parking.findViewById(R.id.parking_timecount);
        parking_inprogressPrice = (TextView) parking.findViewById(R.id.parking_inprogress_price);
        parking_maxTime = (TextView) parking.findViewById(R.id.parking_maxtime);

        parking_checkout_address = (TextView) parking.findViewById(R.id.parking_checkout_address);
        parking_total_time = (TextView) parking.findViewById(R.id.parking_total_time);
        parking_checkout_price = (TextView) parking.findViewById(R.id.parking_checkout_price);

        parking_start_address_text = pref.getString("address", null);
        parking_start_priceper_text = pref.getString("price", null);
        parking_start_time_text = pref.getString("timeLimit", null);

        parking_limit = Long.parseLong(pref.getString("maxTime", null))*60;
        Log.d(TAG, "parkinglimit: "+parking_limit);

        parking_start_address.setText(parking_start_address_text);
        parking_start_priceper.setText(parking_start_priceper_text + " Ft/óra");
        parking_start_time.setText(parking_start_time_text + " perc");

        parking_inprogressAddress.setText(pref.getString("address", null));

        zoneId = pref.getString("zone", null);
        longitude = pref.getString("longitude", "");
        latitude = pref.getString("latitude", "");

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
                //loadJSONStatus(pref.getString(Constants.UID, null));
                loadJSONStatus("F3050076-1CB2-6A54-8AAD-7DF067232155*ABC123");
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
                    //loadJSONStart(pref.getString(Constants.UID, null), zoneId);
                    loadJSONStart("F3050076-1CB2-6A54-8AAD-7DF067232155*ABC123", zoneId, latitude, longitude);
                    //loadJSONStatus(pref.getString(Constants.UID, null));
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
                    //loadJSONStop(pref.getString(Constants.UID, null), zoneId);
                    loadJSONStop("F3050076-1CB2-6A54-8AAD-7DF067232155*ABC123", zoneId);
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

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle("SMS");
                    alertDialog.setMessage("Parkolás kiegyenlítése SMS küldésével");
                    alertDialog.setIcon(R.drawable.ic_parking);

                    alertDialog.setPositiveButton("SMS küldés", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + pref.getString(Constants.SMSBase, null)));
                            intent.putExtra("sms_body", pref.getString(Constants.LicensePlate, null));
                            startActivity(intent);
                        }
                    });

                    alertDialog.setNegativeButton("Mégsem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(getContext(), "Kérjük, ne felejtse el elhozni beutalóját!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    alertDialog.show();

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

    public void loadJSONStart(String userId, String zoneId, String latitude, String longitude) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl(Constants.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterfaceParkingStart requestInterface = retrofit.create(RequestInterfaceParkingStart.class);
        Call<ServerResponse> response = requestInterface.post(userId, zoneId, latitude, longitude);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                if (resp.getAlert() != "") {
                    Toast.makeText(getContext(), resp.getAlert(), Toast.LENGTH_LONG).show();
                    /*Status status = new Status();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.popBackStack();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame, status, "Status")
                            .addToBackStack("Status")
                            .commit();*/
                }
                if (resp.getError() != null) {
                    //Toast.makeText(getContext(), resp.getError().getMessage() + " - " + resp.getError().getMessageDetail(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Hiba a hálózati kapcsolatban. Kérjük, ellenőrizze, hogy csatlakozik-e hálózathoz.", Toast.LENGTH_SHORT).show();
                Log.d(ContentValues.TAG, "No response");
            }
        });

        loadJSONStatus("F3050076-1CB2-6A54-8AAD-7DF067232155*ABC123");
    }

    public void loadJSONStop(String userId, String zoneId) {

        count = 0;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl(Constants.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterfaceParkingStop requestInterface = retrofit.create(RequestInterfaceParkingStop.class);
        Call<ServerResponse> response = requestInterface.post(userId, zoneId);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                if (resp.getAlert() != "") {
                    //Toast.makeText(getContext(), resp.getAlert(), Toast.LENGTH_LONG).show();
                }
                if (resp.getError() != null) {
                    //Toast.makeText(getContext(), resp.getError().getMessage() + " - " + resp.getError().getMessageDetail(), Toast.LENGTH_SHORT).show();
                }
                if (resp.getSum() != null) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("ParkTime", resp.getSum().getTime().toString());
                    Log.d(TAG, "time: "+resp.getSum().getTime().toString());
                    editor.putString("ParkPrice", resp.getSum().getPrice().toString());
                    editor.apply();

                    parking_checkout_address.setText(parking_start_address_text);
                    parking_total_time.setText(String.format("%.0f", Math.ceil(Double.parseDouble(resp.getSum().getTime())))+ " perc");
                    parking_checkout_price.setText(resp.getSum().getPrice()+" Ft");

                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Hiba a hálózati kapcsolatban. Kérjük, ellenőrizze, hogy csatlakozik-e hálózathoz.", Toast.LENGTH_SHORT).show();
                Log.d(ContentValues.TAG, "No response");
            }
        });
    }

    public void loadJSONStatus(String userId) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl(Constants.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterfaceParkingStatus requestInterface = retrofit.create(RequestInterfaceParkingStatus.class);
        Call<ServerResponse> response = requestInterface.post(userId);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                if (resp.getAlert() != "") {
                    Toast.makeText(getContext(), resp.getAlert(), Toast.LENGTH_LONG).show();
                }
                if (resp.getError() != null) {
                    //Toast.makeText(getContext(), resp.getError().getMessage() + " - " + resp.getError().getMessageDetail(), Toast.LENGTH_SHORT).show();
                }
                if (resp.getSum() != null) {
                    price = resp.getZone().getPrice().toString();
                    priceDouble = Double.parseDouble(price);
                    if(T == null){
                        T=new Timer();
                        count = System.currentTimeMillis()/1000-parseLong(resp.getSum().getStart());
                        T.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if(count < 0){
                                        }else {
                                            timeHour = Math.floor(count / 3600);
                                            timeMin = Math.floor((count - timeHour * 3600) / 60);
                                            timeSec = count - timeHour * 3600 - timeMin * 60;
                                            if (timeHour < 10) {
                                                timeHourString = "0" + Integer.toString((int) timeHour);
                                            } else {
                                                timeHourString = Integer.toString((int) timeHour);
                                            }
                                            if (timeMin < 10) {
                                                timeMinString = "0" + Integer.toString((int) timeMin);
                                            } else {
                                                timeMinString = Integer.toString((int) timeMin);
                                            }
                                            if (timeSec < 10) {
                                                timeSecString = "0" + Integer.toString((int) timeSec);
                                            } else {
                                                timeSecString = Integer.toString((int) timeSec);
                                            }
                                            priceDouble2 = Math.ceil(priceDouble * Double.valueOf(Long.toString(count)) / 3600.0);
                                            parking_timeCount.setText(timeHourString + ":" + timeMinString + ":" + timeSecString);
                                            parking_inprogressPrice.setText(Integer.toString((int) priceDouble2) + " Ft");
                                        }
                                        count++;
                                    }
                                });
                            }
                        }, 1000, 1000);
                    }


                    if(T2 == null){
                        T2=new Timer();
                        count2 = parseLong(resp.getSum().getStart())*1000+parking_limit*1000-System.currentTimeMillis();
                        count2 = count2 / 1000;
                        Log.d(TAG, "count: "+count2);
                        T2.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        timeHour2 = Math.floor(count2/3600);
                                        timeMin2 = Math.floor((count2-timeHour2*3600)/60);
                                        timeSec2 = count2-timeHour2*3600-timeMin2*60;
                                        if(timeHour2 < 10){
                                            timeHourString2 = "0"+Integer.toString((int)timeHour2);
                                        }else{
                                            timeHourString2 = Integer.toString((int)timeHour2);
                                        }
                                        if(timeMin2 < 10){
                                            timeMinString2 = "0"+Integer.toString((int)timeMin2);
                                        }else{
                                            timeMinString2 = Integer.toString((int)timeMin2);
                                        }
                                        if(timeSec2 < 10){
                                            timeSecString2 = "0"+Integer.toString((int)timeSec2);
                                        }else{
                                            timeSecString2 = Integer.toString((int)timeSec2);
                                        }
                                        parking_maxTime.setText(timeHourString2 + ":" + timeMinString2 + ":" + timeSecString2);
                                        count2--;
                                    }
                                });
                            }
                        }, 1000, 1000);
                    }
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Hiba a hálózati kapcsolatban. Kérjük, ellenőrizze, hogy csatlakozik-e hálózathoz.", Toast.LENGTH_SHORT).show();
                Log.d(ContentValues.TAG, "No response");
            }
        });
    }

}
