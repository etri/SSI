package com.iitp.iitp_demo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DidVo{
    @SerializedName("did")
    @Expose
    public String did;

    @SerializedName("idCredential")
    @Expose
    public String idCredential;
    @SerializedName("idCredentialJson")
    @Expose
    public String idCredentialJson;

    @SerializedName("officeCredential")
    @Expose
    public String officeCredential;

    @SerializedName("officeCredentialJson")
    @Expose
    public String officeCredentialJson;


    @SerializedName("address")
    @Expose
    public String address;

    @SerializedName("privateKey")
    @Expose
    public String privateKey;

    @SerializedName("publicKey")
    @Expose
    public String publicKey;

    @SerializedName("metaId")
    @Expose
    public String metaId;

    @SerializedName("metaIdOrg")
    @Expose
    public String metaIdOrg;

    @SerializedName("iss")
    @Expose
    public String iss;

    public String getDid(){
        return did;
    }

    public void setDid(String did){
        this.did = did;
    }

    public String getIdCredential(){
        return idCredential;
    }

    public void setIdCredential(String idCredential){
        this.idCredential = idCredential;
    }

    public String getOfficeCredential(){
        return officeCredential;
    }

    public void setOfficeCredential(String officeCredential){
        this.officeCredential = officeCredential;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public String getPrivateKey(){
        return privateKey;
    }

    public void setPrivateKey(String privateKey){
        this.privateKey = privateKey;
    }

    public String getPublicKey(){
        return publicKey;
    }

    public void setPublicKey(String publicKey){
        this.publicKey = publicKey;
    }

    public String getMetaId(){
        return metaId;
    }

    public void setMetaId(String metaId){
        this.metaId = metaId;
    }

    public String getMetaIdOrg(){
        return metaIdOrg;
    }

    public void setMetaIdOrg(String metaIdOrg){
        this.metaIdOrg = metaIdOrg;
    }

    public String getIss(){
        return iss;
    }

    public void setIss(String iss){
        this.iss = iss;
    }

    public String getIdCredentialJson(){
        return idCredentialJson;
    }

    public void setIdCredentialJson(String idCredentialJson){
        this.idCredentialJson = idCredentialJson;
    }

    public String getOfficeCredentialJson(){
        return officeCredentialJson;
    }

    public void setOfficeCredentialJson(String officeCredentialJson){
        this.officeCredentialJson = officeCredentialJson;
    }
}
