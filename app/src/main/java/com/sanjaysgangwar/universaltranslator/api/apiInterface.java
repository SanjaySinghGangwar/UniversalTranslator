package com.sanjaysgangwar.universaltranslator.api;


import com.google.gson.JsonObject;
import com.sanjaysgangwar.universaltranslator.modelClasses.translateModel.Model;
import com.sanjaysgangwar.universaltranslator.modelClasses.visionModel.VisionModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface apiInterface {

    @POST("?key=AIzaSyA7hQ5A_MnRf2TM2yf0nIO61wdqNKPWgyQ")
    Call<VisionModel> visionApi(@Body JsonObject dataToSend);


    @FormUrlEncoded
    @POST("v2")
    Call<Model> translateApi(@Field("key") String key,
                             @Field("q") String q,
                             @Field("target") String target);


}
