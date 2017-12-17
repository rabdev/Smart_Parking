package hu.bitnet.smartparking.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import hu.bitnet.smartparking.Adapters.UpSearchAdapter;
import hu.bitnet.smartparking.MainActivity;
import hu.bitnet.smartparking.R;
import hu.bitnet.smartparking.RequestInterfaces.RequestInterfaceAutocomplete;
import hu.bitnet.smartparking.ServerResponses.ServerResponseSearchZones;
import hu.bitnet.smartparking.objects.Addresses;
import hu.bitnet.smartparking.objects.Constants;
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
public class Search extends Fragment {

    RecyclerView search_rv;
    //public ArrayList<Parking_places> data;
    public UpSearchAdapter mAdapter;
    public ArrayList<Addresses> data;
    public String address;
    EditText upsearch;
    SharedPreferences pref;
    //public SearchAdapter mAdapter;

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

        pref = getActivity().getPreferences(0);

        upsearch= (EditText) getActivity().findViewById(R.id.upsearch);

        loadJSON(upsearch.getText().toString());

        upsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (upsearch.getText().toString().trim().length() > 2) {
                    loadJSON(upsearch.getText().toString());
                    //((MainActivity)getActivity()).searchFocusRequest();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
                    View view = getActivity().getCurrentFocus();
                    if(getActivity().findViewById(R.id.menu_layout).getVisibility()!=View.VISIBLE){
                        getActivity().findViewById(R.id.container_up).setVisibility(View.VISIBLE);
                    }
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void loadJSON(String address){

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl(Constants.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterfaceAutocomplete requestInterface = retrofit.create(RequestInterfaceAutocomplete.class);
        Call<ServerResponseSearchZones> response= requestInterface.post(address);
        response.enqueue(new Callback<ServerResponseSearchZones>() {
            @Override
            public void onResponse(Call<ServerResponseSearchZones> call, Response<ServerResponseSearchZones> response) {
                ServerResponseSearchZones resp = response.body();
                if(resp.getAlert() != null){
                    if(resp.getAlert() != "") {
                        Toast.makeText(getContext(), resp.getAlert(), Toast.LENGTH_LONG).show();
                    }
                }
                if(resp.getError() != null){
                    /*Toast.makeText(getContext(), resp.getError().getMessage()+" - "+resp.getError().getMessageDetail(), Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN,false);
                    editor.apply();
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);*/
                }
                if(resp.getAddresses() == null){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle("Nincs eredmény!");
                    alertDialog.setMessage("Próbálkozzon más kulcsszóval vagy metódussal!");
                    alertDialog.setIcon(R.drawable.ic_parking);

                    alertDialog.setPositiveButton("Rendben", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                                /*Home home1 = new Home();
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.frame, home1, "Home")
                                        .addToBackStack(null)
                                        .commit();*/
                        }
                    });

                    alertDialog.setNegativeButton("Mégsem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                                /*Home home1 = new Home();
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.frame, home1, "Home")
                                        .addToBackStack(null)
                                        .commit();*/
                        }
                    });

                    alertDialog.show();
                }else{
                    data = new ArrayList<Addresses>(Arrays.asList(resp.getAddresses()));
                    //Log.d(TAG, "data: "+data.get(0).getAddress().toString());
                    mAdapter = new UpSearchAdapter(data);
                    search_rv.setAdapter(mAdapter);
                    if (getView()!=null){
                        ((MainActivity)getActivity()).searchFocusRequest();
                    }

                    mAdapter.setOnItemClickListener(new UpSearchAdapter.ClickListener(){
                        @Override
                        public void onItemClick(final int position, View v){
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("address", data.get(position).getAddress().toString());
                            //editor.putString("zone", data.get(position).getId().toString());
                            editor.putString("price", String.format("%.0f", Double.parseDouble((data.get(position).getPrice().toString()))));
                            //editor.putString("id", data.get(position).getId().toString());
                            editor.putString("latitudeZone", data.get(position).getLatitude().toString());
                            editor.putString("longitudeZone", data.get(position).getLongitude().toString());
                            editor.putString("click", "yes");
                            editor.putString("time", "nincs megadva");
                            editor.putString("distance", "nincs megadva");
                            editor.apply();
                            /*FragmentManager map = getActivity().getSupportFragmentManager();
                            map.beginTransaction()
                                    .replace(R.id.frame, new Map())
                                    .addToBackStack(null)
                                    .commit();*/
                            View view = getActivity().getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                            double c = Double.parseDouble(data.get(position).getLatitude().toString());
                            double d = Double.parseDouble(data.get(position).getLongitude().toString());
                            ((MainActivity)getActivity()).addMarker(c,d);
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            fm.popBackStack();
                            ((MainActivity)getActivity()).changeSearch();
                        }

                        @Override
                        public void onItemLongClick(int position, View v) {
                            Log.d(TAG, "onItemLongClick pos = " + position);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ServerResponseSearchZones> call, Throwable t) {
                Toast.makeText(getContext(), "Hiba a hálózati kapcsolatban. Kérjük, ellenőrizze, hogy csatlakozik-e hálózathoz.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "No response");
            }
        });

    }

}
