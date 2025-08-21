package com.technohub.zone.gembot.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiService {

    @Headers({
            "Content-Type: application/json"
    })
    @POST
    Call<Map<String, Object>> sendMessage(
            @Url String url, // dynamic endpoint
            @Body Map<String, Object> body
    );
}
