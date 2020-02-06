package com.iitp.iitp_demo.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.ECKeyUtils;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.PublicKeyListener;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.model.ClaimVo;
import com.iitp.iitp_demo.activity.model.CredentialVo;
import com.iitp.iitp_demo.api.CredentialAPI;
import com.iitp.iitp_demo.api.model.JwtVo;
import com.iitp.iitp_demo.api.model.ResponseResultVo;
import com.iitp.iitp_demo.api.model.ResponseVo;
import com.iitp.iitp_demo.databinding.ActivityRequestPresentationBinding;
import com.iitp.iitp_demo.model.DidVo;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ProgressDialog;
import com.iitp.iitp_demo.util.VerifyJwt;

import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import foundation.icon.did.Credential;
import foundation.icon.did.Presentation;
import foundation.icon.did.core.AlgorithmProvider;
import foundation.icon.did.core.DidKeyHolder;
import foundation.icon.did.exceptions.AlgorithmException;
import foundation.icon.did.jwt.IssuerDid;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestPresentationActivity extends BaseActivity{
    private Gson gson = new Gson();
    private ActivityRequestPresentationBinding layoutBinding;

    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> idNumber = new ObservableField<>();
    public ObservableField<String> address = new ObservableField<>();
    public ObservableField<String> issuer = new ObservableField<>();
    public ObservableField<String> date = new ObservableField<>();
    public ObservableField<String> position = new ObservableField<>();
    public ObservableField<String> department = new ObservableField<>();
    public ObservableField<String> regDate = new ObservableField<>();
    public ObservableField<String> message1 = new ObservableField<>();
    public ObservableField<String> metaId = new ObservableField<>();
    private int type = 0; // 0: 재직증명, 1: 은행계좌 개설


    private CredentialVo idCredentialVo;
    private CredentialVo officeCredentialVo;

    private String idCredential;
    private String officeCredential;
    private ProgressDialog progressDialog;

    private CredentialVo credentialVo;

    private String reqUrl;
    private String redirectUrl;

    private AlertDialog.Builder builder;

    private Context ctx = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent != null){
            did = getDidData();
            Uri uri = getIntent().getData();
            if(uri != null){
                String uriDecode = null;
                try{
                    uriDecode = URLDecoder.decode(uri.toString(), "UTF-8");
                }catch(UnsupportedEncodingException e){
                    PrintLog.e("RequestPresentationActivity error");
                }

                Log.e("TEST", "uri : " + uriDecode);
                Set<String> queryList = uri.getQueryParameterNames();
                SharedPreferences pref = CommonPreference.getInstance(this).getSecureSharedPreferences();

//                String idCredentialJson = pref.getString(Constants.ID_CREDENTIAL_JSON, null);
//                String officeCredentialJson = pref.getString(Constants.OFFICE_CREDENTIAL_JSON, null);
//                idCredential = pref.getString(Constants.ID_CREDENTIAL, null);
//                officeCredential = pref.getString(Constants.OFFICE_CREDENTIAL, null);
                idCredential = did.getIdCredential();
                officeCredential = did.getOfficeCredential();
                message1.set(getString(R.string.requst_presentation_message1));
                if(queryList.size() > 2){
                    type = 1;
                    officeCredentialVo = gson.fromJson(did.getOfficeCredentialJson(), CredentialVo.class);
                    idCredentialVo = gson.fromJson(did.getIdCredentialJson(), CredentialVo.class);
                }else{
                    type = 0;
                    idCredentialVo = gson.fromJson(did.getIdCredentialJson(), CredentialVo.class);
                }
                ctx = this;
                builder = new AlertDialog.Builder(this);
                String jwt = uri.getQueryParameter("jwt");
                reqUrl = uri.getQueryParameter("respUrl");
                redirectUrl = uri.getQueryParameter("redirect");
                if(jwt != null){
                    parseRequestJwt(jwt);
                }
                Log.e("TEST", "jwt : " + jwt);
                Log.e("TEST", "respUrl : " + reqUrl);
                Log.e("TEST", "redirect : " + redirectUrl);

            }
        }
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.activity_request_presentation);
        layoutBinding.setActivity(this);

        setSupportActionBar(layoutBinding.toolbar.appbar);
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            message1.set(getString(R.string.requst_presentation_message1));
            if(type == 0){
                titleText.setText(R.string.requst_presentation_title1);
            }else if(type == 1){
                titleText.setText(R.string.requst_presentation_title2);
            }
        }
        progressDialog = new ProgressDialog(this);
        init();

    }

    private void init(){

        layoutBinding.idCredential.setVisibility(View.VISIBLE);
        layoutBinding.officeCredential.setVisibility(View.GONE);
        if(idCredentialVo != null){
            ClaimVo claims = idCredentialVo.getClaim();
            name.set(claims.getName());
            idNumber.set(claims.getrRN().substring(0,6)+" - "+claims.getrRN().substring(6,13));
            address.set(claims.getAddress());
            issuer.set(claims.getIssuer());
            date.set(claims.getIssueDate());
            metaId.set(did.getIss());
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_title))
                    .setMessage(getString(R.string.no_id_credential))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> finish());
            builder.show();
        }

    }

    public void noClick(){
        finish();
    }

    public void yesClick(){

        if(type == 0){
            FingerBioFactory fingerBioFactory = new FingerBioFactory(this, new BiometricPrompt.AuthenticationCallback(){
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString){
                    PrintLog.e("onAuthenticationError");
                    PrintLog.e("Error = " + errorCode);
                    if(errorCode == BiometricConstants.ERROR_HW_UNAVAILABLE){
                        Toast.makeText(ctx, ctx.getString(R.string.biometric_no_hw), Toast.LENGTH_SHORT).show();
                    }else if(errorCode == BiometricConstants.ERROR_NO_BIOMETRICS){
                        Toast.makeText(ctx, ctx.getString(R.string.biometric_no_bio), Toast.LENGTH_SHORT).show();
                    }
                    super.onAuthenticationError(errorCode, errString);
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result){
                    PrintLog.e("onAuthenticationSucceeded");
                    super.onAuthenticationSucceeded(result);
                    progressDialog.setCancelable(false); // 주변 클릭 터치 시 프로그래서 사라지지 않게 하기

                    runOnUiThread(() -> progressDialog.show());
                    makeResponsePresentation();
                }


                @Override
                public void onAuthenticationFailed(){
                    PrintLog.e("onAuthenticationFailed");
                    super.onAuthenticationFailed();
                }
            });
            fingerBioFactory.setting(getString(R.string.requst_presentation_title1), getString(R.string.checkout_message1), getString(R.string.cancel));
            fingerBioFactory.authenticate();
        }else{
            if(officeCredential != null){
                if(layoutBinding.idCredential.getVisibility() == View.VISIBLE){
                    message1.set(getString(R.string.requst_presentation_message2));
                    layoutBinding.idCredential.setVisibility(View.GONE);
                    layoutBinding.officeCredential.setVisibility(View.VISIBLE);
                    ClaimVo claims = officeCredentialVo.getClaim();
                    name.set(claims.getName());
                    position.set(claims.getTitle());
                    department.set(claims.getDepartment());
                    address.set(claims.getAddress());
                    regDate.set(claims.getStartDate());
                    issuer.set(claims.getIssuer());
                }else{

                    FingerBioFactory fingerBioFactory = new FingerBioFactory(this, new BiometricPrompt.AuthenticationCallback(){
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString){
                            PrintLog.e("onAuthenticationError");
                            super.onAuthenticationError(errorCode, errString);
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result){
                            PrintLog.e("onAuthenticationSucceeded");
                            super.onAuthenticationSucceeded(result);
                            progressDialog.setCancelable(false); // 주변 클릭 터치 시 프로그래서 사라지지 않게 하기

                            runOnUiThread(() -> progressDialog.show());
                            makeResponsePresentation();
                        }


                        @Override
                        public void onAuthenticationFailed(){
                            PrintLog.e("onAuthenticationFailed");
                            super.onAuthenticationFailed();
                        }
                    });
                    fingerBioFactory.setting(getString(R.string.request_account), getString(R.string.checkout_message1), getString(R.string.cancel));
                    fingerBioFactory.authenticate();
                }

            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.dialog_title))
                        .setMessage(getString(R.string.no_office_credential))
                        .setPositiveButton(getString(R.string.yes), (dialog, which) -> finish());
                builder.show();
            }

        }
    }

    /*private void makeRequestPresentation(){
        String did = KeyManagerUtil.getDid(this);
        KeyManager keyManager = KeyManager.getInstance();
        String issuerDid = "did:icon:01:961b6cd64253fb28c9b0d3d224be5f9b18d49f01da390f08";
        String version = "1.0";
        String keyid = KeyManagerUtil.getKeyId(this);
        String manageAddress = KeyManagerUtil.getManagementAddress(this);
        String orgprivateKey = Numeric.toHexStringNoPrefixZeroPadded(keyManager.getPrivateKey(this, manageAddress), 64);
        PrivateKey pk = ECKeyUtils.toECPrivateKey(Numeric.toBigInt(orgprivateKey), "secp256k1");
        AlgorithmProvider.Type type = AlgorithmProvider.Type.ES256K;
        PrintLog.e("pk = " + orgprivateKey);

        List<String> claimTypes = Arrays.asList("name", "title", "department", "birthday", "address", "issuer", "startDate");

        DidKeyHolder didKeyHolder = new DidKeyHolder.Builder()
                .did(did)
                .keyId(keyid)
                .type(type)
                .privateKey(pk)
                .build();


        String nonce = Hex.toHexString(AlgorithmProvider.secureRandom().generateSeed(4));

        ClaimRequest request = new ClaimRequest.Builder(ClaimRequest.Type.PRESENTATION)
                .didKeyHolder(didKeyHolder)
                .responseId(did)
                .requestDate(new Date())
                .requestClaimTypes(claimTypes)
                .nonce(nonce)
                .version(version)
                .build();

//        String unsigendJwt = request.compact();

        String signedJwt = null;
        try{
            signedJwt = request.getJwt().sign(didKeyHolder.getPrivateKey());
        }catch(AlgorithmException e){
            e.printStackTrace();
        }finally{

        }
        PrintLog.e("jwt 1 = " + signedJwt);
        PrintLog.e("jwt 2 = " + request.getJwt());

//        responseCredential(sigendJwt);
    }*/

    private void makeResponsePresentation(){
//        String did = KeyManagerUtil.getDid(this);
//        KeyManager keyManager = KeyManager.getInstance();
        AlgorithmProvider.Type algorithmType = AlgorithmProvider.Type.ES256K;
//        String manageAddress = KeyManagerUtil.getManagementAddress(this);
//        String orgPrivateKey = Numeric.toHexStringNoPrefixZeroPadded(keyManager.getPrivateKey(this, manageAddress), 64);
        String orgPrivateKey = did.getPrivateKey();
        PrivateKey pk = ECKeyUtils.toECPrivateKey(Numeric.toBigInt(orgPrivateKey), "secp256k1");
        String address1 = did.getAddress();
        String keyId =  "MetaManagementKey#" + address1.replace("0x", "");
        String version = "1.0";

        DidKeyHolder ownerKeyHolder = new DidKeyHolder.Builder()
//                .did(did)
                .did(did.getIss())
                .keyId(keyId)
                .type(algorithmType)
                .privateKey(pk)
                .build();

        Presentation presentation = new Presentation.Builder()
                .didKeyHolder(ownerKeyHolder)
                .nonce(credentialVo.getNonce())
                .version(version)
                .build();

        presentation.addCredential(idCredential);
        if(type == 1){
            presentation.addCredential(officeCredential);
        }
        String signedJwt = null;
        try{
            signedJwt = ownerKeyHolder.sign((presentation.buildJwt()));
        }catch(AlgorithmException e){
            PrintLog.e("RequestPresentationActivity error");
        }

        PrintLog.e("jwt 1 = " + signedJwt);
        PrintLog.e("jwt 2 = " + presentation.getJti());

        responseCredential(signedJwt);
    }

    private void parseRequestJwt(String jwt){
        String[] requestJwt = jwt.split("\\.");
        String requestJwtJson = new String(Base64.decode(requestJwt[1], Base64.URL_SAFE));
        PrintLog.e("requestJson : " + requestJwtJson);
        credentialVo = gson.fromJson(requestJwtJson, CredentialVo.class);
    }

    private void responseCredential(String jwt){

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

                            if(type == 0){
                                ResponseVo responseVo = gson.fromJson(responseJson, ResponseVo.class);
                                verifyCheck(responseVo);
//                                sendActivity(responseVo);
                            }else{
                                ResponseResultVo responseVo = gson.fromJson(responseJson, ResponseResultVo.class);
                                boolean rtn = responseVo.getSuccess();
                                if(rtn){
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri = Uri.parse(redirectUrl);
                                    intent.setData(uri);
                                    startActivity(intent);
                                    finish();
                                }else{

                                    builder.setTitle(getString(R.string.dialog_error))
                                            .setMessage(responseVo.getResult().get(0))
                                            .setPositiveButton(getString(R.string.yes), (dialog, which) -> finish());
                                    runOnUiThread(() -> builder.show());

                                }

                            }

                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    assert response.errorBody() != null;
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                progressDialog.dismiss();
            }
        });


    }

    private void sendActivity(ResponseVo response){
        progressDialog.dismiss();
        saveCredential(response);
        Intent intent = new Intent(RequestPresentationActivity.this, FinishGenerateOfficeCredentialActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveCredential(ResponseVo responseVo){
        String credential = responseVo.getResult().getCredential();
        PrintLog.e("response jwt = " + credential);
        String[] credentialJwt = credential.split("\\.");
        String credentialJson = new String(Base64.decode(credentialJwt[1], Base64.URL_SAFE));
        PrintLog.e("credential = " + credential);
        PrintLog.e("credentialJson = " + credentialJson);
        did.setOfficeCredential(credential);
        did.setOfficeCredentialJson(credentialJson);
        didVoList.remove(didIndex);
        didVoList.add(didIndex, did);
        String newDidlist = gson.toJson(didVoList);
        commPref.setValue(Constants.ALL_DID_DATA, newDidlist);
    }

    private void verifyCheck(ResponseVo responseVo){

        Credential credential = Credential.valueOf(responseVo.getResult().getCredential());
        IssuerDid issuerDid = credential.getIssuerDid();
        PrintLog.e("keyId : " + issuerDid.getDid());
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
                            .setPositiveButton(getString(R.string.yes), (dialog, which) -> finish());

                    runOnUiThread(builder::show);
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
    private CommonPreference commPref = null;

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
