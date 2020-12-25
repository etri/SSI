package com.iitp.iitp_demo.api;

import com.iitp.iitp_demo.activity.model.VpResponseVo;
import com.iitp.iitp_demo.api.model.AuthResponseVo;
import com.iitp.iitp_demo.api.model.ChallengeVo;
import com.iitp.iitp_demo.api.model.ResultVo;
import com.iitp.iitp_demo.api.model.WebResultVo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface MarketAPIInfo{

    public final static String BaseUrl = "http://129.254.194.112:9004/";

    @Headers("Content-Type:application/json")
    @POST
    Call<WebResultVo> requestChallenge(@Url String url, @Body ChallengeVo challangeVo);

    @Headers("Content-Type:application/json")
    @POST("/api/vp/verify")
    Call<WebResultVo> resposneVp(@Body VpResponseVo vpResponseVo);
    @POST
    Call<ResponseBody> resposneUniVp(@Url String url, @Body VpResponseVo vpResponseVo);


}


