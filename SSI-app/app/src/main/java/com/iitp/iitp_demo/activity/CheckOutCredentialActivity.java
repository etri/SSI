package com.iitp.iitp_demo.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.ECKeyUtils;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.PublicKeyListener;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.api.CredentialAPI;
import com.iitp.iitp_demo.api.model.JwtVo;
import com.iitp.iitp_demo.api.model.ResponseVo;
import com.iitp.iitp_demo.databinding.ActivityCheckoutCredantialBinding;
import com.iitp.iitp_demo.model.DidVo;
import com.iitp.iitp_demo.model.UserDataVo;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ProgressDialog;
import com.iitp.iitp_demo.util.VerifyJwt;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foundation.icon.did.Credential;
import foundation.icon.did.core.AlgorithmProvider;
import foundation.icon.did.core.DidKeyHolder;
import foundation.icon.did.exceptions.AlgorithmException;
import foundation.icon.did.jwt.IssuerDid;
import foundation.icon.did.protocol.ClaimRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckOutCredentialActivity extends BaseActivity{
    private CommonPreference pref;
    private Gson gson = new Gson();
    private static ActivityCheckoutCredantialBinding layout;
    public String name;
    public String idNumber;
    public String address;
    public String issuer;
    public String metaId;
    private String reqUrl;

    private ProgressDialog progressDialog;

    private Context ctx = null;
    private CommonPreference commPref = null;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent != null){
            Uri uri = getIntent().getData();
            if(uri != null){
                String uriDecode = null;
                try{
                    uriDecode = URLDecoder.decode(uri.toString(), "UTF-8");
                }catch(UnsupportedEncodingException e){
                    PrintLog.e("CheckOutCredentialActivity error");
                }
                reqUrl = uri.getQueryParameter("reqUrl");
//                String jwt = uri.getQueryParameter("jwt"); /추후 사
                String requestCredential = uri.getQueryParameter("requestCredential");
                Log.e("TEST", "reqUrl : " + reqUrl);
                Log.e("TEST", "requestCredential : " + requestCredential);
            }
        }

        pref = CommonPreference.getInstance(this);
        ctx = this;
        layout = DataBindingUtil.setContentView(this, R.layout.activity_checkout_credantial);
        layout.setActivity(this);
        setSupportActionBar(layout.toolbar.appbar);
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            titleText.setText(R.string.checkout_title);
        }
        init();
        pref = CommonPreference.getInstance(this);
    }

    private void init(){ //todo 사용자 리스트가 없는 경우 죽는 문제

        did = getDidData();
        List<UserDataVo> list = new ArrayList<>();
        list = getUserData(this);
        int index = pref.getIntValue(Constants.USER_DATA_INDEX, 0);
        progressDialog = new ProgressDialog(this);
        if(list.size() == 0){
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_title))
                    .setMessage(getString(R.string.dialog_no_user))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            finish();
                        }
                    });

            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    builder.show();
                }
            });
        }else{
            UserDataVo data = list.get(index);
            name = data.getName();
            address = data.getAddress();
            idNumber = data.getId();
            issuer = getString(R.string.tab_title_4);
            metaId = did.getIss();
        }



    }

    public void noClick(){
        finish();
    }

    public void yesClick(){
        FingerBioFactory fingerBioFactory = new FingerBioFactory(this, new BiometricPrompt.AuthenticationCallback(){
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString){
                PrintLog.e("onAuthenticationError");
                PrintLog.e("Error = "+errorCode);
                if(errorCode ==BiometricConstants.ERROR_HW_UNAVAILABLE ){
                    Toast.makeText(ctx, ctx.getString(R.string.biometric_no_hw), Toast.LENGTH_SHORT).show();
                }else if(errorCode ==BiometricConstants.ERROR_NO_BIOMETRICS){
                    Toast.makeText(ctx,ctx.getString(R.string.biometric_no_bio), Toast.LENGTH_SHORT).show();
                }

                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result){
                PrintLog.e("onAuthenticationSucceeded");
                super.onAuthenticationSucceeded(result);

                progressDialog.setCancelable(false); // 주변 클릭 터치 시 프로그래서 사라지지 않게 하기

                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        progressDialog.show();
                    }
                });
                makeRequestCredential();
            }


            @Override
            public void onAuthenticationFailed(){
                PrintLog.e("onAuthenticationFailed");
                super.onAuthenticationFailed();
            }
        });
        fingerBioFactory.setting(getString(R.string.checkout_title), getString(R.string.checkout_message1), getString(R.string.cancel));
        fingerBioFactory.authenticate();
    }


    public void makeRequestCredential(){
//        String did = KeyManagerUtil.getDid(this);
//        KeyManager keyManager = KeyManager.getInstance();
        String issuerDid = "did:icon:01:961b6cd64253fb28c9b0d3d224be5f9b18d49f01da390f08";
        String version = "1.0";
//        String keyid = KeyManagerUtil.getKeyId(this);
        String address1 = did.getAddress();
        String keyid =  "MetaManagementKey#" + address1.replace("0x", "");
        PrintLog.e("keyId = "+keyid);
//        String manageAddress = KeyManagerUtil.getManagementAddress(this);
//        String manageAddress = did.getAddress();
//        String orgprivateKey = Numeric.toHexStringNoPrefixZeroPadded(keyManager.getPrivateKey(this, manageAddress), 64);
        String orgPrivateKey = did.getPrivateKey();
        PrintLog.e("manageAddress = "+keyid);
        PrintLog.e("orgprivateKey = "+orgPrivateKey);
        PrivateKey pk = ECKeyUtils.toECPrivateKey(Numeric.toBigInt(orgPrivateKey), "secp256k1");
        AlgorithmProvider.Type type = AlgorithmProvider.Type.ES256K;
        PrintLog.e("pk = " + orgPrivateKey);

        DidKeyHolder didKeyHolder = new DidKeyHolder.Builder()
//                .did(did)
                .did(did.getIss())
                .keyId(keyid)
                .type(type)
                .privateKey(pk)
                .build();

        Map claims = new HashMap();
        claims.put("name", name);
        claims.put("RRN", idNumber);
        claims.put("address", address);
        claims.put("issuer", issuer);
        claims.put("issueDate", getDate());

        String nonce = Hex.toHexString(AlgorithmProvider.secureRandom().generateSeed(4));

        ClaimRequest request = new ClaimRequest.Builder(ClaimRequest.Type.CREDENTIAL)
                .didKeyHolder(didKeyHolder)
                .requestClaims(claims)
                .responseId(issuerDid)
                .nonce(nonce)
                .version(version)
                .build();
        String requestJwt = null;
        try{
            requestJwt = request.getJwt().sign(didKeyHolder.getPrivateKey());
        }catch(AlgorithmException e){
            PrintLog.e("CheckOutCredentialActivity error");
        }
        PrintLog.e("jwt 1 = " + requestJwt);
        PrintLog.e("jwt 2 = " + request.getJwt());

        requestCredential(requestJwt);
    }

    private String getDate(){
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String getTime = sdf.format(date);
        PrintLog.e("time  = " + getTime);
        return getTime;
    }

    private void requestCredential(String jwt){

        CredentialAPI credentialAPI = CredentialAPI.getInstance();
        PrintLog.e("jwt = " + jwt);
        JwtVo jwtVo = new JwtVo(jwt);
        String json = gson.toJson(jwtVo);
        PrintLog.e("json : " + json);
        Call<ResponseBody> rtn = credentialAPI.verificationAPIInfo.requestResidentIdCard(reqUrl, json);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                progressDialog.dismiss();
                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);

                            ResponseVo responseVo = gson.fromJson(responseJson, ResponseVo.class);
                            verifyCheck(responseVo);
