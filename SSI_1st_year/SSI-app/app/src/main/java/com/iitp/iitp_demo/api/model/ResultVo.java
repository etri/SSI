package com.iitp.iitp_demo.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ResultVo implements Serializable{
    @SerializedName("credential")
    @Expose
    public String credential;
    @SerializedName("propertyName")
    @Expose
    public String propertyName;
    @SerializedName("issuerDid")
    @Expose
    public String issuerDid;
    @SerializedName("issuerName")
    @Expose
    public String issuerName;

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getIssuerDid() {
        return issuerDid;
    }

    public void setIssuerDid(String issuerDid) {
        this.issuerDid = issuerDid;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

}
