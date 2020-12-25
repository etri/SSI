package com.bd.ssi.product;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;
    @Column
    private String productName;

    @Column
    private String type;

    @Column
    private String description;
    @Column
    private Long price;
    @Column
    private String did;                     //로그인한 DID
    @Column
    private String didSelected;             //"DID선택"으로 선택한 DID
    @Column
    private String address;
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @Column
    private String createUser;

    //물품VC 관련 정보
    @Column
    private String productDid;              //물품DID

    //제조사VC 관련 정보
    @Column
    private String manufacturerDid;         //제조사DID
    @Column
    private String manufacturer;            //제조사
    @Column
    private String madeDate;                //제조일
    @Column
    private String serialNum;               //Serial Number
    @Column
    private String productWallet;           //물품정보 Wallet
    @Column
    private String manufacturerWallet;      //제조사 정보 Wallet
    @Column
    private int buyCnt;                     //구매수량
    @Column
    private int reviewCnt;                  //리뷰수

    @Transient
    private List<ProductImage> images;

    @PrePersist
    public void prePersist(){
        createDate = LocalDateTime.now();
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getDidSelected() {
        return didSelected;
    }

    public void setDidSelected(String didSelected) {
        this.didSelected = didSelected;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProductDid() {
        return productDid;
    }

    public void setProductDid(String productDid) {
        this.productDid = productDid;
    }

    public String getManufacturerDid() {
        return manufacturerDid;
    }

    public void setManufacturerDid(String manufacturerDid) {
        this.manufacturerDid = manufacturerDid;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getMadeDate() {
        return madeDate;
    }

    public void setMadeDate(String madeDate) {
        this.madeDate = madeDate;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public String getProductWallet() {
        return productWallet;
    }

    public void setProductWallet(String productWallet) {
        this.productWallet = productWallet;
    }

    public String getManufacturerWallet() {
        return manufacturerWallet;
    }

    public void setManufacturerWallet(String manufacturerWallet) {
        this.manufacturerWallet = manufacturerWallet;
    }

    public int getBuyCnt() {
        return buyCnt;
    }

    public void setBuyCnt(int buyCnt) {
        this.buyCnt = buyCnt;
    }

    public int getReviewCnt() {
        return reviewCnt;
    }

    public void setReviewCnt(int reviewCnt) {
        this.reviewCnt = reviewCnt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", did='" + did + '\'' +
                ", didSelected='" + didSelected + '\'' +
                ", address='" + address + '\'' +
                ", createDate=" + createDate +
                ", createUser='" + createUser + '\'' +
                ", productDid='" + productDid + '\'' +
                ", manufacturerDid='" + manufacturerDid + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", madeDate='" + madeDate + '\'' +
                ", serialNum='" + serialNum + '\'' +
                ", productWallet='" + productWallet + '\'' +
                ", manufacturerWallet='" + manufacturerWallet + '\'' +
                ", buyCnt=" + buyCnt +
                ", reviewCnt=" + reviewCnt +
                ", images=" + images +
                '}';
    }
}


