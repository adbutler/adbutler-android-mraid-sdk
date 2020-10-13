package com.sparklit.adbutler;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

interface APIService {
    @GET("{configParam}")
    Call<PlacementResponse> requestPlacement(@Path(value="configParam", encoded=true) String config);

    @GET
    Call<PlacementResponse> refreshPlacement(@Url String url);

    @GET
    Call<ResponseBody> requestPixel(@Url String url);

    @POST("{configParam}")
    Call<PlacementResponse> requestPlacementPOST(@Path(value="configParam", encoded=true) String config, @Body RequestBody data);
}
