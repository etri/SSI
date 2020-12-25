package com.iitp.iitp_demo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserDataVo implements Serializable{
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("address")
    @Expose
    public String address;
    @SerializedName("issuer")
    @Expose
    public String issuer;

    public UserDataVo(String name, String id, String address, String issuer){
        this.name = name;
        this.id = id;
        this.address = address;
        this.issuer = issuer;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public String getIssuer(){
        return issuer;
    }

    public void setIssuer(String issuer){
        this.issuer = issuer;
    }
}
