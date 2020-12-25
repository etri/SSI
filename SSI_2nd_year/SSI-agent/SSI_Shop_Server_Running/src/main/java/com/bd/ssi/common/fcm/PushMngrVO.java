package com.bd.ssi.common.fcm;

public class PushMngrVO {
    private String idx;
    private String did;
    private String token;
    private String createDate;
    private String updateDate;

    private String dealId;
    private String productName;
    private String didSeller;
    private String buyerToken;
    private String sellerToken;

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDidSeller() {
        return didSeller;
    }

    public void setDidSeller(String didSeller) {
        this.didSeller = didSeller;
    }

    public String getBuyerToken() {
        return buyerToken;
    }

    public void setBuyerToken(String buyerToken) {
        this.buyerToken = buyerToken;
    }

    public String getSellerToken() {
        return sellerToken;
    }

    public void setSellerToken(String sellerToken) {
        this.sellerToken = sellerToken;
    }

    @Override
    public String toString() {
        return "PushMngrVO{" +
                "idx='" + idx + '\'' +
                ", did='" + did + '\'' +
                ", token='" + token + '\'' +
                ", createDate='" + createDate + '\'' +
                ", updateDate='" + updateDate + '\'' +
                ", dealId='" + dealId + '\'' +
                ", productName='" + productName + '\'' +
                ", didSeller='" + didSeller + '\'' +
                ", buyerToken='" + buyerToken + '\'' +
                ", sellerToken='" + sellerToken + '\'' +
                '}';
    }
}
