package hu.bitnet.smartparking;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

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


import hu.bitnet.smartparking.fragments.History;
import hu.bitnet.smartparking.fragments.Search;
import hu.bitnet.smartparking.fragments.Zones;
import hu.bitnet.smartparking.objects.Constants;

public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, LocationSource.OnLocationChangedListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public final static int MILLISECONDS_PER_SECOND = 1000;
    public final static int MINUTE = 60 * MILLISECONDS_PER_SECOND;

    SharedPreferences pref;
    AlertDialog settings_dialog;
    GoogleMap gmap;
    MapView mapView;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location location;
    android.location.LocationListener locationlistener;
    LinearLayout infosav, menu, distance_container, distance, distance_bg, parking_card;
    ImageView settings, collapse, hb_menu, btn_search, btn_navigate, inprogress;
    AppCompatButton history, parkingplaces;
    TextView firstrun, tv_distance, et_distance, indistance, tv_sb_distance;
    EditText search, et_license_plate, et_name, et_smsbase, upsearch;
    SeekBar settings_distance, sb_distance;
    Animation slide_up, slide_up1, slide_up2, slide_down, slide_down2;
    boolean x, bool_license, bool_distance, bool_smsbase;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    int index, prog;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getPreferences(0);
        x = false;


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
        settings = (ImageView) findViewById(R.id.btn_settings);
        collapse = (ImageView) findViewById(R.id.btn_collapse);
        hb_menu = (ImageView) findViewById(R.id.hb_menu);
        btn_search = (ImageView) findViewById(R.id.btn_search);
        btn_navigate= (ImageView) findViewById(R.id.btn_navigate);
        inprogress = (ImageView) findViewById(R.id.btn_inprogress);
        history = (AppCompatButton) findViewById(R.id.btn_history);
        parkingplaces = (AppCompatButton) findViewById(R.id.btn_parking_places);
        search = (EditText) findViewById(R.id.search);
        upsearch = (EditText) findViewById(R.id.upsearch);
        tv_distance = (TextView) findViewById(R.id.tv_distance);
        indistance = (TextView) findViewById(R.id.indistance);
        tv_sb_distance = (TextView) findViewById(R.id.tv_sb_distance);
        sb_distance = (SeekBar) findViewById(R.id.sb_distance);

        menu.setVisibility(View.GONE);
        distance_container.setVisibility(View.GONE);
        parking_card.setVisibility(View.GONE);
        btn_navigate.setVisibility(View.GONE);
        inprogress.setVisibility(View.GONE);
        upsearch.setVisibility(View.GONE);

        /*ColorDrawable[] purple = {new ColorDrawable(getResources().getColor(R.color.colorPrimary, getTheme())), new ColorDrawable(getResources().getColor(R.color.colorPurple,getTheme()))};
        TransitionDrawable transition = new TransitionDrawable(purple);
        inprogress.setBackground(transition);
        transition.startTransition(10000);*/

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


        if (pref.getString(Constants.LicensePlate, "").isEmpty() && pref.getString(Constants.SettingsDistance, "").isEmpty() && pref.getString(Constants.SMSBase, "").isEmpty()) {
            showDialog();
            firstrun.setVisibility(View.VISIBLE);
            bool_distance = false;
            bool_license = false;
            bool_smsbase = false;
            settings_dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        finish();
                        x = false;
                        settings_dialog.dismiss();
                    }
                    return true;
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
                    distance_bg.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
                    tv_distance.setTextColor(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
                    //distance_bg.setColo(getResources().getColor(R.color.colorPrimaryDark,getTheme()));
                    x = false;
                }
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
                    distance_bg.setBackgroundTintList(getResources().getColorStateList(R.color.colorPurple, getTheme()));
                    tv_distance.setTextColor(getResources().getColorStateList(R.color.colorPurple, getTheme()));
                    distance_container.startAnimation(slide_up2);
                    tv_sb_distance.setText(pref.getString(Constants.SettingsDistance, null) + " m");
                    sb_distance.setMax(2500);
                    prog = Integer.parseInt(pref.getString(Constants.SettingsDistance, null));
                    sb_distance.setProgress(prog);
                    sb_distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            prog = sb_distance.getProgress();
                            tv_sb_distance.setText(String.valueOf(prog) + " m");
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
                x = false;
            }
        });
        parkingplaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Zones zones = new Zones();
                history.setBackgroundResource(R.drawable.button_background);
                parkingplaces.setBackgroundResource(R.drawable.button_background_active);
                FragmentManager fragmentManager = getSupportFragmentManager();
                index = fragmentManager.getBackStackEntryCount();
                if (index != 0) {
                    fragmentManager.popBackStack();
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.mapView, zones, zones.getTag())
                        .addToBackStack("Zones")
                        .commit();
                infosav.setVisibility(View.VISIBLE);
                infosav.startAnimation(slide_up1);
                menu.startAnimation(slide_down);
                menu.setVisibility(View.GONE);
                x = false;
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

        upsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (upsearch.getText().toString().trim().length() > 2) {
                    Search search = new Search();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .add(R.id.mapView, search, search.getTag())
                            .addToBackStack("Search")
                            .commit();
                } else {
                    getSupportFragmentManager().popBackStackImmediate();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (upsearch.getVisibility()!= View.VISIBLE){
                    upsearch.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            gmap.getUiSettings().setMyLocationButtonEnabled(true);
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            layoutParams.setMargins(40,40,0,0);
            layoutParams.setMarginStart(40);
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



    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
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

        settings_distance.setMax(2500);
        if (pref.getString(Constants.SettingsDistance, null) != null) {
            if (!pref.getString(Constants.SettingsDistance, null).isEmpty()) {
                prog = Integer.parseInt(pref.getString(Constants.SettingsDistance, null));
                settings_distance.setProgress(prog);
                et_distance.setText(pref.getString(Constants.SettingsDistance, null));
            }
        } else {
            prog = 0;
            settings_distance.setProgress(prog);
            et_distance.setText(String.valueOf(prog));
        }

        settings_distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prog = settings_distance.getProgress();
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
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(Constants.LicensePlate, et_license_plate.getText().toString());
                editor.putString(Constants.SMSBase, et_smsbase.getText().toString());
                editor.putString(Constants.SettingsDistance, et_distance.getText().toString());
                editor.putString(Constants.NAME, et_name.getText().toString());
                editor.apply();
                tv_distance.setText(pref.getString(Constants.SettingsDistance, null));
                indistance.setText(pref.getString(Constants.SettingsDistance, null) + " m-es körzetben");
                x = false;
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
                        MainActivity.super.onBackPressed();
                    }
                    dialog.dismiss();
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
        if (!x) {
            finish();
        } else if (menu.getVisibility() == View.VISIBLE) {
            infosav.setVisibility(View.VISIBLE);
            infosav.startAnimation(slide_up1);
            menu.startAnimation(slide_down);
            menu.setVisibility(View.GONE);
            x = false;
        } else if (distance.getVisibility() == View.VISIBLE) {
            distance_container.setVisibility(View.GONE);
            distance_bg.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
            tv_distance.setTextColor(getResources().getColorStateList(R.color.colorPrimaryDark, getTheme()));
            distance_container.startAnimation(slide_down2);
            x = false;
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
}
