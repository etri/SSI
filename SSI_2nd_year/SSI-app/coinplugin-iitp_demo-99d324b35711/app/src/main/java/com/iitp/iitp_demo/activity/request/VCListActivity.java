package com.iitp.iitp_demo.activity.request;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.credential.CredentialDetailActivity;
import com.iitp.iitp_demo.activity.model.CredentialData;
import com.iitp.iitp_demo.activity.model.ProductVC;
import com.iitp.iitp_demo.activity.model.VCListVo;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.databinding.ActivityVcListBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.metadium.did.MetadiumWallet;
import com.metadium.did.crypto.MetadiumKey;
import com.metadium.did.exception.DidException;
import com.metadium.did.protocol.MetaDelegator;
import com.nimbusds.jwt.JWTClaimsSet;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.iitp.iitp_demo.Constants.JWT_DATA;
import static com.iitp.iitp_demo.chain.VCVPCreater.ProductCredential;
import static com.iitp.iitp_demo.chain.VCVPCreater.ProductProofCredential;

public class VCListActivity extends BaseActivity{

    private ActivityVcListBinding layout;
    private int index = 0;
    private List<CredentialData> listData = new ArrayList<>();
    private List<String> vcList = new ArrayList<>();
    private String type = null;
    private CredentialListAdapter adapter = new CredentialListAdapter();
    private PreferenceUtil preferenceUtil;
    private MetaDelegator delegator;
    private String prdouctDID;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        PrintLog.e("VCListActivity");
        preferenceUtil = PreferenceUtil.getInstance(this);
        delegator = new MetaDelegator("https://testdelegator.metadium.com", "https://api.metadium.com/dev");
        if(intent != null){
            Uri uri = getIntent().getData();
            if(uri != null){
                String uriDecode = null;
                try{
                    uriDecode = URLDecoder.decode(uri.toString(), "UTF-8");
                }catch(UnsupportedEncodingException e){
                    PrintLog.e("VCListActivity error");
                }

                Log.e("TEST", "uri : " + uriDecode);
                Set<String> queryList = uri.getQueryParameterNames();

                layout = DataBindingUtil.setContentView(this, R.layout.activity_vc_list);
                layout.appbarTitle.setText("수신 VC 목록");
                init();
                String vc = uri.getQueryParameter("vc");
                PrintLog.e("vc = " + vc);
                if(vc.equals("fasle") || vc.equals("null")){
                    ToastUtils.custom(Toast.makeText(VCListActivity.this, "보증서 요청 오류", Toast.LENGTH_LONG)).show();
//                    sampleTxData();
                }else if(vc == null || vc.equals("null")){
                    setResult(RESULT_OK);
                    finish();
                }else{
                    VCListVo vo = IITPApplication.gson.fromJson(vc, VCListVo.class);
                    List<String> data = vo.getVc();
                    for(String temp : data){
                        PrintLog.e("vc Data= " + temp);
                        vcList.add(temp);
                    }
                    prdouctDID = getProductDID(vcList.get(0));
                    PrintLog.e("prdouctDID = " + prdouctDID);
                    String walletJson = preferenceUtil.getWalletJson(prdouctDID);
                    PrintLog.e("wallet = " + walletJson);
                    if(walletJson == null){
                        PrintLog.e("========================error");

                    }
                    String type = getVCType(vcList.get(0));
                    ProductVC productVC = null;
                    if(type.equals("ProductCredential")){
                        PrintLog.e("vcList.get(0) = " + vcList.get(0));
                        productVC = new ProductVC(vcList.get(0), vcList.get(1), walletJson);
                    }else{
                        PrintLog.e("vcList.get(1) = " + vcList.get(1));
                        productVC = new ProductVC(vcList.get(1), vcList.get(0), walletJson);
                    }

//                    checkkeyTest(productVC);
                    saveProductVC(productVC);
                }
            }
        }

    }

    private void checkkeyTest(ProductVC productData){

        new Thread(() -> {

            try{
                MetadiumWallet sellerwallet = null;
                //구매자
//                MetadiumWallet wallet = MetadiumWallet.createDid(delegator);
                MetadiumKey newKey = null; // Getting key
                try{
                    newKey = new MetadiumKey();
                }catch(InvalidAlgorithmParameterException e){
                    e.printStackTrace();
                }
                String newsignature = delegator.signAddAssocatedKeyDelegate(prdouctDID, newKey);
                BigInteger publicKey = newKey.getPublicKey();
                //
                String sellerWalletJson = productData.getWalletJson();
                PrintLog.e("wallet json = " + sellerWalletJson);
                try{
                    sellerwallet = MetadiumWallet.fromJson(sellerWalletJson);
                }catch(ParseException e){
                    e.printStackTrace();
                }
                PrintLog.e("prdouctDID =" + prdouctDID);

//                String walletJson = "{\"private_key\":\"d4d0a80b2d57f0ca6860fdfef1e39afd58695b9face5ca079b336d954ec85be8\",\"did\":\"did:meta:testnet:000000000000000000000000000000000000000000000000000000000000211b\"}";
                sellerwallet.updateKeyOfDid(delegator, publicKey, newsignature);
                MetadiumWallet newWallet = new MetadiumWallet(prdouctDID, newKey);

            }catch(DidException e){
                e.printStackTrace();
            }
        }).start();

    }

    private String getProductDID(String vc){
        String did = null;
        String payload = preferenceUtil.getPayload(vc);
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        JWTClaimsSet jwtClaimsSet = null;
        try{
            jwtClaimsSet = JWTClaimsSet.parse(payload);
        }catch(ParseException e){
            PrintLog.e("getProductDID error");
        }
        VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
        PrintLog.e("did = " + credential.toJSONString());
        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
        for(String temp : claims.keySet()){
            PrintLog.e(temp + " : " + claims.get(temp));
        }
        if(claims.get("id") != null){
            did = claims.get("id").toString();
        }else if(claims.get("ProductCredential_id") != null){
            did = claims.get("ProductCredential_id").toString();
        }

        PrintLog.e("did = " + did);
        return did;
    }

    private String getVCType(String vc){
        String payload = preferenceUtil.getPayload(vc);
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        JWTClaimsSet jwtClaimsSet = null;
        try{
            jwtClaimsSet = JWTClaimsSet.parse(payload);
        }catch(ParseException e){
            PrintLog.e("getVCType error");
        }
        VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
        List<String> list = new ArrayList<>(credential.getTypes());
        String type = list.get(1);


        PrintLog.e("type " + type);
        return type;
    }