//                            sendActivity(responseVo);
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    PrintLog.e("response = " + response.errorBody().toString());
                }
//
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                progressDialog.dismiss();
            }
        });


    }

    private void sendActivity(ResponseVo response){
        saveCredential(response);
        Intent intent = new Intent(CheckOutCredentialActivity.this, FinishGenerateIdCredentialActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveCredential(ResponseVo responseVo){
        String credential = responseVo.getResult().getCredential();
        PrintLog.e("response jwt = " + credential);
        SharedPreferences pref = CommonPreference.getInstance(this).getSecureSharedPreferences();
        String credentialJwt[] = credential.split("\\.");
        String credentialJson = new String(Base64.decode(credentialJwt[1], Base64.URL_SAFE));
        PrintLog.e("credential = " + credential);
        PrintLog.e("credentialJson = " + credentialJson);
        did.setIdCredential(credential);
        did.setIdCredentialJson(credentialJson);
        didVoList.remove(didIndex);
        didVoList.add(didIndex,did);
        String newdidlist = gson.toJson(didVoList);
        commPref.setValue(Constants.ALL_DID_DATA,newdidlist);
//        pref.edit().putString(Constants.ID_CREDENTIAL, credential).apply();
//        pref.edit().putString(Constants.ID_CREDENTIAL_JSON, credentialJson).apply();
    }

    private void verifyCheck(ResponseVo responseVo){

        Credential credential = Credential.valueOf(responseVo.getResult().getCredential());
        IssuerDid issuerDid = credential.getIssuerDid();
        PrintLog.e("DId : " + issuerDid.getDid());
        PrintLog.e("keyId : " + issuerDid.getKeyId());
        VerifyJwt verifyJwt = new VerifyJwt();
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        PublicKeyListener publicKeyListener = new PublicKeyListener(){
            @Override
            public void requestComplete(String publicKeyHex){

                if(verifyJwt.checkVerify(responseVo, publicKeyHex)){
                    sendActivity(responseVo);
                }else{
                    builder.setTitle(getString(R.string.dialog_verify_error))
                            .setMessage(getString(R.string.dialog_verify_error))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    finish();
                                }
                            });

                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            builder.show();
                        }
                    });
                }
            }

            @Override
            public void error(String error){

            }
        };
        verifyJwt.getPublicKey(publicKeyListener, issuerDid);
    }
    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }


    String didlist = null;
    int didIndex = 0;
    DidVo did = null;
    List<DidVo> didVoList = new ArrayList<>();

    private DidVo getDidData(){
        commPref = CommonPreference.getInstance(this);
        DidVo didData = null;
        didlist = commPref.getStringValue(Constants.ALL_DID_DATA, null);
        didIndex = commPref.getIntValue(Constants.DID_DATA_INDEX, 0);
        didVoList = gson.fromJson(didlist, new TypeToken<ArrayList<DidVo>>(){
        }.getType());
        didData = didVoList.get(didIndex);
        return didData;
    }

}

