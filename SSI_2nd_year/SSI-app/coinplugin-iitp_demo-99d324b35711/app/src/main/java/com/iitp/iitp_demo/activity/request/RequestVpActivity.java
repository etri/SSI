package com.iitp.iitp_demo.activity.request;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.KeyManager;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.PincodeActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.activity.model.ProductVC;
import com.iitp.iitp_demo.activity.model.RequestCredentialData;
import com.iitp.iitp_demo.activity.model.VpRequestDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestDataVoList;
import com.iitp.iitp_demo.activity.model.VpRequestNoneZKPDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPDelegateDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPReferentDataVo;
import com.iitp.iitp_demo.activity.model.VpResponseVo;
import com.iitp.iitp_demo.api.PreVCAPI;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.chain.VCVPVerifier;
import com.iitp.iitp_demo.databinding.ActivityVpRequestBinding;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.OnSingleClickListener;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.util.ECKeyUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreCardTokenKorea;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreCardTokenSeoul;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreDelegationToken;

public class RequestVpActivity extends BaseActivity{
    private List<RequestCredentialData> listData = new ArrayList<>();
    private ActivityVpRequestBinding layout;
    List<String> vcList;
    private CredentialListAdapter adapter = new CredentialListAdapter();
    private String presentationRequestId = null;
    private PreferenceUtil preferenceUtil;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;
    private boolean checkBio;
    private String url = null;
    private String iss = null;
    private String cardIssuer = null;
    private String credentialType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String requestVP = null;
        preferenceUtil = PreferenceUtil.getInstance(this);
        if(intent != null){
            Uri uri = getIntent().getData();
            if(uri != null){
                String uriDecode = null;
                try{
                    uriDecode = URLDecoder.decode(uri.toString(), "UTF-8");
                }catch(UnsupportedEncodingException e){
                    PrintLog.e("requestVpActivity error");
                }

                Log.e("TEST", "uri : " + uriDecode);
                Set<String> queryList = uri.getQueryParameterNames();

                if(queryList.size() > 2){
                    //todo
                }else{
                    requestVP = uri.getQueryParameter("requestVP");
                    PrintLog.e("requestVP = " + requestVP);
                }

            }
        }
        layout = DataBindingUtil.setContentView(this, R.layout.activity_vp_request);
        setSupportActionBar(layout.toolbar.appbar);
//        setActionBarSet(layout.toolbar, getString(R.string.vc_request), false);
        setActionBarSet(layout.toolbar, "", false);
        if(requestVP != null){
            if(!requestVP.contains("{")){
                String json = getRequestVPJson(requestVP);
                vcList = parseRequestVp(json);
            }else{
                vcList = parseRequestVp(requestVP);
            }
        }
        layout.refuseBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                PrintLog.e("2222");
                finish();
            }
        });
        layout.sendBtn.setOnClickListener(new OnSingleClickListener(){
            @Override
            public void onSingleClick(View v){
                pinCode();
            }
        });

        new Thread(() -> {
            try{
                if(vcList.size() == 0){
                    runOnUiThread(() -> {
                        ToastUtils.custom(Toast.makeText(RequestVpActivity.this, "VC가 없습니다. ", Toast.LENGTH_SHORT)).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    });
                }else{
                    getVcDataList(vcList);
                }
            }catch(Exception e){
                // error
                e.printStackTrace();
            }
        }).start();


    }

    /**
     * pincode 입력
     */
    private void pinCode(){
        checkBio = false;
        BiometricUtils.hasBiometricEnrolled(this);
        if((BiometricUtils.getPincode(RequestVpActivity.this) == null && !(BiometricUtils.isFingerPrint(RequestVpActivity.this)))){
            if(BiometricUtils.checkFingerprint(this)){
                fingerBiofactory(this);
            }else{
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startPincodeActivity();
                    }
                }, 1000);
            }
        }else{
            if(BiometricUtils.isFingerPrint(RequestVpActivity.this)){
                fingerBiofactory(this);
            }else{
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startPincodeActivity();
                    }
                }, 1500);
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        checkBio = false;
    }

    private void startPincodeActivity(){
        Intent intent;
        intent = new Intent(this, PincodeActivity.class);
        intent.putExtra(PIN_SETTING_TYPE, 2);
        startActivityForResult(intent, REQUEST_CODE_VERIFY_PIN_APP_LOCK);
        //finish();
    }

    /**
     * requestvp parsing
     *
     * @param json
     * @return
     */
    private List<String> parseRequestVp(String json){
        boolean delegated_attributes = false;
        VpRequestDataVoList list;
        PrintLog.e("json = " + json);
        VpRequestVo request = IITPApplication.gson.fromJson(json, VpRequestVo.class);
        PrintLog.e("iat = " + request.getIat());
        presentationRequestId = request.getId();
        PrintLog.e("presentationRequestId = " + request.getId());
        PrintLog.e("iss = " + request.getIss());
        PrintLog.e("url = " + request.getPresentationURL());
        url = request.getPresentationURL();
        list = request.getPresentationRequest();
        List<VpRequestDataVo> templist = list.getCriteria();
        Map<String, VpRequestZKPReferentDataVo> requestAttributes = null;
        Map<String, VpRequestZKPDelegateDataVo> delegateAttributes = null;
        for(VpRequestDataVo temp : templist){
            VpRequestNoneZKPDataVo dataVo = temp.getNonZKP();
            PrintLog.e("name = " + dataVo.getName());
            PrintLog.e("nonce = " + dataVo.getNonce());
            PrintLog.e("version = " + dataVo.getVersion());
            requestAttributes = dataVo.getRequested_attributes();
            delegateAttributes = dataVo.getDelegated_attributes();
            if(requestAttributes != null){
//                requested_attributes = true;
                for(String key : requestAttributes.keySet()){
                    VpRequestZKPReferentDataVo restrictions = requestAttributes.get(key);
                    List<Map<String, String>> tempData = restrictions.getRestrictions();
                    for(Map<String, String> data : tempData){
                        for(String key1 : data.keySet()){
                            PrintLog.e(key1 + " : " + data.get(key1));
                            if(key1.equals("type")){
                                if(data.get("type").equals("CardTokenCredential")){
//                                    credentialType = data.get("type");
                                    cardIssuer = tempData.get(0).get("issuer");
                                    PrintLog.e("card issuer = " + cardIssuer);
                                }
                            }
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

        if(delegated_attributes){
            if(requestAttributes != null && delegateAttributes != null){
                makeDelegatedVC(getDID(), delegateAttributes, requestAttributes);
            }
        }
        List<String> vcList = getVcList(delegated_attributes, requestAttributes);
        return vcList;
    }
    /**
     * request vp parsing
     * @param json requestvp
     * @return vp
     */
//    private String noneZKPMakeVP(String json){
//        String vp;
//        boolean delegated_attributes = false;
//        VpRequestDataVoList list;
//        PrintLog.e("json = " + json);
//        VpRequestVo request = IITPApplication.gson.fromJson(json, VpRequestVo.class);
//        PrintLog.e("iat = " + request.getIat());
//        presentationRequestId = request.getId();
//        PrintLog.e("presentationRequestId = " + request.getId());
//        PrintLog.e("iss = " + request.getIss());
//        PrintLog.e("url = " + request.getPresentationURL());
//        list = request.getPresentationRequest();
//        List<VpRequestDataVo> templist = list.getCriteria();
//        Map<String, VpRequestZKPReferentDataVo> requestAttributes = null;
//        Map<String, VpRequestZKPDelegateDataVo> delegateAttributes = null;
//        for(VpRequestDataVo temp : templist){
//            VpRequestNoneZKPDataVo dataVo = temp.getNonZKP();
//            PrintLog.e("name = " + dataVo.getName());
//            PrintLog.e("nonce = " + dataVo.getNonce());
//            PrintLog.e("version = " + dataVo.getVersion());
//            requestAttributes = dataVo.getRequested_attributes();
//            delegateAttributes = dataVo.getDelegated_attributes();
//            if(requestAttributes != null){
////                requested_attributes = true;
//                for(String key : requestAttributes.keySet()){
//                    VpRequestZKPReferentDataVo restrictions = requestAttributes.get(key);
//                    List<Map<String, String>> tempData = restrictions.getRestrictions();
//                    for(Map<String, String> data : tempData){
//                        for(String key1 : data.keySet()){
//                            PrintLog.e(key1 + " : " + data.get(key1));
//                        }
//                    }
//                }
//            }
//            if(delegateAttributes != null){
//                delegated_attributes = true;
//                for(String key : delegateAttributes.keySet()){
//                    VpRequestZKPDelegateDataVo delegate = delegateAttributes.get(key);
//                    PrintLog.e("type = " + delegate.getType());
//                    PrintLog.e("payment = " + delegate.getPayment());
//                    PrintLog.e("did_delegator = " + delegate.getDid_delegator());
//                    PrintLog.e("attr = " + delegate.getDelegated_attr());
//                }
//            }
//        }
//
//        if(delegated_attributes){
//            if(requestAttributes != null && delegateAttributes != null){
//                makeDelegatedVC(getDID(), delegateAttributes);
//            }
//        }
//        List<String> vcList = getVcList(delegated_attributes, requestAttributes);
//        for(String vc : vcList){
//            PrintLog.e("vc = " + vc);
//
//        }
//        vp = makeVpData(getDID(), vcList);
//        return vp;
//    }


    /**
     * 위임 VC 생성 및 저장
     *
     * @param did                did
     * @param delegateAttributes 위임VC 생성 여부
     */
    private void makeDelegatedVC(String did, Map<String, VpRequestZKPDelegateDataVo> delegateAttributes, Map<String, VpRequestZKPReferentDataVo> requestAttributes){
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
        if(requestAttributes != null){
            VpRequestZKPReferentDataVo restrictions = requestAttributes.get(delegator_attr);
            if(restrictions != null){
                List<Map<String, String>> tempData = restrictions.getRestrictions();
                for(Map<String, String> data : tempData){
                    String credentialType = data.get("type");
                    if(credentialType != null){
                        delegator_attr = credentialType;
                        PrintLog.e("delegator_attr = " + delegator_attr);
                    }
                }
            }


        }

        VCVPCreater creator = VCVPCreater.getInstance();
        String deleted_vc = creator.vcCreateDelegationToken(RequestVpActivity.this, did, payment, did_delegator, delegator_attr);
        savePreVCList(NoneZKPStoreDelegationToken, deleted_vc);
    }

    /**
     * 생성 위임 VC 저장
     *
     * @param key key
     * @param jwt vc
     */
    private void savePreVCList(String key, String jwt){
        PrintLog.e("VC = " + jwt);
        PrintLog.e("save Key = " + key);
        CommonPreference.getInstance(RequestVpActivity.this).getSecureSharedPreferences().edit().putString(key, jwt).apply();
    }

    /**
     * get meta did
     *
     * @return did
     */
    private String getDID(){
        String did = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(RequestVpActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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

    /**
     * vp Data 생성
     *
     * @param did    did
     * @param vcList vclist
     * @return vp
     */
    private String makeVpData(String did, List<String> vcList){
        KeyManager keyManager = new KeyManager(did);
        String keyid = keyManager.getManagementKeyId(RequestVpActivity.this);
        ECPrivateKey privatKey = ECKeyUtils.toECPrivateKey(keyManager.getPrivate(RequestVpActivity.this), "secp256k1"); // PrivateKey load

        VCVPCreater creator = VCVPCreater.getInstance();
        String vp = creator.vpCreate(did, keyid, privatKey, vcList);
        PrintLog.e("vpData : " + vp);
        return vp;
    }

    /**
     * requset vc list
     *
     * @param delegator         위임 vc 추가 여부
     * @param requestAttributes requsetvc 항목
     * @return vc list
     */
    private List<String> getVcList(Boolean delegator, Map<String, VpRequestZKPReferentDataVo> requestAttributes){
        VCVPCreater creater = VCVPCreater.getInstance();
        List<String> vclist = new ArrayList<String>();
        if(delegator){
            String vc = CommonPreference.getInstance(RequestVpActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreDelegationToken, null);
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
                        String preKey = null;
                        if(data.get(key1).equals("CardTokenCredential")){
                            if(cardIssuer.equals("did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001d4f")){
                                preKey = NoneZKPStoreCardTokenKorea;
                            }else{
                                preKey = NoneZKPStoreCardTokenSeoul;
                            }
                        }else{
                            preKey = creater.getPreferenceKey(data.get(key1));
                        }
                        PrintLog.e("preKey = " + preKey);
                        if(preKey != null){
                            if(preKey.equals("ProductCredential") || preKey.equals("ProductProofCredential")){
                                ArrayList<ProductVC> productVCSList = preferenceUtil.getProductVC();
                                int size = productVCSList.size();
                                if(size != 0){
                                    ProductVC productVC = preferenceUtil.getProductVC().get(size - 1);
                                    if(preKey.equals("ProductCredential")){
                                        vclist.add(productVC.getProductVC());
                                        vclist.add(productVC.getProductProofVC());
                                    }
                                }
                            }else{
                                String vc = CommonPreference.getInstance(RequestVpActivity.this).getSecureSharedPreferences().getString(preKey, null);
                                PrintLog.e("vc data = " + vc);
                                vclist.add(vc);
                            }
                        }
                    }
                }
            }
        }
        PrintLog.e("vclist size  = " + vclist.size());
        return vclist;
    }

    /**
     * uuid 생성
     *
     * @return uuid
     */
    private String makeUUID(){
        String uuid = UUID.randomUUID().toString();
        PrintLog.e("uuid = " + uuid);
        return uuid;
    }

    /**
     * vp response 생성
     *
     * @param vp vp jwt
     * @return response
     */
    private String makeRequestVpResponse(String vp){
        String uuid = makeUUID();
        VpResponseVo responseVp = new VpResponseVo(uuid, presentationRequestId, vp, null, null);
        String responseVpString = IITPApplication.gson.toJson(responseVp);
        PrintLog.e("response =" + responseVpString);
        return responseVpString;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 1000){
                PrintLog.e("onActivityResult1 : " + requestCode);
                // Go to create meta id
                String vp = makeVpData(getDID(), vcList);
                String response = makeRequestVpResponse(vp);
                Intent intent = new Intent();
                PrintLog.e("vp = " + response);
                intent.putExtra("vp", response);
                setResult(RESULT_OK, intent);
                finish();
//                requestCredential(response_url, response);
            }

            if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        String vp = makeVpData(getDID(), vcList);
                        String response = makeRequestVpResponse(vp);
                        Intent intent = new Intent();
                        PrintLog.e("vp = " + response);
                        intent.putExtra("vp", response);
                        setResult(RESULT_OK, intent);
                        finish();
//                        requestCredential(response_url, response);
                    }
                }, 200);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    private void getVcDataList(List<String> vclist){
        VCVPVerifier verifier = VCVPVerifier.getInstance();
        List<VerifiableCredential> credentialsList = verifier.verifyVCList(vclist);
        for(VerifiableCredential temp : credentialsList){
            List<String> types = new ArrayList<>(temp.getTypes());
            PrintLog.e("type = " + types.get(1));
            if(types.get(1).equals("ProductProofCredential")){
                iss = temp.getIssuer().toString();
                PrintLog.e("iss = " + iss);
            }
            Map<String, Object> claims = (Map<String, Object>) temp.getCredentialSubject();
            runOnUiThread(() -> {
                layout.toolbar.appbarTitle.setText("VC 제출 요청");
            });
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(credentialsList.size() == 4){
                    runOnUiThread(() -> {
                        layout.cardTokenvp1.setVisibility(View.VISIBLE);
                        layout.phonevp1.setVisibility(View.VISIBLE);
                        layout.addressvp1.setVisibility(View.VISIBLE);
                        layout.deligatorvp1.setVisibility(View.VISIBLE);
                    });
                }else if(credentialsList.size() == 2){
                    runOnUiThread(() -> {
                        layout.productVp.setVisibility(View.VISIBLE);
                        layout.productProofvp.setVisibility(View.VISIBLE);
                    });
                }
                if(types.get(1).equals("LoginCredential")){
                    runOnUiThread(() -> {
//                        layout.toolbar.appbarTitle.setText("VC 제출 요청");
                        layout.icon.setBackgroundResource(R.drawable.market);
                        layout.textView.setText("Men's Watch VC");
                        layout.loginvp.setVisibility(View.VISIBLE);
                        if(key.equals("name")){
                            layout.loginName.setText(claims.get(key).toString());
                        }else if(key.equals("birth_date")){
                            layout.loginBirth.setText(claims.get(key).toString());
                        }else if(key.equals("address")){
                            layout.loginAddress.setText(claims.get(key).toString());
                        }else if(key.equals("phone_num")){
                            layout.loginMobile.setText(claims.get(key).toString());
                        }
                    });
                }else if(types.get(1).equals("DelegatedVC")){
                    runOnUiThread(() -> {
                        if(key.equals("DID_delegator")){
                            layout.delDID.setText(claims.get(key).toString());
                        }else if(key.equals("payment")){
                            String price = claims.get(key).toString();
                            int priceInt = Integer.parseInt(price);
                            String formattedStringPrice = NumberFormat.getInstance(Locale.getDefault()).format(priceInt) + "원";
                            layout.delPrice.setText(formattedStringPrice);
                        }else if(key.equals("type")){
                            layout.delType.setText(claims.get(key).toString());
                        }
                    });
                }else if(types.get(1).equals("CardTokenCredential")){
                    runOnUiThread(() -> {
                        if(key.equals("token")){
                            if(cardIssuer.equals("did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001d4f")){
                                layout.cardTitle.setText(R.string.cardVC1);
                            }else{
                                layout.cardTitle.setText(R.string.cardVC2);
                            }
                            layout.cardTokenTv.setText(claims.get(key).toString());
                        }
                    });
                }else if(types.get(1).equals("PhoneCredential")){
                    runOnUiThread(() -> {
                        if(key.equals("name")){
                            layout.mobileName.setText(claims.get(key).toString());
                        }else if(key.equals("phone_num")){
                            layout.cardMobile.setText(claims.get(key).toString());
                        }
                    });
                }else if(types.get(1).equals("AddressCredential")){
                    runOnUiThread(() -> {
                        if(key.equals("address")){
                            layout.postAddress.setText(claims.get(key).toString());
                        }
                    });
                }else if(types.get(1).equals("ProductCredential")){
                    runOnUiThread(() -> {
                        if(key.equals("name")){
                            layout.productName.setText(claims.get(key).toString());
                        }else if(key.equals("production_date")){
                            layout.productDateTv.setText(claims.get(key).toString());
                        }else if(key.equals("SN")){
                            layout.snTv.setText(claims.get(key).toString());
                        }else if(key.equals("id")){
                            layout.productDIDTv.setText(claims.get(key).toString());
                        }
                    });
                }else if(types.get(1).equals("ProductProofCredential")){
                    runOnUiThread(() -> {
                        final float scale = getResources().getDisplayMetrics().density;
                        int dpHeightInPx = 0;
                        int dpWidthInPx = 0;
                        int resource = -1;
                        if(iss.contains("meta")){
                            dpHeightInPx = (int) (30 * scale);
                            dpWidthInPx = (int) (100 * scale);
                            resource = R.drawable.img_metadium;
                        }else if(iss.contains("indy")){
                            dpHeightInPx = (int) (28 * scale);
                            dpWidthInPx = (int) (150 * scale);
                            resource = R.drawable.img_indy;
                        }else if(iss.contains("icon")){
                            dpHeightInPx = (int) (20 * scale);
                            dpWidthInPx = (int) (120 * scale);
                            resource = R.drawable.img_icon;
                        }
                        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) layout.productChain.getLayoutParams();
                        lp.height = dpHeightInPx;
                        lp.width = dpWidthInPx;
                        layout.productChain.setLayoutParams(lp);
                        layout.productChain.setBackgroundResource(resource);
                        if(key.equals("sell_date")){
                            layout.sellDateTv.setText(claims.get(key).toString());
                        }else if(key.equals("price")){
                            String price = claims.get(key).toString();
                            int priceInt = Integer.parseInt(price);
                            String formattedStringPrice = NumberFormat.getInstance(Locale.getDefault()).format(priceInt) + "원";
                            layout.proofPriceDIDTv.setText(formattedStringPrice);
                        }else if(key.equals("ProductCredential_id")){
                            layout.proofDIDTv.setText(claims.get(key).toString());
                        }else if(key.equals("buyer_id")){
                            layout.buyerDIDTv.setText(claims.get(key).toString());
                        }else if(key.equals("seller_id")){
                            layout.sellerDIDTv.setText(claims.get(key).toString());
                        }else if(key.equals("BlockNumber")){
                            String blockNumber = claims.get(key).toString();
                            if(blockNumber.length() > 0){
                                layout.blocknumberTv.setText(claims.get(key).toString());
                            }else{
                                layout.linearLayout76.setVisibility(View.GONE);
                            }
                        }
                    });
                }else if(types.get(1).equals("StockServiceCredential")){
                    runOnUiThread(() -> {
//                        layout.toolbar.appbarTitle.setText("VC 제출 요청");
                        layout.stockvp.setVisibility(View.VISIBLE);
                        if(key.equals("name")){
                            layout.stockNameTv.setText(claims.get(key).toString());
                        }else if(key.equals("register_id")){
                            layout.stockIDTv.setText(claims.get(key).toString());
                        }else if(key.equals("start_date")){
                            layout.stockDateTv.setText(claims.get(key).toString());
                        }else if(key.equals("address")){
                            layout.stockAddressTv.setText(claims.get(key).toString());
                        }
                    });
                }
            }
            PrintLog.e("--------------------------");
        }
    }

    private boolean checkName(String key){
        boolean duplicate = false;
        for(RequestCredentialData tempData : listData){
            if(key.equals(tempData.getName())){
                duplicate = true;
                break;
            }
        }
        return duplicate;
    }

    private void startPinCodeActivity(){
        Intent intent;
        intent = new Intent(RequestVpActivity.this, PincodeActivity.class);
        intent.putExtra(PIN_SETTING_TYPE, 2);
        startActivityForResult(intent, 1000);
    }

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
            RequestCredentialData data = listData.get(i);
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_credential_3, parent, false);
                holder = new CredentialListAdapter.Holder();
                holder.icon = convertView.findViewById(R.id.icon);
                holder.desc1 = convertView.findViewById(R.id.desc1);
                holder.desc2 = convertView.findViewById(R.id.desc2);
                convertView.setTag(holder);
            }else{
                holder = (CredentialListAdapter.Holder) convertView.getTag();
            }
            holder.desc1.setText(data.getName());
            if(data.getData().length() > 60){
                holder.desc2.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            }
            holder.desc2.setText(data.getData());


            return convertView;
        }

        /**
         * holder
         */
        class Holder{
            ImageView icon;
            TextView desc1;
            TextView desc2;
        }
    }

    private void requestCredential(String url, String vp){

        PreVCAPI preVc = PreVCAPI.getInstance();
        if(url == null){
            Call<ResponseBody> rtn = preVc.preVCAPIInfo.requestVP(url, vp);
            rtn.enqueue(new Callback<ResponseBody>(){
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                    layout.progresslayout.setVisibility(View.GONE);
                    if(response.code() == 200){
                        if(response.body() != null){
                            try{
                                String responseJson = response.body().string();
                                PrintLog.e("response = " + responseJson);
                                Intent intent = new Intent();
                                PrintLog.e("vp = " + vp);
                                intent.putExtra("vp", vp);
                                setResult(RESULT_OK, intent);
                                finish();
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }else{
                        PrintLog.e("response = " + response.errorBody().toString());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t){
                    PrintLog.e("fail = " + t.getMessage());
                    ToastUtils.custom(Toast.makeText(RequestVpActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
                    layout.progresslayout.setVisibility(View.GONE);
                }
            });
        }else{
            Intent intent = new Intent();
            PrintLog.e("vp = " + vp);
            intent.putExtra("vp", vp);
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    private String getRequestVPJson(String jwt){
        String payload = preferenceUtil.getPayload(jwt);
        return payload;
    }

    private void fingerBiofactory(Context ctx){
        FingerBioFactory fingerBioFactory = new FingerBioFactory(this, new BiometricPrompt.AuthenticationCallback(){
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString){
                PrintLog.e("onAuthenticationError");
                PrintLog.e("Error = " + errorCode);
                if(errorCode == BiometricConstants.ERROR_HW_UNAVAILABLE || errorCode == BiometricConstants.ERROR_NO_BIOMETRICS
                        || errorCode == 13 || errorCode == 10 || errorCode == 7){
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            // 사용하고자 하는 코드
                            ToastUtils.custom(Toast.makeText(ctx, errString, Toast.LENGTH_SHORT)).show();
                        }
                    }, 0);
                    if(!checkBio){
                        startPincodeActivity();
                        checkBio = true;
                    }
                }
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result){
                PrintLog.e("onAuthenticationSucceeded");
                super.onAuthenticationSucceeded(result);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        String vp = makeVpData(getDID(), vcList);
                        String response = makeRequestVpResponse(vp);
                        requestCredential(url, response);
                    }
                }, 500);
            }


            @Override
            public void onAuthenticationFailed(){
                PrintLog.e("onAuthenticationFailed");
                super.onAuthenticationFailed();
            }
        });
        fingerBioFactory.setting(getString(R.string.did_fingerprint_title), getString(R.string.did_fingerprint_desc), getString(R.string.cancel));
        fingerBioFactory.authenticate();
    }
}

