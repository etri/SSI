package com.iitp.iitp_demo.api;

import com.iitp.iitp_demo.api.model.delegaterVCVo;
import com.iitp.iitp_demo.api.model.pushFriendVo;
import com.iitp.iitp_demo.api.model.pushRegisterVo;
import com.iitp.iitp_demo.api.model.pushResponseVo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface AgentAPIInfo{

    public final static String BaseUrl = "http://129.254.194.138:8080/";
    @Headers("Content-Type:application/json")
    @POST("v1/ms/register")
    Call<pushResponseVo> registerToken(@Body pushRegisterVo data);

    @Headers("Content-Type:application/json")
    @GET("v1/friends/clean")
    Call<pushResponseVo> resetFriends(@Query("pass") String token);


    @Headers("Content-Type:application/json")
    @POST("v1/friends/create")
    Call<pushResponseVo> requestVerify(@Body pushFriendVo data);

    @Headers("Content-Type:application/json")
    @POST("v1/friends")
    Call<pushResponseVo> requestFriendsList(@Body pushRegisterVo data);

    @Headers("Content-Type:application/json")
    @POST("v1/credential/send")
    Call<pushResponseVo> sendCredential(@Body delegaterVCVo data);

    @Headers("Content-Type:application/json")
    @POST("v1/friends/create")
    Call<pushResponseVo> requestCreateFriends(@Body pushFriendVo data);

    @Headers("Content-Type:application/json")
    @GET("v1/credential/receive")
    Call<pushResponseVo> requestGetVC(@Query("token") String token);


}


