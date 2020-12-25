package com.iitp.iitp_demo.activity.request;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.iitp.iitp_demo.activity.model.IndyCredentialVo;
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
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.databinding.ActivityVpCompanyRequestBinding;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.OnSingleClickListener;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.util.ECKeyUtils;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import java.io.IOException;
import java.security.interfaces.ECPrivateKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreDelegationToken;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreIdCard;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreMobile;
import static com.iitp.iitp_demo.chain.VCVPCreater.OfficeCredential;

public class RequestWebVpCompanyActivity extends BaseActivity{
    private List<RequestCredentialData> listData = new ArrayList<>();
    private ActivityVpCompanyRequestBinding layout;
    List<String> vcList = new ArrayList<>();
    private CredentialListAdapter adapter = new CredentialListAdapter();
    private String presentationRequestId = null;
    private PreferenceUtil preferenceUtil;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;
    private boolean checkBio;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String requestVP = null;
        preferenceUtil = PreferenceUtil.getInstance(this);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_vp_company_request);
        setSupportActionBar(layout.toolbar.appbar);
        setActionBarSet(layout.toolbar, "", false);
        layout.linearLayout5.setVisibility(View.GONE);
        requestVP = intent.getStringExtra("requestVP");
        PrintLog.e("requestVP = " + requestVP);
        ArrayList<String> vcList = IITPApplication.gson.fromJson(requestVP, new TypeToken<ArrayList<String>>(){
        }.getType());

        layout.refuseBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setResult(RESULT_CANCELED);
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
                        ToastUtils.custom(Toast.makeText(RequestWebVpCompanyActivity.this, "VC가 없습니다. ", Toast.LENGTH_SHORT)).show();
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
        if((BiometricUtils.getPincode(RequestWebVpCompanyActivity.this) == null && !(BiometricUtils.isFingerPrint(RequestWebVpCompanyActivity.this)))){
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
            if(BiometricUtils.isFingerPrint(RequestWebVpCompanyActivity.this)){
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
        String deleted_vc = creator.vcCreateDelegationToken(RequestWebVpCompanyActivity.this, did, payment, did_delegator, delegator_attr);
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
        CommonPreference.getInstance(RequestWebVpCompanyActivity.this).getSecureSharedPreferences().edit().putString(key, jwt).apply();
    }

    /**
     * get meta did
     *
     * @return did
     */
    private String getDID(){
        String did = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(RequestWebVpCompanyActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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
        String keyid = keyManager.getManagementKeyId(RequestWebVpCompanyActivity.this);
        ECPrivateKey privatKey = ECKeyUtils.toECPrivateKey(keyManager.getPrivate(RequestWebVpCompanyActivity.this), "secp256k1"); // PrivateKey load

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
            String vc = CommonPreference.getInstance(RequestWebVpCompanyActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreDelegationToken, null);
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
                            String vc = CommonPreference.getInstance(RequestWebVpCompanyActivity.this).getSecureSharedPreferences().getString(preKey, null);
                            PrintLog.e("vc data = " + vc);
                            vclist.add(vc);
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
                setResult(RESULT_OK);
                finish();
            }
            if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        setResult(RESULT_OK);
                        finish();
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
        Indy indy = Indy.getInstance(RequestWebVpCompanyActivity.this);
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        int size = vclist.size();
        PrintLog.e("size = " + size);
        if(size != 1){
            for(String temp : vclist){
                if(temp.equals("IdentificationCredential")){
                    String idCredential = CommonPreference.getInstance(RequestWebVpCompanyActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreIdCard, null);
                    String phoneCredential = CommonPreference.getInstance(RequestWebVpCompanyActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreMobile, null);
                    String payload = preferenceUtil.getPayload(idCredential);
                    JWTClaimsSet jwtClaimsSet = null;
                    try{
                        jwtClaimsSet = JWTClaimsSet.parse(payload);
                    }catch(ParseException e){
                      PrintLog.e("getVcDataList error");
                    }
                    VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
                    Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                    for(String key : claims.keySet()){
                        PrintLog.e(key + " : " + claims.get(key));
                        runOnUiThread(() -> {
                            layout.toolbar.appbarTitle.setText("VC 제출 요청");
                            layout.icon.setBackgroundResource(R.drawable.ic_list_item_govern);
                            layout.loginvp.setVisibility(View.VISIBLE);
                            if(key.equals("name")){
                                layout.loginName.setText(claims.get(key).toString());
                            }else if(key.equals("birth_date")){
                                layout.loginBirth.setText(claims.get(key).toString());
                            }else if(key.equals("address")){
                                layout.loginAddress.setText(claims.get(key).toString());
                            }
                        });
                    }
                    payload = preferenceUtil.getPayload(phoneCredential);
                    try{
                        jwtClaimsSet = JWTClaimsSet.parse(payload);
                    }catch(ParseException e){
                        PrintLog.e("getVcDataList error");
                    }
                    credential = verifierTemp.toCredential(jwtClaimsSet);

                    Map<String, Object> claims1 = (Map<String, Object>) credential.getCredentialSubject();
                    for(String key : claims1.keySet()){
                        PrintLog.e(key + " : " + claims1.get(key));
                        runOnUiThread(() -> {
                            layout.phonevp1.setVisibility(View.VISIBLE);
                            if(key.equals("name")){
                                layout.mobileName.setText(claims1.get(key).toString());
                            }else if(key.equals("phone_num")){
                                layout.cardMobile.setText(claims1.get(key).toString());
                            }
                        });
                    }
                    ArrayList<IndyCredentialVo> indyCredentialVos = new ArrayList<>();


                    String storeData = null;
                    try{
                        storeData = indy.getCredentialsWorksForEmptyFilter();
                        indyCredentialVos = IITPApplication.gson.fromJson(storeData, new TypeToken<ArrayList<IndyCredentialVo>>(){
                        }.getType());

//                        PrintLog.e("storeData = " + storeData);
                        if(storeData.length() > 2){
                            String number = null;
                            String graduationData = null;
                            String gpa = null;
                            String collageName = null;
                            String name = null;
                            String birthday = null;
                            String admission_date = null;
                            for(IndyCredentialVo temp1 : indyCredentialVos){
                                PrintLog.e("tag = " + temp1.getCred_def_id());
                                if(temp1.getCred_def_id().contains("university")){
                                    Map<String, String> tempData = temp1.getAttr();
                                    for(String key : tempData.keySet()){
                                        PrintLog.e(key + " : " + tempData.get(key));
                                        if(key.equals("gpa")){
                                            gpa = tempData.get(key);
                                        }else if(key.equals("name")){
                                            name = tempData.get(key);
                                        }else if(key.equals("birth_date")){
                                            birthday = tempData.get(key);
                                        }else if(key.equals("collage_name")){
                                            collageName = tempData.get(key);
                                        }else if(key.equals("number")){
                                            number = tempData.get(key);
                                        }else if(key.equals("graduation_date")){
                                            graduationData = tempData.get(key);
                                        }else if(key.equals("admission_date")){
                                            admission_date = tempData.get(key);
                                        }
                                    }
                                    String finalName = name;
                                    String finalCollageName = collageName;
                                    String finalGpa = gpa;
                                    String finalGraduationData = graduationData;
                                    String finalBirthday = birthday;
                                    String finalNumber = number;
                                    String admissionDate = admission_date;
                                    runOnUiThread(() -> {
                                        layout.univercityvp.setVisibility(View.VISIBLE);
                                        layout.uniName.setText(finalName);
                                        layout.unuSchoolName.setText(finalCollageName);
                                        layout.uniGpa.setText(finalGpa);
                                        layout.uniGraduationDate.setText(finalGraduationData);
                                        layout.uniBirth.setText(finalBirthday);
                                        layout.uniNum.setText(finalNumber);
                                        layout.uniIniDate.setText(admissionDate);
                                    });

                                }
                            }

                        }else{
                            runOnUiThread(() -> {
                                ToastUtils.custom(Toast.makeText(RequestWebVpCompanyActivity.this, "졸업증명서가 없습니다.", Toast.LENGTH_SHORT)).show();
//                                layout.sendBtn.setEnabled(false);
                                layout.sendBtn.setClickable(false);
                                layout.sendBtn.setEnabled(false);
                            });

                        }

                    }catch(Exception e){
                        PrintLog.e("getVcDataList error");
                    }
                }
            }
        }else{
            String vc = vclist.get(0);
            if(vc.contains("{")){
                ArrayList<IndyCredentialVo> indyCredentialVos = new ArrayList<>();
                String storeData = null;
                try{
                    storeData = indy.getCredentialsWorksForEmptyFilter();
                    indyCredentialVos = IITPApplication.gson.fromJson(storeData, new TypeToken<ArrayList<IndyCredentialVo>>(){
                    }.getType());

//                PrintLog.e("storeData = " + storeData);
                    if(storeData.length() > 2){
                        String issuancedate = null;
                        String cropid = null;
                        String groupName = null;
                        String name = null;
                        int listSize = indyCredentialVos.size();
                        for(IndyCredentialVo temp : indyCredentialVos){
                            PrintLog.e("tag = " + temp.getCred_def_id());
                            if(temp.getCred_def_id().equals("ES52TT3zeLLvukKTjX8fMC:3:CL:446:company_TAG_CD")){
                                Map<String, String> tempData = temp.getAttr();
                                for(String key : tempData.keySet()){
                                    PrintLog.e(key + " : " + tempData.get(key));
                                    if(key.equals("issuancedate")){
                                        issuancedate = tempData.get(key);
                                    }else if(key.equals("corp-id")){
                                        cropid = tempData.get(key);
                                    }else if(key.equals("group_name")){
                                        groupName = tempData.get(key);
                                    }else if(key.equals("name")){
                                        name = tempData.get(key);
                                    }
                                }
                                String finalName = name;
                                String finalGroupName = groupName;
                                String finalCropid = cropid;
                                String finalIssuancedate = issuancedate;
                                runOnUiThread(() -> {
                                    layout.companyvp.setVisibility(View.VISIBLE);
                                    layout.companyName.setText(finalName);
                                    layout.companyNum.setText(finalCropid);
                                    layout.companyGroup.setText(finalGroupName);
                                    layout.companyDate.setText(finalIssuancedate);
                                });

                            }
                        }

                    }else{
                        runOnUiThread(() -> ToastUtils.custom(Toast.makeText(RequestWebVpCompanyActivity.this, "사원증이 없습니다.", Toast.LENGTH_SHORT)).show());
                    }
                }catch(Exception e){
                    PrintLog.e("getVcDataList error");
                }
            }else{
                String officeCredential = CommonPreference.getInstance(RequestWebVpCompanyActivity.this).getSecureSharedPreferences().getString(OfficeCredential, null);
                String payload = preferenceUtil.getPayload(officeCredential);
                JWTClaimsSet jwtClaimsSet = null;
                try{
                    jwtClaimsSet = JWTClaimsSet.parse(payload);
                }catch(ParseException e){
                    PrintLog.e("getVcDataList error");
                }
                VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
                Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                String issuer = credential.getIssuer().toString();
                PrintLog.e("issuer = " + issuer);
                String issuancedate = null;
                String cropid = null;
                String groupName = null;
                String name = null;
                for(String key : claims.keySet()){
                    PrintLog.e(key + " : " + claims.get(key));
                    if(key.equals("issuancedate")){
                        issuancedate = claims.get(key).toString();
                    }else if(key.equals("corp-id")){
                        cropid = claims.get(key).toString();
                    }else if(key.equals("group_name")){
                        groupName = claims.get(key).toString();
                    }else if(key.equals("name")){
                        name = claims.get(key).toString();
                    }
                }
                String finalGroupName1 = groupName;
                String finalName1 = name;
                String finalCropid1 = cropid;
                String finalIssuancedate1 = issuancedate;
                runOnUiThread(() -> {
                    final float scale = getResources().getDisplayMetrics().density;
                    int dpHeightInPx = 0;
                    int dpWidthInPx = 0;
                    int resource = -1;
                    if(issuer.contains("meta")){
                        dpHeightInPx = (int) (30 * scale);
                        dpWidthInPx = (int) (100 * scale);
                        resource = R.drawable.img_metadium;
                    }else if(issuer.contains("icon")){
                        dpHeightInPx = (int) (20 * scale);
                        dpWidthInPx = (int) (120 * scale);
                        resource = R.drawable.img_icon;
                    }
                    ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) layout.companychain.getLayoutParams();
                    lp.height = dpHeightInPx;
                    lp.width = dpWidthInPx;
                    layout.companychain.setLayoutParams(lp);
                    layout.companychain.setBackgroundResource(resource);
                    layout.image1.setVisibility(View.GONE);
                    layout.image2.setVisibility(View.GONE);
                    layout.image3.setVisibility(View.GONE);
                    layout.image4.setVisibility(View.GONE);
                    layout.toolbar.appbarTitle.setText("VC 제출 요청");
                    layout.icon.setBackgroundResource(R.drawable.ic_list_item_govern);
                    layout.companyvp.setVisibility(View.VISIBLE);
                    layout.companyDate.setText(finalIssuancedate1);
                    layout.companyNum.setText(finalCropid1);
                    layout.companyGroup.setText(finalGroupName1);
                    layout.companyName.setText(finalName1);
                });
            }
        }
        PrintLog.e("--------------------------");
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
        intent = new Intent(RequestWebVpCompanyActivity.this, PincodeActivity.class);
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
                ToastUtils.custom(Toast.makeText(RequestWebVpCompanyActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
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
                        setResult(RESULT_OK);
                        finish();
                    }
                }, 100);
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

