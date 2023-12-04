package com.ekc.ekccollector.collector.model.api;


import com.ekc.ekccollector.collector.model.models.ImageBody;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface NetworkAPIS {

    String JSON_CONTENT_TYPE = "Content-Type: application/json";

    @Multipart
    @POST("uploadimages")
    Call<Object> uploadImageByPart(@Part MultipartBody.Part imageFile,
                                   @Part MultipartBody.Part day,@Part MultipartBody.Part month,
                                   @Part MultipartBody.Part year,@Part MultipartBody.Part surveyor,
                                   @Part MultipartBody.Part layer,@Part MultipartBody.Part deviceNo);

    @POST("upload")
    Observable<Integer> uploadImage(@Body ImageBody imageBody);

}
