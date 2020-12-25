package com.iitp.iitp_demo.activity.request;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;

import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.KeyManager;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.PincodeActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DataVo;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.activity.model.ProductVC;
import com.iitp.iitp_demo.activity.model.VpRequestDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestDataVoList;
import com.iitp.iitp_demo.activity.model.VpRequestNoneZKPDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPDelegateDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPReferentDataVo;
import com.iitp.iitp_demo.activity.model.VpResponseVo;
import com.iitp.iitp_demo.api.PreVCAPI;
import com.iitp.iitp_demo.api.model.delegaterVCVo;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.chain.VCVPVerifier;
import com.iitp.iitp_demo.databinding.ActivityVpPostRequestBinding;
import com.iitp.iitp_demo.util.BiometricDialog;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.OnSingleClickListener;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.iitp_demo.util.ViewUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.util.ECKeyUtils;

import java.io.IOException;
import java.security.interfaces.ECPrivateKey;
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
import static com.iitp.iitp_demo.chain.VCVPCreater.DELEGATOR_VC;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreDelegationToken;

public class RequestPostVpActivity extends BaseActivity{
    private ActivityVpPostRequestBinding layout;
    private List<String> vcList;
    private String presentationRequestId = null;
    private String responseUrl = null;
    private PreferenceUtil preferenceUtil;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;
    private boolean checkBio = false;

