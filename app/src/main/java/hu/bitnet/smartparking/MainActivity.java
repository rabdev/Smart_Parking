package hu.bitnet.smartparking;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.TransitionDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.bitnet.smartparking.RequestInterfaces.RequestInterfaceNearest;
import hu.bitnet.smartparking.ServerResponses.ServerResponse;
import hu.bitnet.smartparking.fragments.History;
import hu.bitnet.smartparking.fragments.Parking;
import hu.bitnet.smartparking.fragments.Search;
import hu.bitnet.smartparking.fragments.SearchZones;
import hu.bitnet.smartparking.fragments.Zones;
import hu.bitnet.smartparking.objects.Constants;
import hu.bitnet.smartparking.objects.Parking_places;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.content.ContentValues.TAG;
import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, LocationSource.OnLocationChangedListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public final static int MILLISECONDS_PER_SECOND = 1000;
    public final static int MINUTE = 60 * MILLISECONDS_PER_SECOND;

    SharedPreferences pref;
    AlertDialog settings_dialog;
    GoogleMap gmap;
    MapView mapView;
    public GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location location;
    LatLng position;
    android.location.LocationListener locationlistener;
    LinearLayout infosav, menu, distance_container, distance, distance_bg, parking_card, container_up
            ;
    ImageView settings, collapse, hb_menu, btn_search, btn_navigate, btn_myloc, inprogress;
    AppCompatButton history, parkingplaces;
    TextView firstrun, tv_distance, et_distance, indistance, tv_sb_distance, parkingcount, card_count, card_address, card_perprice, distance_km, distance_mins;
    EditText search, et_license_plate, et_name, et_smsbase, upsearch;
    SeekBar settings_distance, sb_distance;
    Animation slide_up, slide_up1, slide_up2, slide_down, slide_down2;
    TransitionDrawable transition;
    boolean x, bool_license, bool_distance, bool_smsbase, search_active;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    public int index, prog;
    public ArrayList<Parking_places> data;
    double latitude,z, latitude1;
    double longitude,y, longitude1;
    private String search_text;
    InputMethodManager imm;
    public boolean parking_card_bool;
    private Marker marker;
    public PendingIntent pendingIntent;
    CardView cardView;
    public Double placeLat;
    public Double placeLong;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getPreferences(0);
        x = false;
        search_active=false;
        parking_card_bool = false;

        Intent intent2 = getIntent();
        if(intent2 != null) {
            String message = intent2.getStringExtra("activity");
            if(message != null && message.equals("driving")){
                if(foregrounded() == true){
                    showDialog2();
                }
            }else{
                Log.d(TAG, "ACTIVITY: not driving");
            }
        }

        /*SharedPreferences.Editor editor = pref.edit();
        editor.putString(Constants.LicensePlate,"");
        editor.putString(Constants.SMSBase,"");
        editor.putString(Constants.SettingsDistance,"");
        editor.putString(Constants.NAME,"");
        editor.apply();*/

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }


        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        }


        infosav = (LinearLayout) findViewById(R.id.infosav);
        menu = (LinearLayout) findViewById(R.id.menu_layout);
        distance_container = (LinearLayout) findViewById(R.id.distance_container);
        distance = (LinearLayout) findViewById(R.id.distance);
        distance_bg = (LinearLayout) findViewById(R.id.distance_bg);
        parking_card= (LinearLayout) findViewById(R.id.parking_card);
        container_up = (LinearLayout) findViewById(R.id.container_up);
        settings = (ImageView) findViewById(R.id.btn_settings);
        collapse = (ImageView) findViewById(R.id.btn_collapse);
        hb_menu = (ImageView) findViewById(R.id.hb_menu);
        btn_search = (ImageView) findViewById(R.id.btn_search);
        btn_navigate= (ImageView) findViewById(R.id.btn_navigate);
        inprogress = (ImageView) findViewById(R.id.btn_inprogress);
        btn_myloc = (ImageView) findViewById(R.id.btn_myloc);
        history = (AppCompatButton) findViewById(R.id.btn_history);
        parkingplaces = (AppCompatButton) findViewById(R.id.btn_parking_places);
        search = (EditText) findViewById(R.id.search);
        upsearch = (EditText) findViewById(R.id.upsearch);
        tv_distance = (TextView) findViewById(R.id.tv_distance);
        indistance = (TextView) findViewById(R.id.indistance);
        tv_sb_distance = (TextView) findViewById(R.id.tv_sb_distance);
        sb_distance = (SeekBar) findViewById(R.id.sb_distance);
        parkingcount = (TextView) findViewById(R.id.parkingcount);
        card_count = (TextView) findViewById(R.id.card_count);
        card_address = (TextView) findViewById(R.id.card_address);
        card_perprice = (TextView) findViewById(R.id.card_perprice);
        distance_km = (TextView) findViewById(R.id.distance_km);
        distance_mins = (TextView) findViewById(R.id.distance_mins);
        cardView= (CardView) findViewById(R.id.card_view);


        menu.setVisibility(View.GONE);
        distance_container.setVisibility(View.GONE);
        parking_card.setVisibility(View.GONE);
        btn_navigate.setVisibility(View.GONE);
        upsearch.setVisibility(View.GONE);

        ParkinginProgress();


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

        if (pref.getString(Constants.LicensePlate, "").isEmpty() || pref.getString(Constants.SettingsDistance, "").isEmpty() || pref.getString(Constants.SMSBase, "").isEmpty()) {
            bool_distance = false;
            bool_license = false;
            bool_smsbase = false;
            showDialog();
            firstrun.setVisibility(View.VISIBLE);
            settings_dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        finish();
                        x = false;
                        settings_dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            settings_dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            settings_dialog.setCanceledOnTouchOutside(false);
        }

        indistance.setText(pref.getString(Constants.SettingsDistance, null) + " m-es körzetben");
        tv_distance.setText(pref.getString(Constants.SettingsDistance, null));

        hb_menu.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (x) {
                    distance_container.setVisibility(View.GONE);
                    //parking_card.setVisibility(View.GONE);
                    distance_bg.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
                    tv_distance.setTextColor(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
                    //distance_bg.setColo(getResources().getColor(R.color.colorPrimaryDark,getTheme()));
                    x = false;
                } else if (distance_container.getVisibility()==View.VISIBLE){
                    distance_container.setVisibility(View.GONE);
                    //parking_card.setVisibility(View.GONE);
                    distance_bg.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
                    tv_distance.setTextColor(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
                    //distance_bg.setColo(getResources().getColor(R.color.colorPrimaryDark,getTheme()));
                    x = false;
                }
                if (container_up.getVisibility()==View.VISIBLE){
                    container_up.setVisibility(View.GONE);
                }
                parking_card.setVisibility(View.GONE);
                menu.setVisibility(View.VISIBLE);
                menu.startAnimation(slide_up);
                x = true;
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
                        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        infosav.setVisibility(View.VISIBLE);
                        infosav.startAnimation(slide_up1);
                        menu.startAnimation(slide_down);
                        menu.setVisibility(View.GONE);
                        x = false;
                    }
                });
                settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog();
                        x = true;
                        if (pref.getString(Constants.LicensePlate,null)!=null) {
                            et_license_plate.setText(pref.getString(Constants.LicensePlate, null));
                        }
                        if (pref.getString(Constants.SMSBase,null)!=null) {
                            et_smsbase.setText(pref.getString(Constants.SMSBase, null));
                        }
                        if(pref.getString(Constants.SettingsDistance,null)!=null) {
                            et_distance.setText(pref.getString(Constants.SettingsDistance, null));
                        }
                        if (!pref.getString(Constants.NAME, "").isEmpty()) {
                            et_name.setText(pref.getString(Constants.NAME, null));
                        }
                    }
                });
            }
        });

        distance.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (!x) {
                    distance_container.setVisibility(View.VISIBLE);
                    if(parking_card.getVisibility()==View.VISIBLE){
                        parking_card.setVisibility(View.GONE);
                    }
                    distance_bg.setBackgroundTintList(getResources().getColorStateList(R.color.colorPurple, getTheme()));
                    tv_distance.setTextColor(getResources().getColorStateList(R.color.colorPurple, getTheme()));
                    distance_container.startAnimation(slide_up2);
                    tv_sb_distance.setText(pref.getString(Constants.SettingsDistance, null) + " m");
                    sb_distance.setMax(500);
                    prog = parseInt(pref.getString(Constants.SettingsDistance, null));
                    sb_distance.setProgress(prog);
                    sb_distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            prog = sb_distance.getProgress()+50;
                            tv_sb_distance.setText(String.valueOf(prog) + " m");
                            if(prog != 0){
                                indistance.setText(prog + " m-es körzetben");
                            }else{
                                indistance.setText(pref.getString(Constants.SettingsDistance, null) + " m-es körzetben");
                            }
                            loadJSON(Double.toString(latitude1), Double.toString(longitude1), String.valueOf(prog));
                            Zones zones = (Zones)getSupportFragmentManager().findFragmentByTag("Zones");
                            if (zones != null && zones.isVisible()) {
                                if(placeLat != null){
                                    zones.loadJSONSearch(String.valueOf(prog), String.valueOf(placeLat), String.valueOf(placeLong));
                                }else {
                                    zones.loadJSONSearch(String.valueOf(prog), Double.toString(latitude1), Double.toString(longitude1));
                                }
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    x = true;
                } else {
                    distance_container.setVisibility(View.GONE);
                    if(parking_card.getVisibility()==View.VISIBLE){
                        parking_card.setVisibility(View.GONE);
                    }
                    //parking_card.setVisibility(View.GONE);
                    distance_bg.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
                    tv_distance.setTextColor(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
                    distance_container.startAnimation(slide_down2);
                    x = false;
                }

            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                History history1 = new History();
                parkingplaces.setBackgroundResource(R.drawable.button_background);
                history.setBackgroundResource(R.drawable.button_background_active);
                FragmentManager fragmentManager = getSupportFragmentManager();
                index = fragmentManager.getBackStackEntryCount();
                if (index != 0) {
                    fragmentManager.popBackStack();
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.mapView, history1, history1.getTag())
                        .addToBackStack("History")
                        .commit();
                infosav.setVisibility(View.VISIBLE);
                infosav.startAnimation(slide_up1);
                menu.startAnimation(slide_down);
                menu.setVisibility(View.GONE);
                btn_myloc.setVisibility(View.GONE);
                btn_search.setVisibility(View.GONE);
                btn_navigate.setVisibility(View.GONE);
                inprogress.setVisibility(View.GONE);
                upsearch.setVisibility(View.GONE);
                cardView.setVisibility(View.GONE);
                x = false;
            }
        });
        parkingplaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Zones zones = new Zones();
                Bundle b = new Bundle();
                if(prog != 0){
                    b.putInt("prog", prog);
                    if(placeLong != null && placeLat != null){
                        Log.d(TAG, "beteszem");
                        b.putDouble("placeLat", placeLat);
                        b.putDouble("placeLong", placeLong);
                    }
                    zones.setArguments(b);
                }else {
                    if (placeLong != null && placeLat != null) {
                        Log.d(TAG, "progot meg nem");
                        b.putDouble("placeLat", placeLat);
                        b.putDouble("placeLong", placeLong);
                        zones.setArguments(b);
                    }
                }
                history.setBackgroundResource(R.drawable.button_background);
                parkingplaces.setBackgroundResource(R.drawable.button_background_active);
                FragmentManager fragmentManager = getSupportFragmentManager();
                index = fragmentManager.getBackStackEntryCount();
                if (index != 0) {
                    fragmentManager.popBackStack();
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.mapView, zones, "Zones")
                        .addToBackStack("Zones")
                        .commit();
                infosav.setVisibility(View.VISIBLE);
                infosav.startAnimation(slide_up1);
                menu.startAnimation(slide_down);
                menu.setVisibility(View.GONE);
                btn_myloc.setVisibility(View.GONE);
                btn_search.setVisibility(View.GONE);
                btn_navigate.setVisibility(View.GONE);
                inprogress.setVisibility(View.GONE);
                upsearch.setVisibility(View.GONE);
                cardView.setVisibility(View.GONE);
                x = false;
            }
        });

        /*search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getX() <= (search.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                        // your action here

                        Log.d(TAG, "keresés");

                        search_text = search.getText().toString();
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("address",search_text);
                        editor.apply();

                        SearchZones searchZones = new SearchZones();
                        history.setBackgroundResource(R.drawable.button_background);
                        parkingplaces.setBackgroundResource(R.drawable.button_background);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        index = fragmentManager.getBackStackEntryCount();
                        if (index != 0) {
                            fragmentManager.popBackStack();
                        }
                        fragmentManager.beginTransaction()
                                .add(R.id.mapView, searchZones, searchZones.getTag())
                                .addToBackStack("Zones")
                                .commit();
                        Log.d(TAG, "SearchZones");
                        infosav.setVisibility(View.VISIBLE);
                        infosav.startAnimation(slide_up1);
                        menu.startAnimation(slide_down);
                        menu.setVisibility(View.GONE);
                        btn_myloc.setVisibility(View.GONE);
                        btn_search.setVisibility(View.GONE);
                        btn_navigate.setVisibility(View.GONE);
                        inprogress.setVisibility(View.GONE);
                        upsearch.setVisibility(View.GONE);
                        cardView.setVisibility(View.GONE);
                        x = false;
                    }
                }
                return false;
            }
        });*/

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        if (search.isActivated()){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search_text = search.getText().toString();
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("address",search_text);
                    editor.apply();
                    container_up.setVisibility(View.VISIBLE);
                    SearchZones searchZones = new SearchZones();
                    history.setBackgroundResource(R.drawable.button_background);
                    parkingplaces.setBackgroundResource(R.drawable.button_background);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    index = fragmentManager.getBackStackEntryCount();
                    if (index != 0) {
                        fragmentManager.popBackStack();
                    }
                    fragmentManager.beginTransaction()
                            .add(R.id.mapView, searchZones, searchZones.getTag())
                            .addToBackStack("Zones")
                            .commit();
                    Log.d(TAG, "SearchZones");
                    infosav.setVisibility(View.VISIBLE);
                    infosav.startAnimation(slide_up1);
                    menu.startAnimation(slide_down);
                    menu.setVisibility(View.GONE);
                    btn_myloc.setVisibility(View.GONE);
                    btn_search.setVisibility(View.GONE);
                    btn_navigate.setVisibility(View.GONE);
                    inprogress.setVisibility(View.GONE);
                    upsearch.setVisibility(View.GONE);
                    cardView.setVisibility(View.GONE);
                    x = false;
                    return true;
                }
                return false;
            }
        });

        btn_myloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (location != null) { // Check to ensure coordinates aren't null, probably a better way of doing this...
                    double c = location.getLatitude();
                    double d = location.getLongitude();
                    LatLng myloc = new LatLng(c, d);
                    gmap.animateCamera(CameraUpdateFactory.newLatLng(myloc));
                    gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 16));
                }
            }
        });

        parking_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parking_card.setVisibility(View.GONE);
                pref.getString("zone", null);
                Log.d(TAG, "zoneID: "+pref.getString("zone", null));
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("longitude", String.valueOf(longitude));
                editor.putString("latitude", String.valueOf(latitude));
                editor.apply();
                Parking parking = new Parking();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .add(R.id.mapView, parking, parking.getTag())
                        .addToBackStack("Parking")
                        .commit();
                btn_myloc.setVisibility(View.GONE);
                btn_navigate.setVisibility(View.GONE);
                upsearch.setVisibility(View.GONE);
                x=false;
            }
        });

        /*indistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parking_card.setVisibility(View.GONE);
                x=false;
                btn_navigate.setVisibility(View.GONE);
                Parking parking = new Parking();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .add(R.id.mapView, parking, parking.getTag())
                        .addToBackStack("Parking")
                        .commit();
                btn_myloc.setVisibility(View.GONE);
                upsearch.setVisibility(View.GONE);
            }
        });*/

        /*PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());

                String placeDetailsStr = place.getName() + "\n"
                        + place.getId() + "\n"
                        + place.getLatLng().toString() + "\n"
                        + place.getAddress() + "\n"
                        + place.getAttributions();
                Toast.makeText(getApplicationContext(), placeDetailsStr, Toast.LENGTH_LONG).show();
                //.setText(placeDetailsStr);
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });*/

        inprogress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    parking_card.setVisibility(View.GONE);
                    x = false;
                    inprogress.setVisibility(View.GONE);
                    btn_navigate.setVisibility(View.GONE);

                    Parking parking = new Parking();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .add(R.id.mapView, parking, parking.getTag())
                            .addToBackStack("Parking")
                            .commit();
                    btn_myloc.setVisibility(View.GONE);
                    upsearch.setVisibility(View.GONE);
            };
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

        upsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (upsearch.getText().toString().trim().length() >= 3) {
                    if (search_active == false) {
                        Search search = new Search();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        index = fragmentManager.getBackStackEntryCount();
                        if (index != 0) {
                            fragmentManager.popBackStack();
                        }
                        fragmentManager.beginTransaction()
                                .add(R.id.mapView, search, search.getTag())
                                .addToBackStack("Search")
                                .commit();
                        upsearch.setActivated(true);
                        upsearch.hasFocus();
                        upsearch.requestFocus();
                        search_active = true;
                        x=true;
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (upsearch.getText().toString().trim().length() == 3) {
                    /*PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                            getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

                    autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                        @Override
                        public void onPlaceSelected(Place place) {
                            // TODO: Get info about the selected place.
                            Log.i(TAG, "Place: " + place.getName());

                            String placeDetailsStr = place.getName() + "\n"
                                    + place.getId() + "\n"
                                    + place.getLatLng().toString() + "\n"
                                    + place.getAddress() + "\n"
                                    + place.getAttributions();
                            upsearch.setText(placeDetailsStr);
                        }

                        @Override
                        public void onError(Status status) {
                            // TODO: Handle the error.
                            Log.i(TAG, "An error occurred: " + status);
                        }
                    });
                    /*if (search_active==false){
                        Search search = new Search();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .add(R.id.mapView, search, search.getTag())
                                .addToBackStack("Search")
                                .commit();
                        upsearch.setActivated(true);
                        upsearch.hasFocus();
                        upsearch.requestFocus();
                        search_active=true;
                        x=true;
                    }*/
                } else if(upsearch.getText().toString().trim().length() < 3) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    index = fragmentManager.getBackStackEntryCount();
                    if (index != 0) {
                        fragmentManager.popBackStackImmediate();
                    }
                    upsearch.setActivated(true);
                    upsearch.hasFocus();
                    upsearch.requestFocus();
                    search_active=false;
                    x=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (upsearch.getVisibility()!= View.VISIBLE){
                    upsearch.setVisibility(View.VISIBLE);
                    upsearch.setActivated(true);
                    upsearch.requestFocus();
                    imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(upsearch, InputMethodManager.SHOW_IMPLICIT);
                }*/
            }
        });
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());

                String placeDetailsStr = place.getName() + "\n"
                        + place.getId() + "\n"
                        + place.getLatLng().toString() + "\n"
                        + place.getAddress() + "\n"
                        + place.getAttributions();

                placeLat = place.getLatLng().latitude;
                placeLong = place.getLatLng().longitude;

                /*SharedPreferences.Editor editor = pref.edit();
                editor.putString("placeLat", String.valueOf(placeLat));
                editor.putString("placeLong", String.valueOf(placeLong));
                editor.apply();*/

                String placeNameTitle = place.getName() + "\n"
                        + place.getAddress();

                if(prog != 0) {
                    loadJSON(String.valueOf(place.getLatLng().latitude), String.valueOf(place.getLatLng().longitude), String.valueOf(prog));
                    marker = gmap.addMarker(new MarkerOptions()
                            .position(place.getLatLng())
                            .title(placeNameTitle));
                    gmap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                    gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));
                    marker.showInfoWindow();
                }else{
                    loadJSON(String.valueOf(place.getLatLng().latitude), String.valueOf(place.getLatLng().longitude), pref.getString(Constants.SettingsDistance, "0"));
                    marker = gmap.addMarker(new MarkerOptions()
                            .position(place.getLatLng())
                            .title(placeNameTitle));
                    gmap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                    gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));
                    marker.showInfoWindow();
                }

                btn_navigate.setVisibility(View.VISIBLE);
                Log.d(TAG, "itt");
                btn_navigate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String uri = String.format("http://maps.google.com/maps?" + "saddr="+latitude1+","+longitude1+ "&daddr="+marker.getPosition().latitude+","+marker.getPosition().longitude+"");
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(intent);
                    }
                });
                //Toast.makeText(getApplicationContext(), placeDetailsStr, Toast.LENGTH_LONG).show();
                //.setText(placeDetailsStr);
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(foregrounded() == true) {
            Intent intent = new Intent(this, DetectActivity.class);
            pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 3000, pendingIntent);
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
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
        latitude1 = latitude;
        longitude1 = longitude;
        location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        //nincs location change figyelés
        /*if(Constants.SettingsDistance != null) {
            if(prog != 0){
                loadJSON(Double.toString(latitude), Double.toString(longitude), String.valueOf(prog));
            }else {
                loadJSON(Double.toString(latitude), Double.toString(longitude), pref.getString(Constants.SettingsDistance, "0"));
            }
        }*/
        return;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;

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
                        return;
                    }
                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) locationlistener);
                } else {
                    locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) locationlistener);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(bestProvider);


            gmap.setMyLocationEnabled(true);
            gmap.getUiSettings().setMyLocationButtonEnabled(false);
            gmap.getUiSettings().setMapToolbarEnabled(false);
            gmap.setInfoWindowAdapter(new MyInfoWindowAdapter());



            if (location == null) {
                locationManager.requestLocationUpdates(bestProvider, 0, 0, (android.location.LocationListener) locationlistener);
                return;
            } else {
                double c = location.getLatitude();
                double d = location.getLongitude();
                LatLng myloc = new LatLng(c, d);
                gmap.animateCamera(CameraUpdateFactory.newLatLng(myloc));
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 16));
                if(Constants.SettingsDistance != null) {
                    /*if(pref.getString("noLoad", null) != null){
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("noLoad", null);
                        editor.apply();
                        addMarker(Double.parseDouble(pref.getString("latitudeZone", null)), Double.parseDouble(pref.getString("longitudeZone", null)));
                    }else {*/
                        if (prog != 0) {
                            loadJSON(Double.toString(c), Double.toString(d), String.valueOf(prog));
                        } else {
                            loadJSON(Double.toString(c), Double.toString(d), pref.getString(Constants.SettingsDistance, "0"));
                        }
                    //}
                }
            }
        }



    }

    @Override
    public void onResume() {
        mapView.onResume();
        ParkinginProgress();
        super.onResume();
    }

    public boolean foregrounded() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    private void showDialog() {
        infosav.setVisibility(View.VISIBLE);
        infosav.startAnimation(slide_up1);
        menu.startAnimation(slide_down);
        menu.setVisibility(View.GONE);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (LayoutInflater.from(getApplicationContext()));
        View dialogview = inflater.inflate(R.layout.dialog_settings, null);
        settings_distance = (SeekBar) dialogview.findViewById(R.id.settings_distance);
        et_license_plate = (EditText) dialogview.findViewById(R.id.license_plate);
        et_name = (EditText) dialogview.findViewById(R.id.name);
        et_smsbase = (EditText) dialogview.findViewById(R.id.sms_base);
        et_distance = (TextView) dialogview.findViewById(R.id.et_distance);
        firstrun = (TextView) dialogview.findViewById(R.id.tv_firstrun);
        firstrun.setVisibility(View.GONE);

        et_name.clearFocus();

        et_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    et_name.setHint("");
                else
                    et_name.setHint("Kovács András");
            }
        });

        if (pref.getString(Constants.LicensePlate,null)!=null) {
            et_license_plate.setText(pref.getString(Constants.LicensePlate, null));
            bool_license = true;
        }
        if (pref.getString(Constants.SMSBase,null)!=null) {
            et_smsbase.setText(pref.getString(Constants.SMSBase, null));
            bool_smsbase = true;
        }
        if(pref.getString(Constants.SettingsDistance,null)!=null) {
            et_distance.setText(pref.getString(Constants.SettingsDistance, null));
            bool_distance = true;
        }
        if (!pref.getString(Constants.NAME, "").isEmpty()) {
            et_name.setText(pref.getString(Constants.NAME, null));
        }

        settings_distance.setMax(450);
        if (pref.getString(Constants.SettingsDistance, null) != null) {
            if (!pref.getString(Constants.SettingsDistance, null).isEmpty()) {
                prog = parseInt(pref.getString(Constants.SettingsDistance, null));
                settings_distance.setProgress(prog);
                bool_distance = true;
                et_distance.setText(pref.getString(Constants.SettingsDistance, null));
            }
        } else {
            prog = 50;
            bool_distance = true;
            settings_distance.setProgress(prog);
            et_distance.setText(String.valueOf(prog));
        }

        settings_distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prog = settings_distance.getProgress()+50;
                if (prog != 0) {
                    bool_distance = true;
                    if (bool_smsbase == true && bool_license == true && bool_distance == true) {
                        settings_dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    }
                } else {
                    bool_distance = false;
                    settings_dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
                et_distance.setText(String.valueOf(prog));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        et_smsbase.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_smsbase.getText().toString().trim().length() > 0) {
                    bool_smsbase = true;
                    if (bool_smsbase == true && bool_license == true && bool_distance == true) {
                        settings_dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    }
                } else {
                    bool_smsbase = false;
                    settings_dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        et_license_plate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_license_plate.getText().toString().trim().length() > 0) {
                    bool_license = true;
                    if (bool_smsbase == true && bool_license == true && bool_distance == true) {
                        settings_dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    }
                } else {
                    bool_license = false;
                    settings_dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        builder.setView(dialogview);
        builder.setTitle("Beállítások");
        builder.setPositiveButton("Mentés", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Pattern p = Pattern.compile("^[a-zA-Z]{3}[-]{1}[0-9]{3}$");
                Matcher m = p.matcher(et_license_plate.getText());
                if(m.matches()) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Constants.LicensePlate, et_license_plate.getText().toString());
                    editor.putString(Constants.SMSBase, et_smsbase.getText().toString());
                    editor.putString(Constants.SettingsDistance, et_distance.getText().toString());
                    editor.putString(Constants.NAME, et_name.getText().toString());
                    Log.i("TAG","android.os.Build.SERIAL: " + Build.SERIAL);
                    editor.putString(Constants.UID, Build.SERIAL+"*"+pref.getString(Constants.LicensePlate, null));
                    editor.apply();
                    tv_distance.setText(pref.getString(Constants.SettingsDistance, null));
                    if(prog != 0){
                        indistance.setText(prog + " m-es körzetben");
                    }else{
                        indistance.setText(pref.getString(Constants.SettingsDistance, null) + " m-es körzetben");
                    }
                    x = false;
                    finish();
                    startActivity(getIntent());
                }else{
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Constants.SMSBase, et_smsbase.getText().toString());
                    editor.putString(Constants.SettingsDistance, et_distance.getText().toString());
                    editor.putString(Constants.NAME, et_name.getText().toString());
                    Log.i("TAG","android.os.Build.SERIAL: " + Build.SERIAL);
                    editor.putString(Constants.UID, Build.SERIAL+"*"+pref.getString(Constants.LicensePlate, null));
                    editor.apply();
                    showDialog3();
                    if(pref.getString(Constants.LicensePlate, null) == null) {
                        settings_dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    finish();
                                    x = false;
                                    settings_dialog.dismiss();
                                    return true;
                                }
                                return false;
                            }
                        });
                    }

                    /*builder.setNegativeButton("Csak navigációt használok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
                        }
                    });*/
                }
            }
        });

        builder.setNegativeButton("Mégse", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (et_license_plate.getText().toString().isEmpty() && et_distance.getText().toString().isEmpty() && et_smsbase.getText().toString().isEmpty()) {
                    MainActivity.super.onBackPressed();
                } else {
                    if (x) {
                        menu.setVisibility(View.VISIBLE);
                        menu.startAnimation(slide_up);
                        x = false;
                    } else {
                        if(Constants.LicensePlate.equals("LicensePlate") || Constants.SMSBase.equals("SMSBase") || Constants.SettingsDistance.equals("Distance")){
                            MainActivity.super.onBackPressed();
                        }
                        //
                    }
                    dialog.dismiss();
                }
            }
        });
        settings_dialog = builder.create();
        settings_dialog.show();
    }

    private void showDialog2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (LayoutInflater.from(getApplicationContext()));
        View dialogview = inflater.inflate(R.layout.dialog_alert, null);

        builder.setView(dialogview);
        builder.setTitle("Figyelmeztetés");
        builder.setPositiveButton("Csak utas vagyok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
            }
        });

        builder.setNegativeButton("Csak navigációt használok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
            }
        });
        settings_dialog = builder.create();
        settings_dialog.show();
    }

    private void showDialog3() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (LayoutInflater.from(getApplicationContext()));
        View dialogview = inflater.inflate(R.layout.dialog_licence, null);

        builder.setView(dialogview);
        builder.setTitle("Figyelmeztetés");
        builder.setPositiveButton("Javítom", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showDialog();
                if(pref.getString(Constants.LicensePlate, null) == null) {
                    settings_dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                finish();
                                x = false;
                                settings_dialog.dismiss();
                                return true;
                            }
                            return false;
                        }
                    });
                    settings_dialog.setCanceledOnTouchOutside(false);
                }
            }
        });
        settings_dialog = builder.create();
        settings_dialog.show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        container_up.setVisibility(View.VISIBLE);
        if (search_active){
            getSupportFragmentManager().popBackStackImmediate();
            changeSearch();
            container_up.setVisibility(View.VISIBLE);
        } else if (parking_card.getVisibility() == View.VISIBLE){
            parking_card.setVisibility(View.GONE);
            btn_navigate.setVisibility(View.GONE);
            container_up.setVisibility(View.VISIBLE);
            x=false;
        } else if (menu.getVisibility() == View.VISIBLE){
            infosav.setVisibility(View.VISIBLE);
            infosav.startAnimation(slide_up1);
            menu.startAnimation(slide_down);
            menu.setVisibility(View.GONE);
            container_up.setVisibility(View.VISIBLE);
            x = false;
        } else if (distance_container.getVisibility() == View.VISIBLE){
            distance_container.setVisibility(View.GONE);
            distance_bg.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
            tv_distance.setTextColor(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
            distance_container.startAnimation(slide_down2);
            container_up.setVisibility(View.VISIBLE);
            x = false;
        } else if (!x) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recreate();
                } else {
                   finish();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void loadJSON(final String latitude, final String longitude, final String distance){

        gmap.clear();

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
                    Toast.makeText(getApplication(), resp.getAlert(), Toast.LENGTH_LONG).show();
                }
                if(resp.getError() != null){
                    /*Toast.makeText(getContext(), resp.getError().getMessage()+" - "+resp.getError().getMessageDetail(), Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN,false);
                    editor.apply();*/
                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                if(resp.getParking_places() != null && resp.getParking_places().length != 0){
                    data = new ArrayList<Parking_places>(Arrays.asList(resp.getParking_places()));

                    ArrayList<Parking_places> markersArray = new ArrayList<>();

                    int freePlaces = 0;

                    for (int i = 0; i < data.size(); i++) {
                        createMarker(data.get(i).getCenterLatitude(), data.get(i).getCenterLongitude(), data.get(i).getAddress(), i);
                        //markerKey = i;
                        //freePlaces += Integer.valueOf(data.get(i).getFreePlaces());
                    }

                    gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker1) {
                            /*Toast.makeText(getContext(), String.valueOf(marker1.getPosition())
                                    + String.valueOf(marker1.getTitle()), Toast.LENGTH_LONG).show();*/

                            if (x){
                                infosav.setVisibility(View.VISIBLE);
                                infosav.startAnimation(slide_up1);
                                if(menu.getVisibility()==View.VISIBLE){
                                    menu.startAnimation(slide_down);
                                    menu.setVisibility(View.GONE);
                                }
                            }
                            if (distance_container.getVisibility()==View.VISIBLE){
                               distance_container.setVisibility(View.GONE);
                            }
                            x = false;
                            parking_card_bool = true;
                            cardView.setVisibility(View.VISIBLE);

                            Log.d(TAG, "marker: "+marker1.getId());
                            Log.d(TAG, "marker: "+marker1.getSnippet());

                            if(marker1.getSnippet() != null) {
                                marker1.hideInfoWindow();
                                position = marker1.getPosition();
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("id", data.get(parseInt(marker1.getSnippet())).getId());
                                editor.putString("zone", data.get(parseInt(marker1.getSnippet())).getId());
                                editor.putString("address", data.get(parseInt(marker1.getSnippet())).getAddress());
                                editor.putString("price", String.format("%.0f", Double.parseDouble(data.get(parseInt(marker1.getSnippet())).getPrice())));
                                editor.putString("latitude", String.format("%.0f", Double.parseDouble(data.get(parseInt(marker1.getSnippet())).getCenterLatitude())));
                                editor.putString("longitude", String.format("%.0f", Double.parseDouble(data.get(parseInt(marker1.getSnippet())).getCenterLatitude())));
                                editor.putString("time", String.format("%.0f", Double.parseDouble(data.get(parseInt(marker1.getSnippet())).getTime())));
                                editor.putString("distance", String.format("%.0f", Double.parseDouble(data.get(parseInt(marker1.getSnippet())).getDistance())));
                                editor.putString("timeLimit", String.format("%.0f", Double.parseDouble(data.get(parseInt(marker1.getSnippet())).getTimeLimit())));
                                editor.putString("maxTime", data.get(parseInt(marker1.getSnippet())).getTimeLimit());
                                editor.putString("codeNumber", String.format("%.0f", Double.parseDouble(data.get(parseInt(marker1.getSnippet())).getCodeNumber())));
                                editor.apply();
                                parking_card.setVisibility(View.VISIBLE);
                                //x=true;
                                btn_navigate.setVisibility(View.VISIBLE);
                                Log.d(TAG, "ott");
                                btn_navigate.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        z = position.latitude;
                                        y = position.longitude;
                                        String uri = String.format("http://maps.google.com/maps?" + "saddr=" + latitude1 + "," + longitude1 + "&daddr=" + z + "," + y + "");
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                        startActivity(intent);
                                    }
                                });
                                card_address.setText(pref.getString("address", null));
                                card_count.setText(data.get(parseInt(marker1.getSnippet())).getFreePlaces());
                                card_perprice.setText(pref.getString("price", null) + " Ft/óra");
                                if (pref.getString("distance", null) != null) {
                                    if (!pref.getString("distance", null).equals("nincs megadva")) {
                                        distance_km.setText(String.format("%.1f", Double.parseDouble(pref.getString("distance", null)) / 1000.0) + " km jelenlegi tartózkodási helytől");
                                    } else {
                                        distance_km.setText(pref.getString("distance", null) + " km jelenlegi tartózkodási helytől");
                                    }
                                }
                                if (pref.getString("time", null) != null) {
                                    if (!pref.getString("time", null).equals("nincs megadva")) {
                                        distance_mins.setText(String.format("%.1f", Double.parseDouble(pref.getString("time", null))) + " perc forgalom nélkül");
                                    } else {
                                        distance_mins.setText(pref.getString("time", null) + " perc forgalom nélkül");
                                    }
                                }
                                //editor.apply();
                                //checkForSlot();
                                return false;
                            }else{
                                parking_card.setVisibility(View.GONE);
                                btn_navigate.setVisibility(View.VISIBLE);
                                Log.d(TAG, "emitt");
                                btn_navigate.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //z = position.latitude;
                                        //y = position.longitude;
                                        Log.d(TAG, "LAT: "+latitude+", LONG: "+longitude);
                                        Log.d(TAG, placeLat+", "+placeLong);
                                        String uri = String.format("http://maps.google.com/maps?" + "saddr=" + latitude1 + "," + longitude1 + "&daddr=" + placeLat + "," + placeLong + "");
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                        startActivity(intent);
                                    }
                                });
                                return false;
                            }
                        }
                    });
                    Log.d(TAG, "click"+pref.getString("click", null));
                    if(pref.getString("click", null)!=null){
                        if(!pref.getString("click", null).equals("no")){
                            Log.d(TAG, "itt vagyok");
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("click", "no");
                            editor.apply();
                        /*if(pref.getString("latitudeZnoe", null) != null && pref.getString("longitudeZone", null) != null){
                            createMarker(pref.getString("latitudeZone", null), pref.getString("longitudeZone", null), pref.getString("address", null));*/
                            double c = Double.parseDouble(pref.getString("latitudeZone", null));
                            double d = Double.parseDouble(pref.getString("longitudeZone", null));
                            Log.d(TAG, "latlong"+c+" "+d);
                        /*parking_card.setVisibility(View.VISIBLE);
                        card_address.setText(pref.getString("address", null));
                        //card_count.setText(data.get(parseInt(marker1.getId().substring(1))).getFreePlaces());
                        card_perprice.setText(pref.getString("prive", null) + " Ft/óra");
                        distance_km.setText(Double.parseDouble(pref.getString("distance", null)) + " km from your current location");
                        distance_mins.setText(Double.parseDouble(pref.getString("time", null)) + " mins without traffic");
                        editor.apply();*/
                            LatLng myloc = new LatLng(c, d);
                            gmap.animateCamera(CameraUpdateFactory.newLatLng(myloc));
                            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 16));

                            card_address.setText(pref.getString("address", null));
                            card_count.setText(pref.getString("freeplaces", null));
                            parkingcount.setText(pref.getString("freeplaces", "0")+ "szabad");
                            card_perprice.setText(pref.getString("price", null) + " Ft/óra");
                            if (pref.getString("distance",null)!=null){
                                if(!pref.getString("distance", null).equals("nincs megadva")){
                                    distance_km.setText(String.format("%.1f", Double.parseDouble(pref.getString("distance", null))/1000.0) + " km jelenlegi tartózkodási helytől");
                                }else{
                                    distance_km.setText(pref.getString("distance", null) + " km jelenlegi tartózkodási helytől");
                                }
                            }
                            if (pref.getString("time",null)!=null){
                                if(!pref.getString("time", null).equals("nincs megadva")){
                                    distance_mins.setText(String.format("%.1f", Double.parseDouble(pref.getString("time", null))) + " perc forgalom nélkül");
                                }else{
                                    distance_mins.setText(pref.getString("time", null) + " perc forgalom nélkül");
                                }
                            }
                            parking_card.setVisibility(View.VISIBLE);
                            btn_navigate.setVisibility(View.VISIBLE);
                            //}
                        } else {
                            if(parking_card_bool == false){
                                parking_card.setVisibility(View.GONE);
                            }
                            x=false;
                            btn_navigate.setVisibility(View.GONE);
                            for (int i = 0; i < data.size(); i++) {
                                Log.d(TAG, "Szabad helyek száma: " + data.get(i).getFreePlaces());
                                freePlaces += Integer.valueOf(data.get(i).getFreePlaces());
                                //createMarker(data.get(i).getCenterLatitude(), data.get(i).getCenterLongitude(), data.get(i).getAddress());
                            /*double c = Double.parseDouble(pref.getString("latitude", null));
                            double d = Double.parseDouble(pref.getString("longitude", null));
                            LatLng myloc = new LatLng(c, d);
                            gmap.animateCamera(CameraUpdateFactory.newLatLng(myloc));
                            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 16));*/
                                parkingcount.setText(String.valueOf(freePlaces) + " szabad");
                                card_count.setText(String.valueOf(freePlaces));
                            }
                        }

                    /*parkingcount.setText(String.valueOf(freePlaces));
                    card_count.setText(String.valueOf(freePlaces));*/
                    /*mAdapter = new SearchAdapter(data);
                    mRecyclerView.setAdapter(mAdapter);

                    mAdapter.setOnItemClickListener(new SearchAdapter.ClickListener(){
                        @Override
                        public void onItemClick(final int position, View v){
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("address", data.get(position).getAddress().toString());
                            editor.putString("price", data.get(position).getPrice().toString());
                            editor.putString("id", data.get(position).getId().toString());
                            editor.putString("latitude", data.get(position).getLatitude().toString());
                            editor.putString("longitude", data.get(position).getLongitude().toString());
                            editor.putString("distance", data.get(position).getDistance().toString());
                            editor.putString("time", data.get(position).getTime().toString());
                            editor.apply();
                            String id = data.get(position).getId().toString();
                            String sessionId = pref.getString("sessionId", null);
                            loadJSONSelect(sessionId, id);
                            FragmentManager map = getActivity().getSupportFragmentManager();
                            map.beginTransaction()
                                    .replace(R.id.frame, new Map())
                                    .addToBackStack(null)
                                    .commit();
                        }

                        @Override
                        public void onItemLongClick(int position, View v) {
                            Log.d(TAG, "onItemLongClick pos = " + position);
                        }
                    });*/
                    }else{
                        if(parking_card_bool == false){
                            parking_card.setVisibility(View.GONE);
                        }
                        x=false;
                        btn_navigate.setVisibility(View.GONE);
                        for (int i = 0; i < data.size(); i++) {
                            Log.d(TAG, "Szabad helyek száma: " + data.get(i).getFreePlaces());
                            freePlaces += Integer.valueOf(data.get(i).getFreePlaces());
                            //createMarker(data.get(i).getCenterLatitude(), data.get(i).getCenterLongitude(), data.get(i).getAddress());
                            /*double c = Double.parseDouble(pref.getString("latitude", null));
                            double d = Double.parseDouble(pref.getString("longitude", null));
                            LatLng myloc = new LatLng(c, d);
                            gmap.animateCamera(CameraUpdateFactory.newLatLng(myloc));
                            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 16));*/
                            parkingcount.setText(String.valueOf(freePlaces) + " szabad");
                            card_count.setText(String.valueOf(freePlaces));
                        }
                    }
                }else{
                    parkingcount.setText("0 szabad");
                    //gmap.clear();
                }

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getApplication(), "Hiba a hálózati kapcsolatban. Kérjük, ellenőrizze, hogy csatlakozik-e hálózathoz.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "No response");
            }
        });

    }

    /*public void loadJSONStatus(String userId) {

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
                    //Toast.makeText(getApplicationContext(), resp.getAlert(), Toast.LENGTH_LONG).show();
                }
                if (resp.getError() != null) {
                    //Toast.makeText(getContext(), resp.getError().getMessage() + " - " + resp.getError().getMessageDetail(), Toast.LENGTH_SHORT).show();
                }
                if (resp.getSum() != null) {
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Hiba a hálózati kapcsolatban. Kérjük, ellenőrizze, hogy csatlakozik-e hálózathoz.", Toast.LENGTH_SHORT).show();
                Log.d(ContentValues.TAG, "No response");
            }
        });
    }*/

    public Marker createMarker(String parklat, String parklong, String address, Integer markerKey) {

        //, String title, String snippet, int iconResID
        double parklatitude = Double.parseDouble(parklat);
        double parklongitude = Double.parseDouble(parklong);

        LatLng myloc = new LatLng(parklatitude, parklongitude);

        /*gmap.animateCamera(CameraUpdateFactory.newLatLng(myloc));
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 15));*/

        marker = gmap.addMarker(new MarkerOptions()
                .position(new LatLng(parklatitude, parklongitude))
                .title(address));
        marker.setSnippet(String.valueOf(markerKey));
        Log.d(TAG, "i: "+markerKey);
        Log.d(TAG, "marker: "+marker.getSnippet());

        return marker;
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter(){
            myContentsView = getLayoutInflater().inflate(R.layout.info_window_layout, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.titleUi));
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippetUi));
            Log.d(TAG, "marker: "+marker.getSnippet());
            tvSnippet.setText("");

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public void addMarker(double c, double d){
        LatLng myloc = new LatLng(c, d);
        gmap.animateCamera(CameraUpdateFactory.newLatLng(myloc));
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 16));
    }

    public void changeSearch(){
        search_active=false;
        x=false;
    }

    public void searchFocusRequest(){
        upsearch.setActivated(true);
        upsearch.requestFocus();
    }

    public void ParkinginProgress(){
        if (pref.getString(Constants.ParkingStatus,"").equals("2")){
            inprogress.setVisibility(View.VISIBLE);
            ValueAnimator backgroundAnim = ObjectAnimator.ofInt(inprogress, "backgroundColor", getResources().getColor(R.color.colorPrimary, getTheme()), getResources().getColor(R.color.colorPurple, getTheme()));
            backgroundAnim.setDuration(3000);
            backgroundAnim.setEvaluator(new ArgbEvaluator());
            backgroundAnim.setRepeatCount(ValueAnimator.INFINITE);
            backgroundAnim.setRepeatMode(ValueAnimator.REVERSE);
            backgroundAnim.start();
        } else if (pref.getString(Constants.ParkingStatus,"").equals("3")){
            inprogress.setVisibility(View.VISIBLE);
            ValueAnimator backgroundAnim = ObjectAnimator.ofInt(inprogress, "backgroundColor", getResources().getColor(R.color.colorPrimary, getTheme()), getResources().getColor(R.color.colorPurple, getTheme()));
            backgroundAnim.setDuration(3000);
            backgroundAnim.setEvaluator(new ArgbEvaluator());
            backgroundAnim.setRepeatCount(ValueAnimator.INFINITE);
            backgroundAnim.setRepeatMode(ValueAnimator.REVERSE);
            backgroundAnim.start();
        } else {
            inprogress.setVisibility(View.GONE);
        }
    }

    public void setX(){
        x=true;
    }
}
