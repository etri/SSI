package com.iitp.iitp_demo.api.didResolver.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DidDocumentVo{
    @SerializedName("@context")
    @Expose
    public String context;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("service")
    @Expose
    public List<Object> service = null;
    @SerializedName("authentication")
    @Expose
    public List<Object> authentication = null;
    @SerializedName("publicKey")
    @Expose
    public List<PublicKeyVo> publicKey = null;

    public String getContext(){
        return context;
    }

    public void setContext(String context){
        this.context = context;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public List<Object> getService(){
        return service;
    }

    public void setService(List<Object> service){
        this.service = service;
    }

    public List<Object> getAuthentication(){
        return authentication;
    }

    public void setAuthentication(List<Object> authentication){
        this.authentication = authentication;
    }

    public List<PublicKeyVo> getPublicKey(){
        return publicKey;
    }

    public void setPublicKey(List<PublicKeyVo> publicKey){
        this.publicKey = publicKey;
    }
}
