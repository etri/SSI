package com.iitp.iitp_demo.api;

import com.iitp.iitp_demo.BuildConfig;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface CredentialAPIInfo{

    public final static String BaseUrl = BuildConfig.MAIN_NET ? "https://resolver.metadium.com/1.0/" : "https://testnetresolver.metadium.com/1.0/";
    @Headers("Content-Type:application/json")
    @POST
    Call<ResponseBody> requestResidentIdCard(@Url String url, @Body String jwt);

}
