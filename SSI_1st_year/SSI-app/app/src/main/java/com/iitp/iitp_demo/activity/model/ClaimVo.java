package com.iitp.iitp_demo.activity.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.iitp.iitp_demo.util.PrintLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClaimVo{
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
    @SerializedName("RRN")
    @Expose
    public String rRN;
    @SerializedName("issueDate")
    @Expose
    public String issueDate;

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
        try{
            Date date = new SimpleDateFormat("yyyyMMdd").parse(startDate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
            String changeDate = sdf.format(date);
            PrintLog.e("Date = date");
            return changeDate;
        }catch(ParseException e){
            e.printStackTrace();
        }

        return startDate;
    }

    public void setStartDate(String startDate){
        this.startDate = startDate;
    }

    public String getrRN(){
        return rRN;
    }

    public void setrRN(String rRN){
        this.rRN = rRN;
    }

    public String getIssueDate(){
        try{
            Date date = new SimpleDateFormat("yyyyMMdd").parse(issueDate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
            String changeDate = sdf.format(date);
            return changeDate;
        }catch(ParseException e){
            e.printStackTrace();
        }

        return issueDate;
    }

    public void setIssueDate(String issueDate){
        this.issueDate = issueDate;
    }
}
