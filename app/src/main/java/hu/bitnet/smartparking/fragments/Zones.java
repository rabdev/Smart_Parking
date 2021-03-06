package hu.bitnet.smartparking.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.Arrays;

import hu.bitnet.smartparking.Adapters.SearchAdapter;
import hu.bitnet.smartparking.MainActivity;
import hu.bitnet.smartparking.R;
import hu.bitnet.smartparking.RequestInterfaces.RequestInterfaceNearest;
import hu.bitnet.smartparking.ServerResponses.ServerResponse;
import hu.bitnet.smartparking.objects.Constants;
import hu.bitnet.smartparking.objects.Parking_places;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Zones extends Fragment {

    SharedPreferences pref;
    RecyclerView zones_rv;
    public SearchAdapter mAdapter;
    public ArrayList<Parking_places> data, data2, data3;
    private Context context;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;
    GoogleMap gmap;
    private double latitude, longitude;
    public Integer prog = 0;
    public Double placeLat, placeLong;

    LocationManager locationManager;

    public Zones() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("prog")){
            prog = getArguments().getInt("prog");
            Log.d(TAG, "van prog");
        }
        if (arguments != null && arguments.containsKey("placeLat")) {
            placeLat = getArguments().getDouble("placeLat");
            Log.d(TAG, "van placelat");
            Log.d(TAG, "placeLat: " + placeLat);
        }
        if (arguments != null && arguments.containsKey("placeLong")) {
            placeLong = getArguments().getDouble("placeLong");
            Log.d(TAG, "van placeLong");
        }

        // Inflate the layout for this fragment
        View zones = inflater.inflate(R.layout.fragment_zones, container, false);
        pref=getActivity().getPreferences(0);

        zones_rv = (RecyclerView) zones.findViewById(R.id.zones_rv);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        zones_rv.setLayoutManager(layoutManager);

        Location location = getLocation();

        if(location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d(TAG, "LAT--Long: "+latitude+" - "+longitude);
        }else{
            Toast.makeText(getContext(), "Engedélyezze eszközén a helymeghatározást!", Toast.LENGTH_LONG).show();
        }

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
                    getActivity().findViewById(R.id.btn_myloc).setVisibility(View.VISIBLE);
                    if(getActivity().findViewById(R.id.menu_layout).getVisibility()!=View.VISIBLE){
                        getActivity().findViewById(R.id.container_up).setVisibility(View.VISIBLE);
                    }
                    getActivity().findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.container_up).setVisibility(View.VISIBLE);
                    if (pref.getString(Constants.ParkingStatus,"").equals("2")){
                        getActivity().findViewById(R.id.btn_inprogress).setVisibility(View.VISIBLE);
                    } else if (pref.getString(Constants.ParkingStatus,"").equals("3")){
                        getActivity().findViewById(R.id.btn_inprogress).setVisibility(View.VISIBLE);
                    } else {
                        getActivity().findViewById(R.id.btn_inprogress).setVisibility(View.GONE);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void loadJSONSearch(String distance, String latitude_inner, String longitude_inner){

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl(Constants.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterfaceNearest requestInterface = retrofit.create(RequestInterfaceNearest.class);
        Call<ServerResponse> response= requestInterface.post(distance, latitude_inner, longitude_inner);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                if(resp.getAlert() != ""){
                    Toast.makeText(getContext(), resp.getAlert(), Toast.LENGTH_LONG).show();
                }
                if(resp.getError() != null){
                    //Toast.makeText(getApplication(), resp.getError().getMessage()+" - "+resp.getError().getMessageDetail(), Toast.LENGTH_SHORT).show();
                    /*SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN,false);
                    editor.apply();
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);*/
                }
                if(resp.getParking_places() != null){
                    data = new ArrayList<Parking_places>(Arrays.asList(resp.getParking_places()));
                    loadJSONData("10000000000", Double.toString(latitude), Double.toString(longitude));
                    //mAdapter = new SearchAdapter(data);
                    //zones_rv.setAdapter(mAdapter);

                    /*mAdapter.setOnItemClickListener(new SearchAdapter.ClickListener(){
                        @Override
                        public void onItemClick(final int position, View v){
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("address", data.get(position).getAddress().toString());
                            editor.putString("zone", data.get(position).getId().toString());
                            editor.putString("price", data.get(position).getPrice().toString());
                            editor.putString("id", data.get(position).getId().toString());
                            editor.putString("latitudeZone", data.get(position).getCenterLatitude().toString());
                            editor.putString("longitudeZone", data.get(position).getCenterLongitude().toString());
                            //editor.putString("distance", data.get(position).getDistance().toString());
                            editor.putString("timeLimit", data.get(position).getTimeLimit().toString());
                            //editor.putString("time", data.get(position).getTime().toString());
                            editor.putString("freeplaces", data.get(position).getFreePlaces().toString());
                            Log.d(TAG, "free: "+data.get(position).getFreePlaces().toString());
                            //editor.putString("click", "yes");
                            editor.apply();

                            double c = Double.parseDouble(data.get(position).getCenterLatitude().toString());
                            double d = Double.parseDouble(data.get(position).getCenterLongitude().toString());

                            getFragmentManager().popBackStack();
                            getActivity().findViewById(R.id.btn_parking_places).setBackgroundResource(R.drawable.button_background);
                            getActivity().findViewById(R.id.btn_myloc).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.container_up).setVisibility(View.VISIBLE);
                            if (pref.getString(Constants.ParkingStatus,"").equals("2")){
                                getActivity().findViewById(R.id.btn_inprogress).setVisibility(View.VISIBLE);
                            } else if (pref.getString(Constants.ParkingStatus,"").equals("3")){
                                getActivity().findViewById(R.id.btn_inprogress).setVisibility(View.VISIBLE);
                            } else {
                                getActivity().findViewById(R.id.btn_inprogress).setVisibility(View.GONE);
                            }

                            ((MainActivity)getActivity()).addMarker(c,d);

                            /*FragmentManager map = getActivity().getSupportFragmentManager();
                            map.beginTransaction()
                                    .replace(R.id.frame, new Map())
                                    .addToBackStack(null)
                                    .commit();*/
                     /*       FragmentManager fm = getActivity().getSupportFragmentManager();
                            getActivity().findViewById(R.id.btn_parking_places).setBackgroundResource(R.drawable.button_background);
                            getActivity().findViewById(R.id.btn_myloc).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.container_up).setVisibility(View.VISIBLE);
                            fm.popBackStack();
                        }

                        @Override
                        public void onItemLongClick(int position, View v) {
                            Log.d(TAG, "onItemLongClick pos = " + position);
                        }
                    });*/
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Hiba a hálózati kapcsolatban. Kérjük, ellenőrizze, hogy csatlakozik-e hálózathoz.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "No response");
            }
        });

    }

    public void loadJSONData(String distance, String latitude, String longitude){

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl(Constants.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterfaceNearest requestInterface = retrofit.create(RequestInterfaceNearest.class);
        Call<ServerResponse> response= requestInterface.post(distance, latitude, longitude);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                if(resp.getAlert() != ""){
                    Toast.makeText(getContext(), resp.getAlert(), Toast.LENGTH_LONG).show();
                }
                if(resp.getError() != null){
                    //Toast.makeText(getApplication(), resp.getError().getMessage()+" - "+resp.getError().getMessageDetail(), Toast.LENGTH_SHORT).show();
                    /*SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN,false);
                    editor.apply();
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);*/
                }
                if(resp.getParking_places() != null){
                    data2 = new ArrayList<Parking_places>(Arrays.asList(resp.getParking_places()));
                    data3 = new ArrayList<Parking_places>(Arrays.asList(resp.getParking_places()));
                    data3.clear();

                    for (int k = 0; k < data.size(); k++){
                        for(int i = 0; i < data2.size(); i++){
                            if(data.get(k).getId().equals(data2.get(i).getId())){
                                Log.d(TAG, "egyezés");
                                data3.add(data2.get(i));
                            }else{
                                Log.d(TAG, "nincs egyezés");
                            }
                        }
                    }
                    mAdapter = new SearchAdapter(data3);
                    zones_rv.setAdapter(mAdapter);

                    mAdapter.setOnItemClickListener(new SearchAdapter.ClickListener(){
                        @Override
                        public void onItemClick(final int position, View v){
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("address", data3.get(position).getAddress().toString());
                            editor.putString("zone", data3.get(position).getId().toString());
                            editor.putString("price", data3.get(position).getPrice().toString());
                            editor.putString("id", data3.get(position).getId().toString());
                            editor.putString("latitudeZone", data3.get(position).getCenterLatitude().toString());
                            editor.putString("longitudeZone", data3.get(position).getCenterLongitude().toString());
                            editor.putString("distance", data3.get(position).getDistance().toString());
                            editor.putString("timeLimit", data3.get(position).getTimeLimit().toString());
                            editor.putString("time", data3.get(position).getTime().toString());
                            editor.putString("freeplaces", data3.get(position).getFreePlaces().toString());
                            Log.d(TAG, "free: "+data3.get(position).getFreePlaces().toString());
                            //editor.putString("click", "yes");
                            editor.apply();

                            double c = Double.parseDouble(data3.get(position).getCenterLatitude().toString());
                            double d = Double.parseDouble(data3.get(position).getCenterLongitude().toString());

                            getFragmentManager().popBackStack();
                            getActivity().findViewById(R.id.btn_parking_places).setBackgroundResource(R.drawable.button_background);
                            getActivity().findViewById(R.id.btn_myloc).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.container_up).setVisibility(View.VISIBLE);
                            if (pref.getString(Constants.ParkingStatus,"").equals("2")){
                                getActivity().findViewById(R.id.btn_inprogress).setVisibility(View.VISIBLE);
                            } else if (pref.getString(Constants.ParkingStatus,"").equals("3")){
                                getActivity().findViewById(R.id.btn_inprogress).setVisibility(View.VISIBLE);
                            } else {
                                getActivity().findViewById(R.id.btn_inprogress).setVisibility(View.GONE);
                            }

                            ((MainActivity)getActivity()).addMarker(c,d);

                            /*FragmentManager map = getActivity().getSupportFragmentManager();
                            map.beginTransaction()
                                    .replace(R.id.frame, new Map())
                                    .addToBackStack(null)
                                    .commit();*/
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            getActivity().findViewById(R.id.btn_parking_places).setBackgroundResource(R.drawable.button_background);
                            getActivity().findViewById(R.id.btn_myloc).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.container_up).setVisibility(View.VISIBLE);
                            fm.popBackStack();
                        }

                        @Override
                        public void onItemLongClick(int position, View v) {
                            Log.d(TAG, "onItemLongClick pos = " + position);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Hiba a hálózati kapcsolatban. Kérjük, ellenőrizze, hogy csatlakozik-e hálózathoz.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "No response");
            }
        });

    }

    private Location getLocation() {
        // TODO Auto-generated method stub
        try {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {
                this.canGetLocation = true;

                if (isNetworkEnabled) {

                    if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Log.d(TAG, "nagy nulla");
                        return null;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 3, locationListener);

                    if (locationManager != null){
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null){
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.d(TAG, "LAT--Long2: "+latitude+" - "+longitude);
                        }
                    }
                }

                if (isGPSEnabled){
                    if (location == null){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 3, locationListener);
                        if (locationManager != null){
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null){
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.d(TAG, "LAT--Long3: "+latitude+" - "+longitude);
                            }
                        }
                    }
                } else {
                    //showAlertDialog();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, latitude+","+longitude);
        /*placeLat = pref.getString("placeLat", null);
        placeLong = pref.getString("placeLong", null);*/
        if(prog != 0) {
            if(placeLat != null && placeLong != null){
                Log.d(TAG, "vagy itt");
                loadJSONSearch(String.valueOf(prog), Double.toString(placeLat), Double.toString(placeLong));
                //loadJSONData(pref.getString(Constants.SettingsDistance, "0"), Double.toString(latitude), Double.toString(longitude));
            }else {
                Log.d(TAG, "prog van");
                loadJSONSearch(String.valueOf(prog), Double.toString(latitude), Double.toString(longitude));
                //loadJSONData(pref.getString(Constants.SettingsDistance, "0"), Double.toString(latitude), Double.toString(longitude));
            }
        }else{
            if(placeLat != null && placeLong != null) {
                Log.d(TAG, "itt kéne lenni");
                loadJSONSearch(pref.getString(Constants.SettingsDistance, "0"), Double.toString(placeLat), Double.toString(placeLong));
                //loadJSONData(pref.getString(Constants.SettingsDistance, "0"), Double.toString(latitude), Double.toString(longitude));
            }else {
                Log.d(TAG, "prog nincs");
                loadJSONSearch(pref.getString(Constants.SettingsDistance, "0"), Double.toString(latitude), Double.toString(longitude));
                //loadJSONData(pref.getString(Constants.SettingsDistance, "0"), Double.toString(latitude), Double.toString(longitude));
            }
        }
        return location;
    }
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d(TAG, "LAT--Long4: "+latitude+" - "+longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}
