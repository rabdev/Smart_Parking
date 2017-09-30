package hu.bitnet.smartparking;

import android.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, LocationSource.OnLocationChangedListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public final static int MILLISECONDS_PER_SECOND = 1000;
    public final static int MINUTE = 60 * MILLISECONDS_PER_SECOND;

    SharedPreferences pref;
    GoogleMap gmap;
    MapView mapView;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location location;
    android.location.LocationListener locationlistener;
    LinearLayout infosav, menu, distance_container;
    ImageView settings, collapse, hb_menu, distance;
    AppCompatButton history, parkingplaces;
    EditText search;
    Animation slide_up, slide_up1, slide_up2, slide_down, slide_down2;
    boolean x;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getPreferences(0);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_ASK_PERMISSIONS);


        }


        infosav = (LinearLayout) findViewById(R.id.infosav);
        menu = (LinearLayout) findViewById(R.id.menu_layout);
        distance_container = (LinearLayout) findViewById(R.id.distance_container);
        settings = (ImageView) findViewById(R.id.btn_settings);
        collapse = (ImageView) findViewById(R.id.btn_collapse);
        hb_menu = (ImageView) findViewById(R.id.hb_menu);
        distance = (ImageView) findViewById(R.id.distance);
        history = (AppCompatButton) findViewById(R.id.btn_history);
        parkingplaces = (AppCompatButton) findViewById(R.id.btn_parking_places);
        search= (EditText) findViewById(R.id.search);

        menu.setVisibility(View.GONE);
        distance_container.setVisibility(View.GONE);
        x = false;

        slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);

        slide_down2 = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);

        slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);

        slide_up1 = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);

        slide_up2 = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);


        hb_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.setVisibility(View.VISIBLE);
                menu.startAnimation(slide_up);
                slide_up.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        infosav.startAnimation(slide_down);
                        infosav.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                collapse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        infosav.setVisibility(View.VISIBLE);
                        infosav.startAnimation(slide_up1);
                        menu.startAnimation(slide_down);
                        menu.setVisibility(View.GONE);
                    }
                });
            }
        });

        distance.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (x==false) {
                    distance_container.setVisibility(View.VISIBLE);
                    distance.setColorFilter(getResources().getColor(R.color.colorPurple, getTheme()));
                    distance_container.startAnimation(slide_up2);
                    x=true;
                } else {
                    distance_container.setVisibility(View.GONE);
                    distance.setColorFilter(R.color.colorPrimaryDark);
                    distance_container.startAnimation(slide_down2);
                    x=false;
                }

            }
        });

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
        mGoogleApiClient.connect();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MINUTE);
        mLocationRequest.setFastestInterval(15 * MILLISECONDS_PER_SECOND);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location loc) {
        double latitude = loc.getLatitude();
        double longitude = loc.getLongitude();
        location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMyLocationEnabled(true);
        gmap.getUiSettings().setMyLocationButtonEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

        locationlistener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                double latitude = loc.getLatitude();
                double longitude = loc.getLongitude();
                location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                return;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String bestProvider) {
                LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, (android.location.LocationListener) locationlistener);
                } else {
                    locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) locationlistener);
                }
            }

            @Override
            public void onProviderDisabled(String s) {
                LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) locationlistener);
                } else {
                    locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) locationlistener);
                }
            }
        };
        location = locationManager.getLastKnownLocation(bestProvider);
        if (location == null) {
            locationManager.requestLocationUpdates(bestProvider, 0, 0, (android.location.LocationListener) locationlistener);
            return;
        } else {
            double c = location.getLatitude();
            double d = location.getLongitude();
            LatLng myloc = new LatLng(c, d);
            gmap.animateCamera(CameraUpdateFactory.newLatLng(myloc));
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 16));
        }
    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }
}
