package hu.bitnet.smartparking.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import hu.bitnet.smartparking.Adapters.HistoryAdapter;
import hu.bitnet.smartparking.R;
import hu.bitnet.smartparking.RequestInterfaces.RequestInterfaceHistory;
import hu.bitnet.smartparking.ServerResponses.ServerResponse;
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
public class History extends Fragment {

    SharedPreferences pref;
    RecyclerView history_rv;
    public ArrayList<hu.bitnet.smartparking.objects.History> data;
    public HistoryAdapter mAdapter;

    public History() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View history = inflater.inflate(R.layout.fragment_history, container, false);
        pref = getActivity().getPreferences(0);
        history_rv = (RecyclerView) history.findViewById(R.id.history_rv);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        history_rv.setLayoutManager(layoutManager);

        loadJSON(pref.getString(Constants.UID, null));

        return history;
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
                    getActivity().findViewById(R.id.btn_history).setBackgroundResource(R.drawable.button_background);
                    getActivity().findViewById(R.id.btn_myloc).setVisibility(View.VISIBLE);
                    //getActivity().findViewById(R.id.btn_search).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.card_view).setVisibility(View.VISIBLE);
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

    public void loadJSON(String sessionId){

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl(Constants.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterfaceHistory requestInterface = retrofit.create(RequestInterfaceHistory.class);
        Call<ServerResponse> response= requestInterface.post(sessionId);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                if(resp.getAlert() != ""){
                    Toast.makeText(getContext(), resp.getAlert(), Toast.LENGTH_LONG).show();
                }
                if(resp.getError() != null){
                    //Toast.makeText(getContext(), resp.getError().getMessage()+" - "+resp.getError().getMessageDetail(), Toast.LENGTH_SHORT).show();
                }
                if(resp.getHistory() != null){
                    data = new ArrayList<hu.bitnet.smartparking.objects.History>(Arrays.asList(resp.getHistory()));
                    mAdapter = new HistoryAdapter(data);
                    history_rv.setAdapter(mAdapter);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Hiba a hálózati kapcsolatban. Kérjük, ellenőrizze, hogy csatlakozik-e hálózathoz.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "No response");
            }
        });

    }

}
