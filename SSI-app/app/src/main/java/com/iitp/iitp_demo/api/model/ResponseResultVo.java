package com.iitp.iitp_demo.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ResponseResultVo implements Serializable{
    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("result")
    @Expose
    public List<String> result;

    public Boolean getSuccess(){
        return success;
    }

    public void setSuccess(Boolean success){
        this.success = success;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public List<String> getResult(){
        return result;
    }

    public void setResult(List<String> result){
        this.result = result;
    }
}
