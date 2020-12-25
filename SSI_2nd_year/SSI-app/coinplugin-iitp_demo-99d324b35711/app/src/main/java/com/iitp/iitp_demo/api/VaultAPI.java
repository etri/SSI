package com.iitp.iitp_demo.api;


import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class VaultAPI{

    private static VaultAPI _vaultApi = null;
    public final VaultAPIInfo vaultAPIInfo;
    private final Retrofit retrofit;


    public VaultAPI(){
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
                .baseUrl(VaultAPIInfo.BaseUrl)
                .client(httpClient.build())
                .build();
        vaultAPIInfo = retrofit.create(VaultAPIInfo.class);
    }

    public static VaultAPI getInstance(){

        if(_vaultApi == null){
            _vaultApi = new VaultAPI();
        }
        return _vaultApi;
    }
}
