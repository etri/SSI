package com.iitp.iitp_demo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OfficeCredentailVo{
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("department")
    @Expose
    public String department;
    @SerializedName("birthday")
    @Expose
    public String birthday;
    @SerializedName("address")
    @Expose
    public String address;

    @SerializedName("issuer")
    @Expose
    public String issuer;

    @SerializedName("startDate")
    @Expose
    public String startDate;

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getDepartment(){
        return department;
    }

    public void setDepartment(String department){
        this.department = department;
    }

    public String getBirthday(){
        return birthday;
    }

    public void setBirthday(String birthday){
        this.birthday = birthday;
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

    public String getStartDate(){
        return startDate;
    }

    public void setStartDate(String startDate){
        this.startDate = startDate;
    }
}