    private String name1 = null;
    private String birth1 = null;
    private String address1 = null;
    private String name2 = null;
    private String birth2 = null;
    private String address2 = null;
    private String name3 = null;
    private String credentialType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String requestVP = null;
        requestVP = intent.getStringExtra("jwt");
        layout = DataBindingUtil.setContentView(this, R.layout.activity_vp_post_request);
        setSupportActionBar(layout.toolbar.appbar);
        PrintLog.e("request vp = " + requestVP);
        setActionBarSet(layout.toolbar, getString(R.string.vc_request_post), false);
        preferenceUtil = PreferenceUtil.getInstance(this);
        DataVo data = IITPApplication.gson.fromJson(requestVP, DataVo.class);
        String jwt = data.getJwt();
        String json = getRequestVPJson(jwt);
        vcList = parseRequestVp(json);
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
                if(getDelegatorVC()){
                    getVcDataList(vcList);
                    runOnUiThread(() -> {
                        layout.deligator.setText(name1);
                        layout.birth.setText(birth1);
                        layout.address.setText(address1);
                        layout.deligator1.setText(name2);
                        layout.birth1.setText(name3);
                        layout.vcType.setText(credentialType);
                        layout.deligator3.setText(name2);
                        layout.address3.setText(address2);
                        layout.birth3.setText(birth2);

                    });
//                    if(listData.size() != 0){
////                        runOnUiThread(() -> adapter.notifyDataSetChanged());
//                    }else{
//                        runOnUiThread(() -> {
//                            ToastUtils.custom(Toast.makeText(RequestPostVpActivity.this, "VC가 없습니다. ", Toast.LENGTH_SHORT)).show();
//                            setResult(RESULT_CANCELED);
//                            finish();
//                        });
//
//                    }
                }else{
                    runOnUiThread(() -> showEmpty("위임장이 없습니다."));
                }
            }catch(Exception e){
                // error
                e.printStackTrace();
            }
        }).start();

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
     * pincode 입력
     */
    private void pinCode(){
        checkBio = false;
        BiometricUtils.hasBiometricEnrolled(this);
        if((BiometricUtils.getPincode(this) == null && !(BiometricUtils.isFingerPrint(this)))){
            if(BiometricUtils.checkFingerprint(this)){
                fingerBioFactory(this);
            }else{
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startPincodeActivity();
                    }
                }, 1000);
            }
        }else{
            if(BiometricUtils.isFingerPrint(this)){
                fingerBioFactory(this);
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
        responseUrl = request.getPresentationURL();
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
        String deleted_vc = creator.vcCreateDelegationToken(RequestPostVpActivity.this, did, payment, did_delegator, delegator_attr);
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
        CommonPreference.getInstance(RequestPostVpActivity.this).getSecureSharedPreferences().edit().putString(key, jwt).apply();
    }

    /**
     * get meta did
     *
     * @return did
     */
    private String getDID(){
        String did = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(RequestPostVpActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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
        String keyid = keyManager.getManagementKeyId(RequestPostVpActivity.this);
        ECPrivateKey privatKey = ECKeyUtils.toECPrivateKey(keyManager.getPrivate(RequestPostVpActivity.this), "secp256k1"); // PrivateKey load

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
            String vc = CommonPreference.getInstance(RequestPostVpActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreDelegationToken, null);
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
                        if(preKey != null){
                            if(preKey.equals("ProductCredential") || preKey.equals("ProductProofCredential")){
                                ProductVC productVC = preferenceUtil.getProductVC().get(0);
                                if(preKey.equals("ProductCredential")){
                                    vclist.add(productVC.getProductVC());
                                }else if(preKey.equals("ProductProofCredential")){
                                    vclist.add(productVC.getProductProofVC());
                                }
                            }else{
                                String vc = CommonPreference.getInstance(RequestPostVpActivity.this).getSecureSharedPreferences().getString(preKey, null);
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
            if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                String vp = makeVpData(getDID(), vcList);
                String response = makeRequestVpResponse(vp);

                requestCredential(responseUrl, response);
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
            List<String> types = new ArrayList<String>(temp.getTypes());
            PrintLog.e("type = " + types.get(1));
            if(types.get(1).equals("IdentificationCredential")){
                Map<String, Object> claims = (Map<String, Object>) temp.getCredentialSubject();
                String id = claims.get("id").toString();
                PrintLog.e("id = " + id);
                if(id.equals(getDID())){
                    for(String key : claims.keySet()){
                        PrintLog.e(key + " : " + claims.get(key));
                        if(key.equals("name")){
                            name2 = claims.get(key).toString();
                        }else if(key.equals("address")){
                            address2 = claims.get(key).toString();
                        }else if(key.equals("birth_date")){
                            birth2 = claims.get(key).toString();
                        }
                    }
                }else{
                    for(String key : claims.keySet()){
                        PrintLog.e(key + " : " + claims.get(key));
                        if(key.equals("name")){
                            name1 = claims.get(key).toString();
                        }else if(key.equals("address")){
                            address1 = claims.get(key).toString();
                        }else if(key.equals("birth_date")){
                            birth1 = claims.get(key).toString();
                        }
                    }
                }
            }else{
                Map<String, Object> claims = (Map<String, Object>) temp.getCredentialSubject();
                for(String key : claims.keySet()){
                    PrintLog.e(key + " : " + claims.get(key));
                    if(key.equals("verifier")){
                        name3 = claims.get(key).toString();
                    }else if(key.equals("type")){
                        credentialType = claims.get(key).toString();
                    }
                }
            }

            PrintLog.e("--------------------------");
        }
    }

    private boolean getDelegatorVC(){
        boolean result = false;
        String vc = CommonPreference.getInstance(this).getSecureSharedPreferences().getString(DELEGATOR_VC, null);
        PrintLog.e("vc = " + vc);
        if(vc != null){
            delegaterVCVo vcVo = IITPApplication.gson.fromJson(vc, delegaterVCVo.class);
            vcList.add(vcVo.getHolderVc());
            vcList.add(vcVo.getPoaVc());
            result = true;
        }
        return result;
    }

    private void startPinCodeActivity(){
        Intent intent;
        intent = new Intent(RequestPostVpActivity.this, PincodeActivity.class);
        intent.putExtra(PIN_SETTING_TYPE, 2);
        startActivityForResult(intent, 1000);
    }

    /**
     * finish dialog
     *
     * @param text
     */
    private void showEmpty(String text){
        LayoutInflater inflater = (LayoutInflater) RequestPostVpActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog customDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(this, 300);
        float height = ViewUtils.dp2px(this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btPositive = dialogView.findViewById(R.id.cancel1);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        twoBtn.setVisibility(View.GONE);
        oneBtn.setVisibility(View.VISIBLE);
        btPositive.setText(R.string.ok);
        textview.setText(text);
        btPositive.setOnClickListener(v -> {
            customDialog.dismiss();
            finish();
        });

        customDialog.show();
    }

    private void requestCredential(String url, String vp){
        PrintLog.e("response Url = " + responseUrl);
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
                ToastUtils.custom(Toast.makeText(RequestPostVpActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    private String getRequestVPJson(String jwt){
        String payload = preferenceUtil.getPayload(jwt);
        return payload;
    }

    private void fingerBioFactory(Context ctx){
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
                        requestCredential(responseUrl, response);
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

    private void fingerPrintFactory(){
        BiometricDialog biometricDialog = new BiometricDialog(RequestPostVpActivity.this,
                getString(R.string.did_fingerprint_title),
                getString(R.string.did_fingerprint_desc),
                new BiometricDialog.OnAuthenticationListener(){
                    @Override
                    public void onSuccess(FingerprintManager.AuthenticationResult result){
                        BiometricUtils.enableFingerPrint(RequestPostVpActivity.this);
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                String vp = makeVpData(getDID(), vcList);
                                String response = makeRequestVpResponse(vp);
                                requestCredential(responseUrl, response);
                            }
                        }, 500);
                    }

                    @Override
                    public void onError(int errorCode, CharSequence errString, AlertDialog errorDialog){
                        PrintLog.e("error " + errString.toString());
                        if(errorDialog != null){
                            errorDialog.setOnDismissListener(d -> {

                            });
                        }
                    }

                    @Override
                    public void onCancel(){
                        PrintLog.e("cancel");
                    }
                }
                , 0);

        biometricDialog.setCancelable(false);
        biometricDialog.setPositiveButton(new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                PrintLog.e("positive");
                dialog.dismiss();
                PrintLog.e("11111");
                finish();
            }
        });
        biometricDialog.setNegativeButton(new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                PrintLog.e("neagative");
                dialog.dismiss();
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startPinCodeActivity();
                    }
                }, 1000);
            }
        });
        biometricDialog.show();
    }
}

