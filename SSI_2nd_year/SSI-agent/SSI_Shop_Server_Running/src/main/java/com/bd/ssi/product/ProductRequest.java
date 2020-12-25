package com.bd.ssi.product;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ProductRequest {
    private int productId;
    private int usedProductId;
    private String productName;
    private String description;
    private String type;
    private Long price;
    private String address;
    private String did;
    private String didSelected;
    private String img;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getUsedProductId() {
        return usedProductId;
    }

    public void setUsedProductId(int usedProductId) {
        this.usedProductId = usedProductId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return "ProductRequest{" +
                "productId=" + productId +
                ", usedProductId=" + usedProductId +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", price=" + price +
                ", address='" + address + '\'' +
                ", did='" + did + '\'' +
                ", didSelected='" + didSelected + '\'' +
                ", img='" + img + '\'' +
                '}';
    }
}
