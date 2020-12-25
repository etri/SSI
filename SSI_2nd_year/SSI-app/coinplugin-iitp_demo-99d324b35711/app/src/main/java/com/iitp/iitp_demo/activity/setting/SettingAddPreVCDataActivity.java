package com.iitp.iitp_demo.activity.setting;

import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.FinishListener;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.api.AgentAPI;
import com.iitp.iitp_demo.api.PreVCAPI;
import com.iitp.iitp_demo.api.model.CredentialRequestResponseVo;
import com.iitp.iitp_demo.api.model.CredentialRequestVo;
import com.iitp.iitp_demo.api.model.RequestGetOfferVo;
import com.iitp.iitp_demo.api.model.RequestVCVo;
import com.iitp.iitp_demo.api.model.ResponseVC;
import com.iitp.iitp_demo.api.model.ZkpResponse;
import com.iitp.iitp_demo.api.model.pushResponseVo;
import com.iitp.iitp_demo.chain.Icon;
import com.iitp.iitp_demo.chain.Metadium;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.chain.indy.ZkpClaimVo;
import com.iitp.iitp_demo.databinding.ActivitySettingAddPrevcBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.iitp_demo.util.ViewUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreCardTokenKorea;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreCardTokenSeoul;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreIdCard;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreLogin;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreMobile;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStorePost;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreStock;
import static com.iitp.iitp_demo.chain.VCVPCreater.ZKP_IdentificationCredential;

public class SettingAddPreVCDataActivity extends BaseActivity{


