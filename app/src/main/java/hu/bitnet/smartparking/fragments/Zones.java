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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

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
    public ArrayList<Parking_places> data;
    private Context context;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;
    GoogleMap gmap;
    private double latitude, longitude;

    LocationManager locationManager;

    public Zones() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                    getActivity().findViewById(R.id.btn_search).setVisibility(View.VISIBLE);
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

    public void loadJSONSearch(String distance, String latitude, String longitude){

        Log.d(TAG, "zonák");

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
                    data = new ArrayList<Parking_places>(Arrays.asList(resp.getParking_places()));
                    mAdapter = new SearchAdapter(data);
                    zones_rv.setAdapter(mAdapter);

                    mAdapter.setOnItemClickListener(new SearchAdapter.ClickListener(){
                        @Override
                        public void onItemClick(final int position, View v){
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("address", data.get(position).getAddress().toString());
                            editor.putString("zone", data.get(position).getId().toString());
                            editor.putString("price", data.get(position).getPrice().toString());
                            editor.putString("id", data.get(position).getId().toString());
                            editor.putString("latitudeZone", data.get(position).getCenterLatitude().toString());
                            editor.putString("longitudeZone", data.get(position).getCenterLongitude().toString());
                            editor.putString("distance", data.get(position).getDistance().toString());
                            editor.putString("timeLimit", data.get(position).getTimeLimit().toString());
                            editor.putString("time", data.get(position).getTime().toString());
                            editor.putString("freeplaces", data.get(position).getFreePlaces().toString());
                            Log.d(TAG, "free: "+data.get(position).getFreePlaces().toString());
                            editor.putString("click", "yes");
                            editor.apply();

                            double c = Double.parseDouble(data.get(position).getCenterLatitude().toString());
                            double d = Double.parseDouble(data.get(position).getCenterLongitude().toString());

                            ((MainActivity)getActivity()).addMarker(c,d);

                            /*FragmentManager map = getActivity().getSupportFragmentManager();
                            map.beginTransaction()
                                    .replace(R.id.frame, new Map())
                                    .addToBackStack(null)
                                    .commit();*/
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            getActivity().findViewById(R.id.btn_parking_places).setBackgroundResource(R.drawable.button_background);
                            getActivity().findViewById(R.id.btn_myloc).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.btn_search).setVisibility(View.VISIBLE);
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
        loadJSONSearch("100000", Double.toString(latitude), Double.toString(longitude));
        return location;
    }
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
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
