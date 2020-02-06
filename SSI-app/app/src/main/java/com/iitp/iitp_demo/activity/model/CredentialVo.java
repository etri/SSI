package com.iitp.iitp_demo.activity.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CredentialVo{

    @SerializedName("sub")
    @Expose
    public String sub;
    @SerializedName("iat")
    @Expose
    public Integer iat;
    @SerializedName("exp")
    @Expose
    public Integer exp;
    @SerializedName("iss")
    @Expose
    public String iss;
    @SerializedName("nonce")
    @Expose
    public String nonce;
    @SerializedName("type")
    @Expose
    public List<String> type = null;
    @SerializedName("version")
    @Expose
    public String version;
    @SerializedName("claim")
    @Expose
    public ClaimVo claim;

    public String getSub(){
        return sub;
    }

    public void setSub(String sub){
        this.sub = sub;
    }

    public Integer getIat(){
        return iat;
    }

    public void setIat(Integer iat){
        this.iat = iat;
    }

    public Integer getExp(){
        return exp;
    }

    public void setExp(Integer exp){
        this.exp = exp;
    }

    public String getIss(){
        return iss;
    }

    public void setIss(String iss){
        this.iss = iss;
    }

    public String getNonce(){
        return nonce;
    }

    public void setNonce(String nonce){
        this.nonce = nonce;
    }

    public List<String> getType(){
        return type;
    }

    public void setType(List<String> type){
        this.type = type;
    }

    public String getVersion(){
        return version;
    }

    public void setVersion(String version){
        this.version = version;
    }

    public ClaimVo getClaim(){
        return claim;
    }

    public void setClaim(ClaimVo claim){
        this.claim = claim;
    }
}
