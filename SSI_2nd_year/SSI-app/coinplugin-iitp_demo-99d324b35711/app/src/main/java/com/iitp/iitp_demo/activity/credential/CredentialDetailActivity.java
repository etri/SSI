package com.iitp.iitp_demo.activity.credential;

import android.content.Intent;
import android.icu.text.NumberFormat;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.databinding.ActivityCredentailDetailBinding;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.iitp.iitp_demo.Constants.JWT_DATA;
import static com.iitp.iitp_demo.chain.VCVPCreater.DELEGATOR_VC;
import static com.iitp.iitp_demo.chain.VCVPCreater.ProductCredential;
import static com.iitp.iitp_demo.chain.VCVPCreater.ProductProofCredential;

public class CredentialDetailActivity extends BaseActivity{

    private ActivityCredentailDetailBinding layout;
    private String jwt;
    private String issuer;
    private String type;
    private String myDid;
    private String desc;
    private String data;
    private String blockNumber;
    private PreferenceUtil preferenceUtil;
    private int index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        layout = DataBindingUtil.setContentView(this, R.layout.activity_credentail_detail);

        layout.back.setOnClickListener(v -> onBackPressed());
        jwt = intent.getStringExtra(JWT_DATA);
        index = intent.getIntExtra("index", 0);
        preferenceUtil = PreferenceUtil.getInstance(this);
        setData();
        setTitle(type);
        setUI();
    }

    /**
     * data set
     */
    private void setUI(){
        if(!issuer.contains("did")){
            issuer = "did:sov:" + issuer;
        }
        if(!myDid.contains("did")){
            if(myDid.length() != 0){
                myDid = "did:sov:" + myDid;
            }
        }
        layout.issuerDID.setText(issuer);
        layout.userDID.setText(myDid);
        layout.dataName.setText(desc);
        layout.dataData.setText(data);
    }

    /**
     * title bar Set
     *
     * @param did issuer did
     */
    private void setTitle(String type){
        PrintLog.e("title type = " + type);
        switch(type){
            case "IdentificationCredential":
                layout.vcName.setText(R.string.idcredentialVC);
                layout.issuerName.setText("행정안전부");
                layout.appbarTitle.setText("행정안전부");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_govern);
                break;
            case "CardTokenCredential":
                if(index == 0){
                    layout.vcName.setText(R.string.cardVC1);
                    layout.issuerName.setText("한국카드");
                    layout.appbarTitle.setText("한국카드");
                }else{
                    layout.vcName.setText(R.string.cardVC2);
                    layout.issuerName.setText("서울카드");
                    layout.appbarTitle.setText("서울카드");
                }

                layout.icon.setBackgroundResource(R.drawable.ic_list_item_card);
                break;
            case "AddressCredential":
                layout.vcName.setText(R.string.addressVC);
                layout.issuerName.setText("우체국");
                layout.appbarTitle.setText("우체국");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_post);
                break;
            case "GraduationCredential":
                layout.issuerName.setText("대학");
                layout.appbarTitle.setText("대학 컨소시엄");
                layout.icon.setBackgroundResource(R.drawable.uni);
                break;
            case "DelegatedVC":
            case DELEGATOR_VC:
                layout.issuerName.setText(issuer);
                layout.appbarTitle.setText("신분증 위임장");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_vc);
                break;
            case ProductCredential:
                layout.vcName.setText("물품정보VC");
                layout.issuerName.setText("Men's Watch");
                layout.appbarTitle.setText("고가품마켓");
                layout.icon.setBackgroundResource(R.drawable.market);
                break;
            case ProductProofCredential:
                if(!issuer.equals("did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001de8")){
                    layout.issuerName.setVisibility(View.GONE);
                    layout.text1.setVisibility(View.GONE);
                }else{
                    layout.issuerName.setText("Men's Watch");
                }
                layout.vcName.setText("거래증명VC");

                layout.appbarTitle.setText("고가품마켓");
                layout.icon.setBackgroundResource(R.drawable.market);
                break;
            case "StockServiceCredential":
                layout.vcName.setText("한국증권VC");
                layout.issuerName.setText("한국증권");
                layout.appbarTitle.setText("증권사");
                layout.icon.setBackgroundResource(R.drawable.stock);
                break;
            case "PhoneCredential":
                layout.vcName.setText("한국통신VC");
                layout.issuerName.setText("한국통신");
                layout.appbarTitle.setText("통신사");
                layout.icon.setBackgroundResource(R.drawable.phone);
                break;
            case "LoginCredential":
                layout.vcName.setText("Men's Watch VC");
                layout.issuerName.setText("Men's Watch");
                layout.appbarTitle.setText("고가품마켓");
                layout.icon.setBackgroundResource(R.drawable.market);
                break;
            default:
                break;
        }
    }

    /**
     * set claim data
     */
    private void setData(){
        String payload = preferenceUtil.getPayload(jwt);
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        JWTClaimsSet jwtClaimsSet = null;
        try{
            jwtClaimsSet = JWTClaimsSet.parse(payload);
        }catch(ParseException e){
            PrintLog.e("setData error");
        }
        VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
        issuer = credential.getIssuer().toString();
        List<String> listTypes = new ArrayList<>(credential.getTypes());
        PrintLog.e("type = " + listTypes.get(1));
        type = listTypes.get(1);
        desc = "";
        data = "";
        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
        if(type.equals("IdentificationCredential")){
            layout.idCredentialLayout.setVisibility(View.VISIBLE);
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(key.equals("name")){
                    layout.idNameTv.setText(claims.get(key).toString());
                }else if(key.equals("birth_date")){
                    layout.idBirthTv.setText(claims.get(key).toString());
                }else if(key.equals("address")){
                    layout.idAddressTv.setText(claims.get(key).toString());
                }else if(key.equals("id")){
                    myDid = claims.get(key).toString();
                }else if(key.equals("mobile")){

                }
            }
        }else if(type.equals("LoginCredential")){
            layout.loginLayout.setVisibility(View.VISIBLE);
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(key.equals("name")){
                    layout.loginNameTv.setText(claims.get(key).toString());
                }else if(key.equals("birth_date")){
                    layout.loginBirthTv.setText(claims.get(key).toString());
                }else if(key.equals("address")){
                    layout.loginAddressTv.setText(claims.get(key).toString());
                }else if(key.equals("id")){
                    myDid = claims.get(key).toString();
                }else if(key.equals("phone_num")){
                    layout.loginMobileTv.setText(claims.get(key).toString());
                }
            }
        }else if(type.equals("CardTokenCredential")){
            layout.cardCredentialLayout.setVisibility(View.VISIBLE);
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(key.equals("token")){
                    layout.cardTokenTv.setText(claims.get(key).toString());
                }else if(key.equals("id")){
                    myDid = claims.get(key).toString();
                }
            }
        }else if(type.equals("AddressCredential")){
            layout.postCredentialLayout.setVisibility(View.VISIBLE);
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(key.equals("name")){
                    layout.postNameTv.setText(claims.get(key).toString());
                }else if(key.equals("address")){
                    layout.postAddressTv.setText(claims.get(key).toString());
                }else if(key.equals("id")){
                    myDid = claims.get(key).toString();
                }
            }
        }else if(type.equals("StockServiceCredential")){
            layout.stockCredentialLayout.setVisibility(View.VISIBLE);
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(key.equals("register_id")){
                    layout.stockIdTv.setText(claims.get(key).toString());
                }else if(key.equals("name")){
                    layout.stockNameTv.setText(claims.get(key).toString());
                }else if(key.equals("start_date")){
                    layout.stockStartDateTv.setText(claims.get(key).toString());
                }else if(key.equals("address")){
                    layout.stockAddressTv.setText(claims.get(key).toString());
                }else if(key.equals("id")){
                    myDid = claims.get(key).toString();
                }
            }
        }else if(type.equals("ProductCredential")){
            layout.productCredentialLayout.setVisibility(View.VISIBLE);
            layout.text3.setText(R.string.claimProductDID);
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(key.equals("production_date")){
                    layout.dateTv.setText(claims.get(key).toString());
                }else if(key.equals("name")){
                    layout.productNameTv.setText(claims.get(key).toString());
                }else if(key.equals("SN")){
                    layout.snTv.setText(claims.get(key).toString());
                }else if(key.equals("id")){
                    myDid = claims.get(key).toString();
                }else if(key.equals("BlockNumber")){
                    blockNumber = claims.get(key).toString();
                }
            }
        }else if(type.equals("ProductProofCredential")){
            layout.productProofCredentialLayout.setVisibility(View.VISIBLE);
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(key.equals("ProductCredential_id")){
                    layout.productDIDTv.setText(claims.get(key).toString());
                }else if(key.equals("seller_id")){
                    layout.sellerDIDTv.setText(claims.get(key).toString());
                }else if(key.equals("buyer_id")){
                    layout.buyerDIDTv.setText(claims.get(key).toString());
                }else if(key.equals("user_id")){
                    myDid = claims.get(key).toString();
                }else if(key.equals("price")){
                    String price = claims.get(key).toString();
                    int priceInt = Integer.parseInt(price);
                    String formattedStringPrice = NumberFormat.getInstance(Locale.getDefault()).format(priceInt) + "원";
                    layout.priceTv.setText(formattedStringPrice);
                }else if(key.equals("sell_date")){
                    layout.sellDateTv.setText(claims.get(key).toString());
                }else if(key.equals("BlockNumber")){
                    String blockNumber = claims.get(key).toString();
                    if(blockNumber.length() > 0){
                        layout.blocknumberTv.setText(blockNumber);
                        layout.blocknumber.setVisibility(View.VISIBLE);
                    }else{
                        layout.blocknumber.setVisibility(View.GONE);
                    }
                }
            }
            if(myDid == null){
                myDid = claims.get("buyer_id").toString();
            }
        }else if(type.equals("PhoneCredential")){
            layout.phoneCredentialLayout.setVisibility(View.VISIBLE);
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(key.equals("name")){
                    layout.phoneNameTv.setText(claims.get(key).toString());
                }else if(key.equals("phone_num")){
                    layout.phoneTv.setText(claims.get(key).toString());
                }else if(key.equals("id")){
                    myDid = claims.get(key).toString();
                }
            }
        }else if(type.equals("DelegatedVC")){
            layout.phoneCredentialLayout.setVisibility(View.VISIBLE);
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(key.equals("name")){
                    layout.phoneNameTv.setText(claims.get(key).toString());
                }else if(key.equals("phone_num")){
                    layout.phoneTv.setText(claims.get(key).toString());
                }else if(key.equals("id")){
                    myDid = claims.get(key).toString();
                }
            }
        }else{
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(!key.equals("OldProductProofCredential")){
                    if(key.equals("id")){
                        myDid = claims.get(key).toString();
                    }else{
                        if(type.equals("DelegatedVC") && key.equals("DID_delegator")){
                            myDid = claims.get(key).toString();
                        }
                        if(desc.length() == 0){
                            desc = VCVPCreater.getInstance().getKor(key);
                            if(key.equals("price")){
                                String price = claims.get(key).toString();
                                int priceInt = Integer.parseInt(price);
                                String formattedStringPrice = NumberFormat.getInstance(Locale.getDefault()).format(priceInt) + "원";
                                data = formattedStringPrice;
                            }else{
                                data = claims.get(key).toString();
                            }
                        }else{
                            desc = desc + "\n\n " + VCVPCreater.getInstance().getKor(key);
                            if(key.equals("price")){
                                String price = claims.get(key).toString();
                                int priceInt = Integer.parseInt(price);
                                String formattedStringPrice = NumberFormat.getInstance(Locale.getDefault()).format(priceInt) + "원";
                                data = data + "\n\n " + formattedStringPrice;
                            }else{
                                data = data + "\n\n " + claims.get(key).toString();
                            }
                        }

                    }
                }
            }
        }
        PrintLog.e("desc = " + desc);
        PrintLog.e("data = " + data);
    }
}