    private ActivitySettingAddPrevcBinding layout;
    private RequestVCVo requestVCVo;
    private RequestGetOfferVo nonZKPrequestVCVo;
    private String json;
    private String name;
    private String metaDid;
    private String indyDID;
    private boolean idCredential = false;
    private boolean mobileCredential = false;
    private boolean cardTokenCredential = false;
    private boolean postCredential = false;
    private boolean stockCredential = false;
    private boolean loginCredential = false;
    private Indy indy;
    private PreferenceUtil preferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_setting_add_prevc);
        setActionBarSet(layout.toolbar, "사전발행정보 입력", true);
        indy = Indy.getInstance(SettingAddPreVCDataActivity.this);
        preferenceUtil = PreferenceUtil.getInstance(this);
        initView();
    }

    @Override
    protected void onResume(){

        super.onResume();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    /**
     * init view
     */
    private void initView(){
        layout.publish.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(checkData()){
                    View focusView = getCurrentFocus();
                    if(focusView != null){
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                    }
                    preferenceUtil.resetData();
                    layout.publish.setEnabled(false);
                    nonzpkVcCreate();
                }
            }
        });

        layout.phoneNumber.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.phoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        layout.birth.addTextChangedListener(new TextWatcher(){
            private int _beforeLenght = 0;
            private int _afterLenght = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
                _beforeLenght = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                if(s.length() <= 0){
                    PrintLog.e("onTextChanged: Intput text is wrong (Type : Length)");
                    return;
                }

                char inputChar = s.charAt(s.length() - 1);
                if(inputChar != '-' && (inputChar < '0' || inputChar > '9')){
                    layout.birth.getText().delete(s.length() - 1, s.length());
                    PrintLog.e("onTextChanged: Intput text is wrong (Type : Number)");
                    return;
                }
                _afterLenght = s.length();
                // 삭제 중
                if(_beforeLenght > _afterLenght){
                    // 삭제 중에 마지막에 -는 자동으로 지우기
                    if(s.toString().endsWith("-")){
                        layout.birth.setText(s.toString().substring(0, s.length() - 1));
                    }
                }
                // 입력 중
                else if(_beforeLenght < _afterLenght){
//                    PrintLog.e("_afterLenght = " + _afterLenght);
                    if(_afterLenght == 7 && s.toString().indexOf("-") < 0){
                        layout.birth.setText(s.toString().subSequence(0, 6) + "-" + s.toString().substring(6, s.length()));
                    }
                }
                layout.birth.setSelection(layout.birth.length());
            }

            @Override
            public void afterTextChanged(Editable s){

            }
        });
        layout.stockStartDate.addTextChangedListener(new TextWatcher(){
            private int _beforeLenght = 0;
            private int _afterLenght = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
                _beforeLenght = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                if(s.length() <= 0){
                    PrintLog.e("onTextChanged: Intput text is wrong (Type : Length)");
                    return;
                }

                char inputChar = s.charAt(s.length() - 1);
                if(inputChar != '-' && (inputChar < '0' || inputChar > '9')){
                    layout.stockStartDate.getText().delete(s.length() - 1, s.length());
                    PrintLog.e("onTextChanged: Intput text is wrong (Type : Number)");
                    return;
                }

                _afterLenght = s.length();

                // 삭제 중
                if(_beforeLenght > _afterLenght){
                    // 삭제 중에 마지막에 -는 자동으로 지우기
                    if(s.toString().endsWith("-")){
                        layout.stockStartDate.setText(s.toString().substring(0, s.length() - 1));
                    }
                }
                // 입력 중
                else if(_beforeLenght < _afterLenght){
//                    PrintLog.e("_afterLenght = " + _afterLenght);
                    if(_afterLenght == 5 && s.toString().indexOf("-") < 0){
                        layout.stockStartDate.setText(s.toString().subSequence(0, 4) + "-" + s.toString().substring(4, s.length()));
                    }else if(_afterLenght == 8){
                        layout.stockStartDate.setText(s.toString().subSequence(0, 7) + "-" + s.toString().substring(7, s.length()));
                    }
//
                }
                layout.stockStartDate.setSelection(layout.stockStartDate.length());


            }

            @Override
            public void afterTextChanged(Editable s){

            }
        });

        layout.schoolDate.addTextChangedListener(new TextWatcher(){
            private int _beforeLenght = 0;
            private int _afterLenght = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
                _beforeLenght = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                if(s.length() <= 0){
                    PrintLog.e("onTextChanged: Intput text is wrong (Type : Length)");
                    return;
                }

                char inputChar = s.charAt(s.length() - 1);
                if(inputChar != '-' && (inputChar < '0' || inputChar > '9')){
                    layout.schoolDate.getText().delete(s.length() - 1, s.length());
                    PrintLog.e("onTextChanged: Intput text is wrong (Type : Number)");
                    return;
                }

                _afterLenght = s.length();

                // 삭제 중
                if(_beforeLenght > _afterLenght){
                    // 삭제 중에 마지막에 -는 자동으로 지우기
                    if(s.toString().endsWith("-")){
                        layout.schoolDate.setText(s.toString().substring(0, s.length() - 1));
                    }
                }
                // 입력 중
                else if(_beforeLenght < _afterLenght){
//                    PrintLog.e("_afterLenght = " + _afterLenght);
                    if(_afterLenght == 5 && s.toString().indexOf("-") < 0){
                        layout.schoolDate.setText(s.toString().subSequence(0, 4) + "-" + s.toString().substring(4, s.length()));
                    }else if(_afterLenght == 8){
                        layout.schoolDate.setText(s.toString().subSequence(0, 7) + "-" + s.toString().substring(7, s.length()));
                    }
//
                }
                layout.schoolDate.setSelection(layout.schoolDate.length());
            }

            @Override
            public void afterTextChanged(Editable s){

            }
        });

    }

    String editString;

    /**
     * 사전 발행 VC를 위해 meta id 생성
     */
    private void nonzpkVcCreate(){
        layout.progresslayout.setVisibility(View.VISIBLE);
        new Thread(this::createMetadiumDid).start();
    }


    private boolean checkData(){
        boolean rtn = false;
        String name = layout.name.getText().toString();
        String birth = changeBirth(layout.birth.getText().toString());
        String address = layout.address.getText().toString();
        String phoneNumber = layout.phoneNumber.getText().toString();
        String registerId = layout.stockId.getText().toString();
        String startDate = layout.stockStartDate.getText().toString();
        String uniStartDate = layout.schoolDate.getText().toString();
        PrintLog.e("phoneNumber = " + phoneNumber);
        PrintLog.e("birth = " + birth);
        PrintLog.e("startDate = " + startDate);
        PrintLog.e("uniStartDate = " + uniStartDate);

        if(birth != null && startDate != null && phoneNumber != null && uniStartDate != null){
            if(name.length() != 0 && birth.length() != 0 && address.length() != 0 && phoneNumber.length() != 0 && registerId.length() != 0 && startDate.length() != 0 && uniStartDate.length() != 0){
                if(checkDate(birth) && checkDate(startDate) && checkphone(phoneNumber) && checkDate(uniStartDate)){
                    rtn = true;
                }else{
                    if(!checkDate(birth)){
                        ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "올바른 생년월일을 입력해주세요.", Toast.LENGTH_SHORT)).show();
                    }else if(!checkDate(startDate)){
                        ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "올바른 시작일을 입력해주세요.", Toast.LENGTH_SHORT)).show();
                    }else if(!checkphone(phoneNumber)){
                        ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "형식에 맞지 않는 전화번호 입니다.", Toast.LENGTH_SHORT)).show();
                    }else if(!checkDate(uniStartDate)){
                        ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "올바른 입학일자를 입력해주세요.", Toast.LENGTH_SHORT)).show();
                    }
                }
            }else{
                ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "입력받지 않은 값이 있습니다.", Toast.LENGTH_SHORT)).show();
            }
        }

        PrintLog.e("return = " + rtn);
        return rtn;
    }

    private String changeBirth(String birth){
        String changeBirth = null;
        String tempBirth = null;
        String[] temp = birth.split("-");
        int index = Integer.parseInt(temp[1].substring(0, 1));
        String year = null;
        if(index == 1 || index == 2 || index == 5 || index == 6){
            year = "19";
        }else if(index == 3 || index == 4 || index == 7 || index == 8){
            year = "20";
        }else if(index == 0 || index == 9){
            year = "18";
        }
        tempBirth = year + temp[0];
        changeBirth = tempBirth.substring(0, 4) + "-" + tempBirth.substring(4, 6) + "-" + tempBirth.substring(6, 8);
        return changeBirth;
    }

    private boolean checkDate(String date){
        boolean check = false;
        String temp[] = date.split("-");
        int year = Integer.parseInt(temp[0]);
        int month = Integer.parseInt(temp[1]);
        int day = Integer.parseInt(temp[2]);
        PrintLog.e(year + " " + month + " " + day);
        if(year > 1900 && year < 2500){
            if(month > 0 && month < 13){
                if(day > 0 && day < 31){
                    check = true;
                }else{
                    check = false;
                }
            }else{
                check = false;
            }
        }else{
            check = false;
        }
        return check;
    }

    private boolean checkphone(String phone){
        boolean check = false;
        if(phone.contains("010")){
            check = true;
        }
        return check;
    }

    /**
     * meta did 생성 완료 후 VC 생성
     */
    private void createMetadiumDid(){
        Metadium metadium = Metadium.getInstance();
        FinishListener finish = new FinishListener(){
            @Override
            public void finishOK(String did){
                PrintLog.e("finish create");
                metaDid = did;
                metadium.saveDid(SettingAddPreVCDataActivity.this, "메타디움", did, BlockChainType.METADIUM, true);
//                requestVC();
                createIndy();
            }

            @Override
            public void finishError(String error){
                PrintLog.e("error");
            }
        };

        metadium.createMetaDID(SettingAddPreVCDataActivity.this, finish);

    }

    private void createIconDid(){
        Icon icon = Icon.getInstance();
        FinishListener finish = new FinishListener(){
            @Override
            public void finishOK(String did){
                PrintLog.e("finish icon did create");
                icon.saveDid(SettingAddPreVCDataActivity.this, "아이콘", BlockChainType.ICON);
                requestVC();
            }
            @Override
            public void finishError(String error){

            }
        };
        new Thread(() -> {
            try{
                icon.creatIconDid(finish);
            }catch(Exception e){
                // error
                e.printStackTrace();
            }
        }).start();

    }

    private void createIndy(){

        FinishListener indyFinish = new FinishListener(){
            @Override
            public void finishOK(String did){
                PrintLog.e("finish indy did create");
                indyDID = did;
                indy.saveDid(SettingAddPreVCDataActivity.this, "인디", BlockChainType.INDY);
                preferenceUtil.setIndyDID(indyDID);
//                requestVC();
                createIconDid();
            }

            @Override
            public void finishError(String error){

            }
        };
        new Thread(() -> {
            try{
                indy.createIndyDid(indyFinish);
            }catch(Exception e){
                // error
                e.printStackTrace();
            }
        }).start();
    }

    private void requestVC(){
        String name = layout.name.getText().toString();
        String birth = changeBirth(layout.birth.getText().toString());
        String address = layout.address.getText().toString();
        String phoneNumber = layout.phoneNumber.getText().toString();
        String registerId = layout.stockId.getText().toString();
        String startDate = layout.stockStartDate.getText().toString();
        String schoolId = layout.schoolId.getText().toString();
        String idCardNum = layout.birth.getText().toString();
        String uniStartDate = layout.schoolDate.getText().toString();
        PrintLog.e("IdCardnum = " + idCardNum);
        PrintLog.e("------------ birth = " + birth);
        PrintLog.e("------------ schoolId = " + schoolId);
        requestVCVo = new RequestVCVo(metaDid, name, birth, address, phoneNumber, idCardNum, registerId, startDate, schoolId, birth, "korea", uniStartDate);
        requestIDCredential(requestVCVo, NoneZKPStoreIdCard);
        requestMobileCredential(requestVCVo, NoneZKPStoreMobile);
        requestCardTokenCredential(requestVCVo, NoneZKPStoreCardTokenKorea);
        requestVCVo.setCompany("seoul");
        requestCardTokenCredential(requestVCVo, NoneZKPStoreCardTokenSeoul);
        requestPostCredential(requestVCVo, NoneZKPStorePost);
        requestStockCredential(requestVCVo, NoneZKPStoreStock);
        requestSetSchoolAdd(requestVCVo);
        requestLoginCredential(requestVCVo, NoneZKPStoreLogin);
//        nonZKPrequestVCVo = new RequestGetOfferVo(indyDID, name, birth, address);
//        requestZKPIDgetOffer(nonZKPrequestVCVo);

    }

    /**
     * 신분증 vc 생성
     *
     * @param did  did
     * @param type vc Type
     */
    private void requestIDCredential(RequestVCVo requestData, String type){

        PreVCAPI preVc = PreVCAPI.getInstance();

        Call<ResponseBody> rtn = preVc.preVCAPIInfo.requestPreVC(com.iitp.iitp_demo.Constants.gover_url, requestData);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                            ResponseVC vc = IITPApplication.gson.fromJson(responseJson, ResponseVC.class);
                            savePreVCList(type, vc.getVc());
                            setData(vc.getVc());
                            createFriends();
                            idCredential = true;
                            checkReceiveData();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "IDCredential Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 신분증 vc 생성
     *
     * @param did  did
     * @param type vc Type
     */
    private void requestZKPIDgetOffer(RequestGetOfferVo requestData){
        PreVCAPI preVc = PreVCAPI.getInstance();
        Call<ResponseBody> rtn = preVc.preVCAPIInfo.requestGetOffer(com.iitp.iitp_demo.Constants.gover_url_zkp_get_offer, requestData);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                            ZkpResponse offerData = IITPApplication.gson.fromJson(responseJson, ZkpResponse.class);
                            String credentialRequest = indy.createCredentialRequest(requestData.getDid(), offerData.getCredid(), offerData.getOffer(), offerData.getSchemaid());
                            PrintLog.e("credentialRequest = " + credentialRequest);
                            requestZKPSetCredentialRequest(credentialRequest, offerData);
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "IDCredential Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    private void requestZKPSetCredentialRequest(String requestJson, ZkpResponse offerData){
        PreVCAPI preVc = PreVCAPI.getInstance();
        PrintLog.e("url = " + offerData.getRequestapi());
        CredentialRequestVo requestData = new CredentialRequestVo(offerData.getId(), requestJson);
        Call<ResponseBody> rtn = preVc.preVCAPIInfo.requestSetCredentialRequest(offerData.getRequestapi(), requestData);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                            CredentialRequestResponseVo vcData = IITPApplication.gson.fromJson(responseJson, CredentialRequestResponseVo.class);
                            PrintLog.e("vcData = " + vcData.getVc());
                            PrintLog.e("result = " + vcData.getResult());
                            savePreVCList(ZKP_IdentificationCredential, vcData.getVc());
                            String vc = getCredential(vcData.getVc());
                            PrintLog.e("vc = " + vc);
                            String storeRequestId = indy.storeCredential(offerData.getId(), vc);
                            indy.setRequestIDCredentialData(SettingAddPreVCDataActivity.this);
                            checkReceiveData();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "IDCredential Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 모바일  vc 생성
     *
     * @param did  did
     * @param type vc Type
     */
    private void requestMobileCredential(RequestVCVo requestData, String type){
        PreVCAPI preVc = PreVCAPI.getInstance();
        Call<ResponseBody> rtn = preVc.preVCAPIInfo.requestPreVC(com.iitp.iitp_demo.Constants.tele_url, requestData);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                            ResponseVC vc = IITPApplication.gson.fromJson(responseJson, ResponseVC.class);
                            savePreVCList(type, vc.getVc());
                            mobileCredential = true;
                            checkReceiveData();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "MobileCredential Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 카드토큰  vc 생성
     *
     * @param did  did
     * @param type vc Type
     */
    private void requestCardTokenCredential(RequestVCVo requestData, String type){
        PreVCAPI preVc = PreVCAPI.getInstance();
        Call<ResponseBody> rtn = preVc.preVCAPIInfo.requestPreVC(com.iitp.iitp_demo.Constants.card_url, requestData);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                            ResponseVC vc = IITPApplication.gson.fromJson(responseJson, ResponseVC.class);
                            savePreVCList(type, vc.getVc());
                            cardTokenCredential = true;
                            checkReceiveData();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "CardtokenCredential Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 우체국 vc 생성
     *
     * @param did  did
     * @param type vc Type
     */
    private void requestPostCredential(RequestVCVo requestData, String type){
        PreVCAPI preVc = PreVCAPI.getInstance();
        Call<ResponseBody> rtn = preVc.preVCAPIInfo.requestPreVC(com.iitp.iitp_demo.Constants.post_url, requestData);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                            ResponseVC vc = IITPApplication.gson.fromJson(responseJson, ResponseVC.class);
                            savePreVCList(type, vc.getVc());
                            postCredential = true;
                            checkReceiveData();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "PostCredential Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 증권사 vc 생성
     *
     * @param did  did
     * @param type vc Type
     */
    private void requestStockCredential(RequestVCVo requestData, String type){
        PreVCAPI preVc = PreVCAPI.getInstance();
        Call<ResponseBody> rtn = preVc.preVCAPIInfo.requestPreVC(com.iitp.iitp_demo.Constants.stock_url, requestData);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){

                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                            ResponseVC vc = IITPApplication.gson.fromJson(responseJson, ResponseVC.class);
                            savePreVCList(type, vc.getVc());
                            stockCredential = true;
                            checkReceiveData();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "StockCredential Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 대학교 학생 추가 생성
     *
     * @param did  did
     * @param type vc Type
     */
    private void requestSetSchoolAdd(RequestVCVo requestData){
        PreVCAPI preVc = PreVCAPI.getInstance();
        Call<ResponseBody> rtn = preVc.preVCAPIInfo.requestPreVC(com.iitp.iitp_demo.Constants.school_add_url, requestData);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){

                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "university Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    private void requestLoginCredential(RequestVCVo requestData, String type){
        PreVCAPI preVc = PreVCAPI.getInstance();

        Call<ResponseBody> rtn = preVc.preVCAPIInfo.requestPreVC(com.iitp.iitp_demo.Constants.login_url, requestData);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){

                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                            ResponseVC vc = IITPApplication.gson.fromJson(responseJson, ResponseVC.class);
                            savePreVCList(type, vc.getVc());
                            loginCredential = true;
                            checkReceiveData();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingAddPreVCDataActivity.this, "LoginPreVC Server error", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
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
        if(jwt != null){
            CommonPreference.getInstance(SettingAddPreVCDataActivity.this).getSecureSharedPreferences().edit().putString(key, jwt).apply();
        }
    }

    /**
     * data set
     *
     * @param jwt jwt
     */
    private void setData(String jwt){
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        String payload = preferenceUtil.getPayload(jwt);
        JWTClaimsSet jwtClaimsSet = null;
        try{
            jwtClaimsSet = JWTClaimsSet.parse(payload);
        }catch(ParseException e){
            PrintLog.e("setData error");
        }
//        metaDid = jwtClaimsSet.getSubject();
        PrintLog.e("metaDid = " + metaDid);
        VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
        PrintLog.e("credential = " + credential.toJSONString());
        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
        for(String key : claims.keySet()){
            PrintLog.e(key + " : " + claims.get(key));
        }
        name = claims.get("name").toString();
    }

    /**
     * agnet 친구 추가
     */
    private void createFriends(){
        PrintLog.e("push is set");
        MutableLiveData<pushResponseVo> response = new MutableLiveData<>();
        response.observe(this, pushAPIResponseVo -> {
            if(pushAPIResponseVo != null){
                PrintLog.e("status = " + pushAPIResponseVo.getStatus());
                checkFCMToken();
            }else{
//                        networkErrorDialog(SplashActivity.this);
            }
        });
        AgentAPI.getInstance().requestCreateFriends(metaDid, name, response);
    }

    /**
     * check receive vc data all
     */
    private void checkReceiveData(){
        if(idCredential && mobileCredential && cardTokenCredential && stockCredential && postCredential && loginCredential){
            layout.progresslayout.setVisibility(View.GONE);
            showDialogFinish(getString(R.string.setting_prevc_desc));
        }

    }

    /**
     * fcm token 체크
     */
    private void checkFCMToken(){
        PrintLog.e("push is set");
        ArrayList<DidDataVo> didList;
        String didListJson = CommonPreference.getInstance(SettingAddPreVCDataActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
        if(didListJson != null){
            didList = IITPApplication.gson.fromJson(didListJson, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
            String did = null;
            for(DidDataVo temp : didList){
                if(temp.getFavorite()){
                    did = temp.getDid();
                }
            }
            MutableLiveData<pushResponseVo> response = new MutableLiveData<>();
            response.observe(this, pushAPIResponseVo -> {
                if(pushAPIResponseVo != null){
                    PrintLog.e("status = " + pushAPIResponseVo.getStatus());
                    PrintLog.e("status = " + pushAPIResponseVo.getErrorMessage());
                }else{
//                        networkErrorDialog(SplashActivity.this);
                }
            });
            AgentAPI.getInstance().registerToken(response, did);
        }
    }

    /**
     * finish dialog
     *
     * @param text
     */
    private void showDialogFinish(String text){
        LayoutInflater inflater = (LayoutInflater) SettingAddPreVCDataActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        title.setText(text);
        twoBtn.setVisibility(View.GONE);
        oneBtn.setVisibility(View.VISIBLE);
        btPositive.setText(R.string.ok);
        textview.setText(getString(R.string.setting_prevc_dialog));
        btPositive.setOnClickListener(v -> {
            customDialog.dismiss();
            finish();
        });
    }


    private String setTime(String time){
        String changeTimeFormat = null;
        if(time.length() == 8){
            String year;
            String month;
            String day;
            year = time.substring(0, 4);
            month = time.substring(4, 6);
            day = time.substring(6, 8);
            changeTimeFormat = year + "-" + month + "-" + day;
        }
        return changeTimeFormat;
    }

    private String setPhone(String phone){
        String changePhoneFormat = null;
        if(phone.length() == 11){
            String first;
            String second;
            String third;
            first = phone.substring(0, 3);
            second = phone.substring(3, 7);
            third = phone.substring(7, 11);
            changePhoneFormat = first + "-" + second + "-" + third;
        }
        return changePhoneFormat;
    }

    private String getCredential(String vc){
        String credentialData = null;
        String temp = preferenceUtil.getPayload(vc);
        ZkpClaimVo zkpdagta = IITPApplication.gson.fromJson(temp, ZkpClaimVo.class);
        String ddd = zkpdagta.getClaim();
        credentialData = new String(android.util.Base64.decode(ddd, 0));
        return credentialData;
    }
}
