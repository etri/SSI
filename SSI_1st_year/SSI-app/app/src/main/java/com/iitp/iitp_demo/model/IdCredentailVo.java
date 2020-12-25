package com.iitp.iitp_demo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class IdCredentailVo{
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("RRN")
    @Expose
    public String RRN;
    @SerializedName("issueDate")
    @Expose
    public String issueDate;
    @SerializedName("issuer")
    @Expose
    public String issuer;
    @SerializedName("address")
    @Expose
    public String address;

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getRRN(){
        return RRN;
    }

    public void setRRN(String RRN){
        this.RRN = RRN;
    }

    public String getIssueDate(){
        return issueDate;
    }

    public void setIssueDate(String issueDate){
        this.issueDate = issueDate;
    }

    public String getIssuer(){
        return issuer;
    }

    public void setIssuer(String issuer){
        this.issuer = issuer;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address = address;
    }
}
