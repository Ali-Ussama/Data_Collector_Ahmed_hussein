package com.ekc.ekccollector.collector.model.api;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.ekc.ekccollector.collector.model.models.ImageBody;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    public static String BASE_URL = "http://62.135.109.58:8080/api/";

    private static ApiClient remoteDataSource;
    private Retrofit retrofit;
    private CommonCallback<Object> callback;

    public interface CommonCallback<T> {
        void onSuccess(T response);

        void onFailure(Throwable throwable);
    }

    public static synchronized ApiClient getInstance() {
        if (remoteDataSource == null) {
            remoteDataSource = new ApiClient();
        }

        return remoteDataSource;
    }

    private NetworkAPIS getAPI() {

        OkHttpClient.Builder okHttpBuilder = ProgressManager.getInstance().with(new OkHttpClient.Builder());
        okHttpBuilder.connectTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpBuilder.addInterceptor(interceptor);

        Gson gson = new GsonBuilder().setLenient().create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpBuilder.build())
                .build();
        return retrofit.create(NetworkAPIS.class);
    }

//    @SuppressLint("CheckResult")
//    public void uploadImage(ImageBody imageBody, CommonCallback<Object> callback) {
//        try {
//            this.callback = callback;
////            ProgressRequestBody fileBody = new ProgressRequestBody(image, this);
////            MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", image.getName(), fileBody);
//            NetworkAPIS networkAPIS = ApiClient.getInstance().getAPI();
//            Observable<Integer> observable = networkAPIS.uploadImage(imageBody);
//            observable.subscribeOn(Schedulers.newThread())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .map(result -> result)
//                    .subscribe(this::handleUploadImageResult, this::handleUploadImageError);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @SuppressLint("CheckResult")
    public void uploadImage(ImageBody imageBody, CommonCallback<Object> callback) {
        try {
            RequestBody requestFile = RequestBody.create(imageBody.getImageFile(), MediaType.parse("image/*"));

            MultipartBody.Part fileBody = MultipartBody.Part.createFormData("File[0]", imageBody.getImageFile().getName(), requestFile);
            MultipartBody.Part dayBody = MultipartBody.Part.createFormData("Day", imageBody.getDateInfo().getDay());
            MultipartBody.Part monthBody = MultipartBody.Part.createFormData("Month", imageBody.getDateInfo().getMonth());
            MultipartBody.Part yearBody = MultipartBody.Part.createFormData("Year", imageBody.getDateInfo().getYear());
            MultipartBody.Part surveyorBody = MultipartBody.Part.createFormData("Survayor", imageBody.getSurvayor());
            MultipartBody.Part layerBody = MultipartBody.Part.createFormData("Layer", imageBody.getLayerName());
            MultipartBody.Part deviceBody = MultipartBody.Part.createFormData("Device", imageBody.getDeviceNo());

            NetworkAPIS networkAPIS = ApiClient.getInstance().getAPI();
            Call<Object> call = networkAPIS.uploadImageByPart(fileBody, dayBody, monthBody, yearBody, surveyorBody, layerBody, deviceBody);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                    try {
                        if (response != null && response.isSuccessful()) {
                            if (callback != null) {
                                callback.onSuccess(response);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                    if (callback != null) {
                        callback.onFailure(t);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUploadImageError(Throwable t) {
        callback.onFailure(t);
    }

    private void handleUploadImageResult(Integer status) {
        callback.onSuccess(status);
    }


}
