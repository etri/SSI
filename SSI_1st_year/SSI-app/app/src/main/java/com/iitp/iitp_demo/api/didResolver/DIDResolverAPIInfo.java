package com.iitp.iitp_demo.api.didResolver;

import com.iitp.iitp_demo.BuildConfig;
import com.iitp.iitp_demo.api.didResolver.model.DIDDocVo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DIDResolverAPIInfo{

    //server Url
    public final static String BaseUrl = BuildConfig.MAIN_NET ? "http://129.254.194.103:9000/1.0/" : "http://129.254.194.103:9000/1.0/";
//"http://129.254.194.103:9000/1.0/"
    @GET("identifiers/{icondid}")
    Call<DIDDocVo> getDIDData(@Path(value = "icondid") String iconid);


}
