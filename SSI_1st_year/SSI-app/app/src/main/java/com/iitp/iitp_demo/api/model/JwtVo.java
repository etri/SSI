package com.iitp.iitp_demo.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JwtVo{
    @SerializedName("jwt")
    @Expose
    public String jwt;

    public JwtVo(String jwt){
        this.jwt = jwt;
    }

    public String getJwt(){
        return jwt;
    }

    public void setJwt(String jwt){
        this.jwt = jwt;
    }
}