//    private void sampleTxData(){
//        MetaDelegator delegator = new MetaDelegator("https://testdelegator.metadium.com", "https://api.metadium.com/dev");
//        ArrayList<DidDataVo> didList = MainActivity.getDidList(VCListActivity.this);
//        DidDataVo didData = null;
//        String metadid = preferenceUtil.getMetaDID();
//        for(DidDataVo data : didList){
//            if(data.getDid().equals(metadid)){
//                didData = data;
//                PrintLog.e("did = " + didData.getDid());
//            }
//        }
//        ECKeyPair ecKeyPair = getManagementECKeyPair(didData.getMnemonic());
//
//        MetadiumKey newKey = new MetadiumKey(ecKeyPair);
//        MetadiumWallet sellerWallet = new MetadiumWallet(didData.getDid(), newKey);
//        VCVPCreater vcvpCreater = VCVPCreater.getInstance();
//        vcData = new MutableLiveData<>();
//        vcData.observe(this, pushListVos -> {
//            PrintLog.e("vcdata = " + vcData.getValue());
//            vcList.add(vcData.getValue());
//            String vcdat = vcvpCreater.vcCreateProductionCredential();
//            PrintLog.e("vcdat = " + vcdat);
//            vcList.add(vcdat);
////            saveProductVC(vcList);
//        });
//        new Thread(() -> {
//            try{
//                try{
//                    String vc = null;
//                    vc = vcvpCreater.TxproofTest("pruductDID",sellerWallet, sellerWallet.getDid(), BigInteger.valueOf(16000), delegator.currentBlockNumber(), null);
//                    vcData.postValue(vc);
//                }catch(IOException e){
//                    e.printStackTrace();
//                }
//            }catch(Exception e){
//                // error
//                e.printStackTrace();
//            }
//        }).start();
//
//
//    }

    /**
     * 일반 VC 처리
     */
    private void setVCData(){
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        for(String jwt : vcList){

            String payload = preferenceUtil.getPayload(jwt);
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("setVCData error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            String issuer = credential.getIssuer().toString();
            List<String> list = new ArrayList<>(credential.getTypes());
            type = list.get(1);
            PrintLog.e("jwt = " + jwt);
            PrintLog.e("type = " + type);
            String desc = "";
            PrintLog.e("issuer = " + issuer);
            Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
            desc = "";
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(!key.equals("OldProductProofCredential")){
                    if(desc.length() == 0){
                        desc = VCVPCreater.getInstance().getKor(key);
                    }else{

                        desc = desc + ", " + VCVPCreater.getInstance().getKor(key);
                    }
                }

            }
            PrintLog.e("desc = " + desc);
            CommonPreference.getInstance(VCListActivity.this).getSecureSharedPreferences().edit().putString(type, jwt).apply();
            listData.add(new CredentialData(credential, desc, issuer, type));
        }
        layout.credentialList.setAdapter(adapter);
    }

    private void init(){
        setVCData();
        layout.okBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        PrintLog.e("onDestroy");
    }


    /**
     * list adapter
     */
    class CredentialListAdapter extends BaseAdapter{

        CredentialListAdapter(){

        }

        @Override
        public int getCount(){
            return listData.size();
        }

        @Override
        public Object getItem(int i){
            return null;
        }

        @Override
        public long getItemId(int i){
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent){
            CredentialListAdapter.Holder holder;
            CredentialData data = listData.get(i);
            String issuerType = data.getType();
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_credential_2, parent, false);
                holder = new CredentialListAdapter.Holder();
                holder.icon = convertView.findViewById(R.id.icon);
                holder.desc1 = convertView.findViewById(R.id.desc1);
                holder.desc2 = convertView.findViewById(R.id.desc2);
                holder.layout = convertView.findViewById(R.id.layout);
                convertView.setTag(holder);
            }else{
                holder = (CredentialListAdapter.Holder) convertView.getTag();
            }


            holder.desc2.setText(data.getDesc1());
            holder.layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent intent = new Intent(VCListActivity.this, CredentialDetailActivity.class);
                    intent.putExtra(JWT_DATA, vcList.get(i));
                    startActivity(intent);
                }
            });
            PrintLog.e("issuer type = " + issuerType);
            switch(issuerType){
                case "IdentificationCredential":
                    holder.icon.setBackgroundResource(R.drawable.ic_list_item_govern);
                    holder.desc1.setText("신분증");
                    break;
                case "CardTokenCredential":
                    holder.icon.setBackgroundResource(R.drawable.ic_list_item_card);
                    holder.desc1.setText(R.string.cardVC1);
                    break;
                case "AddressCredential":
                    holder.icon.setBackgroundResource(R.drawable.ic_list_item_post);
                    holder.desc1.setText(R.string.addressVC);
                    break;
                case "GraduationCredential":
                case "EmployeeCredential":
                    holder.icon.setBackgroundResource(R.drawable.uni);
                    break;
                case "DelegatedVC":
                    holder.desc1.setText("위임장");
                    holder.icon.setBackgroundResource(R.drawable.ic_list_item_vc);
                    break;
                case ProductCredential:
                    holder.desc1.setText("물품정보VC");
                    holder.icon.setBackgroundResource(R.drawable.market);
                    break;
                case ProductProofCredential:
                    holder.desc1.setText("거래증명VC");
                    holder.icon.setBackgroundResource(R.drawable.market);
                    break;
                default:
                    break;
            }
            return convertView;
        }

        /**
         * holder
         */
        class Holder{
            ImageView icon;
            TextView desc1;
            TextView desc2;
            ConstraintLayout layout;
        }
    }

    private void saveProductVC(ProductVC productVC){
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        for(String data : vcList){
            String payload = preferenceUtil.getPayload(data);
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("saveProductVC error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            String issuer = credential.getIssuer().toString();
            List<String> list = new ArrayList<>(credential.getTypes());
            type = list.get(1);
            PrintLog.e("jwt = " + data);
            PrintLog.e("type = " + type);
            String desc = "";
            PrintLog.e("issuer = " + issuer);
            Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(!(key.equals("OldProductProofCredential")||key.equals("user_id"))){
                    if(desc.length() == 0){
                        desc = VCVPCreater.getInstance().getKor(key);
                    }else{
                        desc = desc + ", " + VCVPCreater.getInstance().getKor(key);
                    }
                }
            }
            PrintLog.e("desc = " + desc);
            PrintLog.e("type = " + type);
            PrintLog.e("data = " + data);
            listData.add(new CredentialData(credential, desc, issuer, type));
            adapter.notifyDataSetChanged();
        }
        preferenceUtil.setProductVC(productVC);
    }
}

