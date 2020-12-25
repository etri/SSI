package com.iitp.iitp_demo.api.didResolver.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PublicKeyVo{
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("created")
    @Expose
    public Integer created;
    @SerializedName("publicKeyBase64")
    @Expose
    public String publicKeyBase64;
    @SerializedName("revoked")
    @Expose
    public Integer revoked;

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public Integer getCreated(){
        return created;
    }

    public void setCreated(Integer created){
        this.created = created;
    }

    public String getPublicKeyBase64(){
        return publicKeyBase64;
    }

    public void setPublicKeyBase64(String publicKeyBase64){
        this.publicKeyBase64 = publicKeyBase64;
    }

    public Integer getRevoked(){
        return revoked;
    }

    public void setRevoked(Integer revoked){
        this.revoked = revoked;
    }
}
