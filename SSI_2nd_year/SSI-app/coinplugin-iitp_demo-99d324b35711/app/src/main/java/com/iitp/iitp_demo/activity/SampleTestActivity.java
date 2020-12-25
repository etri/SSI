package com.iitp.iitp_demo.activity;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.KeyManager;
import com.iitp.iitp_demo.FinishListener;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestDataVoList;
import com.iitp.iitp_demo.activity.model.VpRequestNoneZKPDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPDelegateDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPReferentDataVo;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.databinding.ActivityDidDeatilBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.verifiable.util.ECKeyUtils;

import org.intellij.lang.annotations.Language;

import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreDelegationToken;

public class SampleTestActivity extends BaseActivity{

    private ActivityDidDeatilBinding layout;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_did_deatil);
        setSupportActionBar(layout.toolbar.appbar);
        setActionBarSet(layout.toolbar, getString(R.string.did_detail_title), true);
        noneZKPTest(online1);
//        ZKPTest(onlinezkp);
//        VCVPCreater.getInstance().vcCreate();
//        createIndyDid();
    }

    private String online1 = "\n" +
            "{\n" +
            "\"iss\":\"고가품 거래 서버 DID\",\n" +
            "\"iat\":\"발행 시간\",\n" +
            "\"id\":\"efef5830-b372-4a42-93e6-49c212e59b78\",\n" +
            "\"presentationURL\":\"URL\",\n" +
            "\"presentationRequest\":{\n" +
            "\"criteria\":[\n" +
            "{\n" +
            "\"nonZKP\":{\n" +
            "\"nonce\":\"123432421212\",\n" +
            "\"name\":\"login-Request\",\n" +
            "\"version\":\"0.1\",\n" +
            "\"requested_attributes\":{\n" +
            "\"attr1_referent\":{\n" +
            "\"restrictions\":[\n" +
            "{\n" +
            "\"issuer\":\"발행인DID\"\n" +
            "},\n" +
            "{\n" +
            "\"type\":\"LoginCredential\"\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "}\n" +
            "}\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "}";

    private String online2 = "{\n" +
            "\"iss\":\"고가품 거래 서버 DID\",\n" +
            "\"iat\":\"발행 시간\",\n" +
            "\"id\":\"efef5830-b372-4a42-93e6-49c212e59b78\",\n" +
            "\"presentationURL\":\"URL\",\n" +
            "\"presentationRequest\":{\n" +
            "\"criteria\":[\n" +
            "{\n" +
            "\"nonZKP\":{\n" +
            "\"nonce\":\"123432421212\",\n" +
            "\"name\":\"Market-Payment-Request\",\n" +
            "\"version\":\"0.1\",\n" +
            "\"delegated_attributes\":{\n" +
            "\"delegated_attr1_referent\":{\n" +
            "\"type\":\"delegated_VC\",\n" +
            "\"delegated_attr\":\"attr1_referent\",\n" +
            "\"did_delegator\":\"did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001b00\",\n" +
            "\"payment\":\"50000\"\n" +
            "}\n" +
            "},\n" +
            "\"requested_attributes\":{\n" +
            "\"attr1_referent\":{\n" +
            "\"restrictions\":[\n" +
            "{\n" +
            "\"issuer\":\"발행인DID\"\n" +
            "},\n" +
            "{\n" +
            "\"type\":\"CardTokenCredential\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "\"attr2_referent\":{\n" +
            "\"restrictions\":[\n" +
            "{\n" +
            "\"issuer\":\"발행인DID\"\n" +
            "},\n" +
            "{\n" +
            "\"type\":\"PhoneCredential\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "\"attr3_referent\":{\n" +
            "\"restrictions\":[\n" +
            "{\n" +
            "\"issuer\":\"발행인DID\"\n" +
            "},\n" +
            "{\n" +
            "\"type\":\"AddressCredential\"\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "}\n" +
            "}\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "}";

    @Language("JSON")
    private String onlinezkp = "{\n" +
            "  " +
            "\"iss\":\"고가품 거래 서버 DID\",\n" +
            "  " +
            "\"iat\":\"발행 시간\",\n" +
            "  " +
            "\"id\":\"efef5830-b372-4a42-93e6-49c212e59b78\",\n" +
            "  " +
            "\"presentationURL\":\"URL\",\n" +
            "  " +
            "\"presentationRequest\":{\n" +
            "    " +
            "\"criteria\":[\n" +
            "      " +
            "{\n" +
            "        " +
            "\"ZKP\":{\n" +
            "          " +
            "\"nonce\":\"123432421212\",\n" +
            "          " +
            "\"name\":\"Used-Market-Payment-Request\",\n" +
            "          " +
            "\"version\":\"0.1\",\n" +
            "          " +
            "\"requested_attributes\":{\n" +
            "            " +
            "\"attr1_referent\":{\n" +
            "              " +
            "\"name\":\"phone_number\",\n" +
            "              " +
            "\"restrictions\":[\n" +
            "                " +
            "{\n" +
            "                  " +
            "\"issuer\":\"발행인DID\"\n" +
            "                " +
            "},\n" +
            "                " +
            "{\n" +
            "                  " +
            "\"type\":\"폰번호 VC Type\"\n" +
            "                " +
            "}\n" +
            "              " +
            "]\n" +
            "            " +
            "},\n" +
            "            " +
            "\"attr2_referent\":{\n" +
            "              " +
            "\"name\":\"address\",\n" +
            "              " +
            "\"restrictions\":[\n" +
            "                " +
            "{\n" +
            "                  " +
            "\"issuer\":\"발행인DID\"\n" +
            "                " +
            "},\n" +
            "                " +
            "{\n" +
            "                  " +
            "\"type\":\"주소 VC Type\"\n" +
            "                " +
            "}\n" +
            "              " +
            "]\n" +
            "            " +
            "}\n" +
            "          " +
            "}\n" +
            "        " +
            "}\n" +
            "      " +
            "}\n" +
            "    " +
            "]\n" +
            "  " +
            "}\n" +
            "}";

    private void noneZKPTest(String json){
        boolean delegated_attributes = false;
//        boolean requested_attributes = false;
        VpRequestDataVoList list;
        PrintLog.e("json = " + json);
        VpRequestVo sample = IITPApplication.gson.fromJson(json, VpRequestVo.class);
        PrintLog.e("iat = " + sample.getIat());
        PrintLog.e("id = " + sample.getId());
        PrintLog.e("iss = " + sample.getIss());
        PrintLog.e("url = " + sample.getPresentationURL());
        list = sample.getPresentationRequest();
        List<VpRequestDataVo> templist = list.getCriteria();
        Map<String, VpRequestZKPReferentDataVo> requestAttributes = null;
        Map<String, VpRequestZKPDelegateDataVo> delegateAttributes = null;
        for(VpRequestDataVo temp : templist){
            VpRequestNoneZKPDataVo aa = temp.getNonZKP();
            PrintLog.e("name = " + aa.getName());
            PrintLog.e("nonce = " + aa.getNonce());
            PrintLog.e("version = " + aa.getVersion());
            requestAttributes = aa.getRequested_attributes();
            delegateAttributes = aa.getDelegated_attributes();
            if(requestAttributes != null){
//                requested_attributes = true;
                for(String key : requestAttributes.keySet()){
                    VpRequestZKPReferentDataVo restrictions = requestAttributes.get(key);
                    List<Map<String, String>> tempData = restrictions.getRestrictions();
                    for(Map<String, String> data : tempData){
                        for(String key1 : data.keySet()){
                            PrintLog.e(key1 + " : " + data.get(key1));
                        }
                    }
                }
            }
            if(delegateAttributes != null){
                delegated_attributes = true;
                for(String key : delegateAttributes.keySet()){
                    VpRequestZKPDelegateDataVo delegate = delegateAttributes.get(key);
                    PrintLog.e("type = " + delegate.getType());
                    PrintLog.e("payment = " + delegate.getPayment());
                    PrintLog.e("did_delegator = " + delegate.getDid_delegator());
                    PrintLog.e("attr = " + delegate.getDelegated_attr());
                }
            }
        }

        if(delegated_attributes == true){
            if(requestAttributes != null && delegateAttributes != null){
                makeDelegatedVC(getDID(), delegateAttributes);
            }
        }
        List<String> vcList = getVcList(delegated_attributes, requestAttributes);
        for(String vc : vcList){
            PrintLog.e("vc = " + vc);

        }
        makeVpData(getDID(), vcList);

    }

    private List<String> getVcList(Boolean delegator, Map<String, VpRequestZKPReferentDataVo> requestAttributes){
        VCVPCreater creater = VCVPCreater.getInstance();
        List<String> vclist = new ArrayList<String>();
        if(delegator){
            String vc = CommonPreference.getInstance(SampleTestActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreDelegationToken, null);
            if(vc != null){
                vclist.add(vc);
            }
        }
        for(String key : requestAttributes.keySet()){
            VpRequestZKPReferentDataVo restrictions = requestAttributes.get(key);
            List<Map<String, String>> tempData = restrictions.getRestrictions();
            for(Map<String, String> data : tempData){
                for(String key1 : data.keySet()){
                    PrintLog.e(key1 + " : " + data.get(key1));
                    if(key1.equals("type")){
                        String preKey = creater.getPreferenceKey(data.get(key1));
                        PrintLog.e("preKey = " + preKey);
                        String vc = CommonPreference.getInstance(SampleTestActivity.this).getSecureSharedPreferences().getString(preKey, null);
                        vclist.add(vc);
                    }
                }
            }
        }

        return vclist;
    }

    private void makeVpData(String did, List<String> vcList){
        KeyManager keyManager = new KeyManager(did);
        String keyid = keyManager.getManagementKeyId(SampleTestActivity.this);
        ECPrivateKey privatKey = ECKeyUtils.toECPrivateKey(keyManager.getPrivate(SampleTestActivity.this), "secp256k1"); // PrivateKey load

        VCVPCreater creator = VCVPCreater.getInstance();
        String vp = creator.vpCreate(did, keyid, privatKey, vcList);
        PrintLog.e("vpData : " + vp);
    }

    //위임 VC 생성 및 저장
    private void makeDelegatedVC(String did, Map<String, VpRequestZKPDelegateDataVo> delegateAttributes){
        String type = null;
        String payment = null;
        String did_delegator = null;
        String delegator_attr = null;

        if(delegateAttributes != null){
            for(String key : delegateAttributes.keySet()){
                VpRequestZKPDelegateDataVo delegate = delegateAttributes.get(key);
                type = delegate.getType();
                PrintLog.e("type = " + type);
                payment = delegate.getPayment();
                PrintLog.e("payment = " + payment);
                did_delegator = delegate.getDid_delegator();
                PrintLog.e("did_delegator = " + did_delegator);
                delegator_attr = delegate.getDelegated_attr();
                PrintLog.e("delegator_attr = " + delegator_attr);
            }
        }

        VCVPCreater creator = VCVPCreater.getInstance();
        String deleted_vc = creator.vcCreateDelegationToken(SampleTestActivity.this, did, payment, did_delegator, delegator_attr);
        savePreVCList(NoneZKPStoreDelegationToken, deleted_vc);
    }

    private void savePreVCList(String key, String jwt){
        PrintLog.e("VC = " + jwt);
        PrintLog.e("save Key = " + key);
        CommonPreference.getInstance(SampleTestActivity.this).getSecureSharedPreferences().edit().putString(key, jwt).apply();
    }

    private void ZKPTest(String json){
        VpRequestDataVoList list;
        PrintLog.e("json = " + json);
        VpRequestVo sample = IITPApplication.gson.fromJson(json, VpRequestVo.class);
        PrintLog.e("iat = " + sample.getIat());
        PrintLog.e("id = " + sample.getId());
        PrintLog.e("iss = " + sample.getIss());
        PrintLog.e("url = " + sample.getPresentationURL());
        list = sample.getPresentationRequest();
        List<VpRequestDataVo> templist = list.getCriteria();
        for(VpRequestDataVo temp : templist){

            VpRequestZKPDataVo zkpData = temp.getZKP();
            if(zkpData != null){
                PrintLog.e("name = " + zkpData.getName());
                PrintLog.e("nonce = " + zkpData.getNonce());
                PrintLog.e("version = " + zkpData.getVersion());
                Map<String, VpRequestZKPReferentDataVo> requestAttributes = zkpData.getRequested_attributes();
                if(requestAttributes != null){
                    for(String key : requestAttributes.keySet()){
                        VpRequestZKPReferentDataVo restrictions = requestAttributes.get(key);
                        List<Map<String, String>> tempData = restrictions.getRestrictions();
                        for(Map<String, String> data : tempData){
                            for(String key1 : data.keySet()){
                                PrintLog.e(key1 + " : " + data.get(key1));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        PrintLog.e("onDestroy");
    }

    private String getDID(){
        String did = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(SampleTestActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
        if(json != null){
            didList = IITPApplication.gson.fromJson(json, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
        }
        for(DidDataVo data : didList){
            if(data.getBlackChain().equals(BlockChainType.METADIUM)){
                did = data.getDid();
            }
        }
        return did;
    }



}

