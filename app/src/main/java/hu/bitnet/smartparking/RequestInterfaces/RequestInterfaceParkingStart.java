package hu.bitnet.smartparking.RequestInterfaces;

import hu.bitnet.smartparking.ServerResponses.ServerResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Attila on 2017.08.08..
 */

public interface RequestInterfaceParkingStart {

    @POST("parking_start")
    @FormUrlEncoded
    Call<ServerResponse> post(@Field("userId") String userId, @Field("zoneId") String zoneId, @Field("latitude") String latitude, @Field("longitude") String longitude);

}
