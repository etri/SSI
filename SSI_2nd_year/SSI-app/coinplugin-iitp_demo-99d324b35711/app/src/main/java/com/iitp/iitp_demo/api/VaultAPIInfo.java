package com.iitp.iitp_demo.api;

import com.iitp.iitp_demo.api.model.AuthBackupDataVo;
import com.iitp.iitp_demo.api.model.AuthBackupVo;
import com.iitp.iitp_demo.api.model.AuthIdVo;
import com.iitp.iitp_demo.api.model.AuthInitResponseResultVo;
import com.iitp.iitp_demo.api.model.AuthRecoveryDataVo;
import com.iitp.iitp_demo.api.model.AuthResponseVo;
import com.iitp.iitp_demo.api.model.AuthVerifyVo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface VaultAPIInfo{

    public final static String BaseUrl = "http://129.254.194.142:8080/"; //master
    public final static String Storage1 = "http://129.254.194.145:8080/"; //storage 1
    public final static String Storage2 = "http://129.254.194.158:8080/"; //storage 2
    @Headers("Content-Type:application/json")
    @POST("v1/auth/initiate")
    Call<AuthResponseVo> requestMasterAuth(@Body AuthIdVo authId);

    @Headers("Content-Type:application/json")
    @POST("v1/auth/verify")
    Call<AuthResponseVo> requestMasterVerify(@Body AuthVerifyVo verifyVo);

    @Headers("Content-Type:application/json")
    @POST
    Call<AuthResponseVo> requestStorage1Auth(@Url String url,@Body AuthIdVo authId);

    @Headers("Content-Type:application/json")
    @POST
    Call<AuthResponseVo> requestStorage1Verify(@Url String url, @Body AuthVerifyVo verifyVo);

    @Headers("Content-Type:application/json")
    @POST
    Call<AuthResponseVo> requestStorage2Auth(@Url String url,@Body AuthIdVo authId);

    @Headers("Content-Type:application/json")
    @POST
    Call<AuthResponseVo> requestStorage2Verify(@Url String url, @Body AuthVerifyVo verifyVo);


    @Headers("Content-Type:application/json")
    @POST("v1/vault/master")
    Call<AuthResponseVo> requestBackupMaster(@Body AuthBackupVo authBackupVo);

    @Headers("Content-Type:application/json")
    @POST
    Call<AuthResponseVo> requestBackupStorage(@Url String url, @Body AuthBackupDataVo authBackupDataVo);


    @Headers("Content-Type:application/json")
    @POST("v1/vault/restore")
    Call<AuthResponseVo> requestRecoveryMaster(@Body AuthRecoveryDataVo json);

    @Headers("Content-Type:application/json")
    @POST
    Call<AuthResponseVo> requestRecoveryStorage(@Url String url, @Body AuthRecoveryDataVo authRecoveryDataVo);

}


