package com.bd.ssi.deal;

import com.bd.ssi.auth.User;
import com.bd.ssi.product.Product;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dealId;

    @JoinColumn(name="productId")
    @ManyToOne
    private Product product;

    @JoinColumn(name="buyer")
    @ManyToOne
    private User buyer;

    @Column
    private String buyerName;

    @Column
    private String request;
    @Column
    private String paymentMethod;
    @Column
    private String paymentCard;

    @Column
    private String state; //[ 결제대기 | 결제완료 | 배송중 | 배송완료 | 구매확정 ]
    @Column
    private Integer count;
    @Column
    private String pricePerOne;
    @Column
    private String totalPrice;
    @Column
    private String phone;
    @Column
    private String address;

    @Column
    private String did;             //로그인 DID
    @Column
    private String didSeller;       //판매자 DID
    @Column
    private String didSelected;     //"DID선택" 버튼으로 선택한 DID. 구매자 DID
    @Column
    private String didBuyer;        //"DID선택" 버튼으로 선택한 DID. SSI APP에서 넘겨주는 값. didSelected는 등록시 선택한 DID. 구매자 DID
    @Column
    private String publicKey;
    @Column
    private String vcState;         // [미발급 | 발급요청 | 발급완료]
    
    @Column
    private String buyNSell;        // 'B'이면 구매내역 리스트에 표시, 'S'이면 판매내역 리스트에 표시

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dealDate;

    private String etc;

    private String sign;

    @PrePersist
    public void prePersist(){
        dealDate = LocalDateTime.now();
    }

    public Integer getDealId() {
        return dealId;
    }

    public void setDealId(Integer dealId) {
        this.dealId = dealId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentCard() {
        return paymentCard;
    }

    public void setPaymentCard(String paymentCard) {
        this.paymentCard = paymentCard;
    }

    public LocalDateTime getDealDate() {
        return dealDate;
    }

    public void setDealDate(LocalDateTime dealDate) {
        this.dealDate = dealDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getPricePerOne() {
        return pricePerOne;
    }

    public void setPricePerOne(String pricePerOne) {
        this.pricePerOne = pricePerOne;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getDidSeller() {
        return didSeller;
    }

    public void setDidSeller(String didSeller) {
        this.didSeller = didSeller;
    }

    public String getDidSelected() {
        return didSelected;
    }

    public void setDidSelected(String didSelected) {
        this.didSelected = didSelected;
    }

    public String getDidBuyer() {
        return didBuyer;
    }

    public void setDidBuyer(String didBuyer) {
        this.didBuyer = didBuyer;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getBuyNSell() {
        return buyNSell;
    }

    public void setBuyNSell(String buyNSell) {
        this.buyNSell = buyNSell;
    }

    public String getVcState() {
        return vcState;
    }

    public void setVcState(String vcState) {
        this.vcState = vcState;
    }

    public String getEtc() {
        return etc;
    }

    public void setEtc(String etc) {
        this.etc = etc;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "Deal{" +
                "dealId=" + dealId +
                ", product=" + product +
                ", buyer=" + buyer +
                ", buyerName='" + buyerName + '\'' +
                ", request='" + request + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentCard='" + paymentCard + '\'' +
                ", state='" + state + '\'' +
                ", count=" + count +
                ", pricePerOne='" + pricePerOne + '\'' +
                ", totalPrice='" + totalPrice + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", did='" + did + '\'' +
                ", didSeller='" + didSeller + '\'' +
                ", didSelected='" + didSelected + '\'' +
                ", didBuyer='" + didBuyer + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", vcState='" + vcState + '\'' +
                ", buyNSell='" + buyNSell + '\'' +
                ", dealDate=" + dealDate +
                ", etc='" + etc + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
