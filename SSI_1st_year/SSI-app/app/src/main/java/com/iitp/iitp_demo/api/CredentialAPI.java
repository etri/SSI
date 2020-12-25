package com.iitp.iitp_demo.api;


import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class CredentialAPI{

    private static CredentialAPI _verificationAPI = null;
    public final CredentialAPIInfo verificationAPIInfo;
    private final Retrofit retrofit;
    public Map<String, String> headers = new HashMap<>();


    public CredentialAPI(){
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
                .baseUrl(CredentialAPIInfo.BaseUrl)
                .client(httpClient.build())
                .build();
        verificationAPIInfo = retrofit.create(CredentialAPIInfo.class);
    }

    public static CredentialAPI getInstance(){

        if(_verificationAPI == null){
            _verificationAPI = new CredentialAPI();
        }
        return _verificationAPI;
    }

    /**
     * 헤더 보안 추가
     *
     * @param nonce  nonce
     * @param method "POST", "GET"
     * @param url    url
     * @param jwt    jwt data
     *               algorithm HmacSHA256
     *               message = nonce + method + url + jwt
     */
   /* public void setHeader(String nonce, String method, String url, String jwt){
        String SIGNATURE_ALGORITHM = "HmacSHA256";
        String message = nonce + method + url + jwt;
        byte[] bkey = AA_API_KEY.getBytes();
        String signature = null;
        final SecretKeySpec secretKey = new SecretKeySpec(bkey, SIGNATURE_ALGORITHM);
        try{
            Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            mac.init(secretKey);
            signature = Numeric.toHexStringNoPrefix(mac.doFinal(message.getBytes()));
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch(InvalidKeyException e){
            e.printStackTrace();
        }
//        PrintLog.e("signature = "+signature );
        headers.put("Content-Type", "application/jwt");
        headers.put("ATTESTATOR-API-NONCE", nonce);
        headers.put("ATTESTATOR-API-SIGNATURE", signature);
    }*/

}
