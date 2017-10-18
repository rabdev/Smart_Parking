package hu.bitnet.smartparking.RequestInterfaces;

import hu.bitnet.smartparking.ServerResponses.ServerResponseSearchZones;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Attila on 2017.08.08..
 */

public interface RequestInterfaceAutocomplete {

    @POST("autocomplete")
    @FormUrlEncoded
    Call<ServerResponseSearchZones> post(@Field("address") String address);

}
