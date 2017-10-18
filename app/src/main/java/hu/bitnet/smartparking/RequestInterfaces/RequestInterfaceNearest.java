package hu.bitnet.smartparking.RequestInterfaces;

import hu.bitnet.smartparking.ServerResponses.ServerResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Attila on 2017.10.13..
 */

public interface RequestInterfaceNearest {

    @POST("nearest_list")
    @FormUrlEncoded
    Call<ServerResponse> post(@Field("radius") String sessionId, @Field("latitude") String latitude, @Field("longitude") String longitude);
}
