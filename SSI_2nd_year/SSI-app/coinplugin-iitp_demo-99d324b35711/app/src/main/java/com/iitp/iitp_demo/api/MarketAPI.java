package com.iitp.iitp_demo.api;


import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MarketAPI{

    private static MarketAPI _marketApi = null;
    public final MarketAPIInfo marketAPIInfo;
    private final Retrofit retrofit;


    public MarketAPI(){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger(){
            @Override
            public void log(@NonNull String message){
                Log.d("Verification", message);
            }
        });

            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient = new OkHttpClient.Builder();
        httpClient.interceptors().add(loggingInterceptor);

        httpClient.readTimeout(30, TimeUnit.SECONDS);
        httpClient.writeTimeout(15, TimeUnit.SECONDS);
        retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(MarketAPIInfo.BaseUrl)
                .client(httpClient.build())
                .build();
        marketAPIInfo = retrofit.create(MarketAPIInfo.class);
    }

    public static MarketAPI getInstance(){

        if(_marketApi == null){
            _marketApi = new MarketAPI();
        }
        return _marketApi;
    }
}
