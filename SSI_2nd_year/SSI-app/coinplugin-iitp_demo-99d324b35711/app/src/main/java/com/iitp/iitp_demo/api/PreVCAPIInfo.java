package com.iitp.iitp_demo.api;

import com.iitp.iitp_demo.api.model.CredentialRequestVo;
import com.iitp.iitp_demo.api.model.RequestGetOfferVo;
import com.iitp.iitp_demo.api.model.RequestVCVo;
import com.iitp.iitp_demo.api.model.SendDIDVo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface PreVCAPIInfo{

    public final static String BaseUrl = "https://resolver.metadium.com/1.0/";
    @Headers("Content-Type:application/json")
    @POST
    Call<ResponseBody> requestPreVC(@Url String url, @Body RequestVCVo data);


    @Headers("Content-Type:application/json")
    @POST
    Call<ResponseBody> requestGetOffer(@Url String url, @Body RequestGetOfferVo data);

    @Headers("Content-Type:application/json")
    @POST
    Call<ResponseBody> requestSetCredentialRequest(@Url String url, @Body CredentialRequestVo data);

    @Headers("Content-Type:application/json")
    @POST
    Call<ResponseBody> requestPreVC(@Url String url, @Body String data);

    @Headers("Content-Type:application/json")
    @POST
    Call<ResponseBody> requestVP(@Url String url, @Body String data);

    @Headers("Content-Type:application/json")
    @GET
    Call<ResponseBody> requestLoginPreVC(@Url String url, @Query(value = "did", encoded = false) String json);


    @Headers("Content-Type:application/json")
    @POST
    Call<ResponseBody> sendDID(@Url String url, @Body SendDIDVo data);



}


