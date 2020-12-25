package com.iitp.iitp_demo.api.didResolver;

import android.util.Log;

import androidx.annotation.NonNull;

import com.iitp.iitp_demo.BuildConfig;
import com.iitp.iitp_demo.IITPApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class DIDResolverAPI{

    private static DIDResolverAPI _DIDAPI = null;
    public final DIDResolverAPIInfo didResolverAPIInfo;
    private final Retrofit retrofit;
    public Map<String, String> headers = new HashMap<>();


    public DIDResolverAPI(){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger(){
            @Override
            public void log(@NonNull String message){
                Log.d("DID Resolver", message);
            }
        });
        if(BuildConfig.DEBUG){
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        }else{
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        }
        httpClient = new OkHttpClient.Builder();
        httpClient.interceptors().add(loggingInterceptor);
        httpClient.readTimeout(30, TimeUnit.SECONDS);
        httpClient.writeTimeout(15, TimeUnit.SECONDS);

        retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(DIDResolverAPIInfo.BaseUrl)
                .client(httpClient.build())
                .build();
        didResolverAPIInfo = retrofit.create(DIDResolverAPIInfo.class);
    }

    public static DIDResolverAPI getInstance(){

        if(_DIDAPI == null){
            _DIDAPI = new DIDResolverAPI();
        }
        return _DIDAPI;
    }
}
