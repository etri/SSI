package com.iitp.iitp_demo.activity.request;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;

import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.PincodeActivity;
import com.iitp.iitp_demo.activity.credential.CredentialDetailUniActivity;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.activity.model.IndyCredentialVo;
import com.iitp.iitp_demo.api.PreVCAPI;
import com.iitp.iitp_demo.api.model.CredentialRequestResponseVo;
import com.iitp.iitp_demo.api.model.CredentialRequestVo;
import com.iitp.iitp_demo.api.model.DidResponseDataVo;
import com.iitp.iitp_demo.api.model.DidvoResponse;
import com.iitp.iitp_demo.api.model.SendDIDVo;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.chain.indy.ZkpClaimVo;
import com.iitp.iitp_demo.databinding.ActivityDidsSelectBinding;
import com.iitp.iitp_demo.util.BiometricDialog;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.OnSingleClickListener;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.MnemonicUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.iitp.core.crypto.KeyManager.BIP44_META_PATH;
import static com.iitp.iitp_demo.Constants.DID;
import static com.iitp.iitp_demo.Constants.JWT_DATA;
import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.CompanyType;
import static com.iitp.iitp_demo.chain.VCVPCreater.OfficeCredential;
import static com.iitp.iitp_demo.chain.VCVPCreater.ZKP_IdentificationCredential;

public class DidSelectWebActivity extends BaseActivity{

    private ActivityDidsSelectBinding layout;
    private int index = 0;
    private ArrayList<DidDataVo> didList = new ArrayList<DidDataVo>();
    private DidListAdapter adapter;
    private PreferenceUtil preferenceUtil;
    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;
    private boolean checkBio = false;
    private String messageId = null;
    private String requestUrl = null;
    private Indy indy;
    private String did = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_dids_select);
        setSupportActionBar(layout.toolbar.appbar);
        setActionBarSet(layout.toolbar, getString(R.string.did_select_title), false);
        preferenceUtil = PreferenceUtil.getInstance(this);
        Intent intent = getIntent();
        messageId = intent.getStringExtra("msgid");
        requestUrl = intent.getStringExtra("requesturl");
        PrintLog.e("messageId = " + messageId);
        PrintLog.e("requestUrl = " + requestUrl);
        init();
    }

    /**
     * init
     */
    private void init(){
        didList = MainActivity.getDidList(DidSelectWebActivity.this);
        adapter = new DidListAdapter();
        layout.didList.setAdapter(adapter);

        layout.selectBtn.setOnClickListener(new OnSingleClickListener(){
            @Override
            public void onSingleClick(View v){
                BiometricUtils.hasBiometricEnrolled(DidSelectWebActivity.this);
                if((BiometricUtils.getPincode(DidSelectWebActivity.this) == null && !(BiometricUtils.isFingerPrint(DidSelectWebActivity.this)))){
                    if(BiometricUtils.checkFingerprint(DidSelectWebActivity.this)){
                        fingerBioFactory(DidSelectWebActivity.this);
                    }else{
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                startPincodeActivity();
                            }
                        }, 1000);
                    }
                }else{
                    if(BiometricUtils.isFingerPrint(DidSelectWebActivity.this)){
                        fingerBioFactory(DidSelectWebActivity.this);
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
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        indy = Indy.getInstance(DidSelectWebActivity.this);
        checkBio = false;
    }

    /**
     * selectDID
     */
    private void selectDID(){
        Intent intent = new Intent();
        int index = adapter.getSelectIndex();
        PrintLog.e("index = " + index);
        String did = didList.get(index).getDid();
        PrintLog.e("did= " + did);
        if(did.contains("meta") || did.contains("icon") || did.contains("sov")){
            intent.putExtra(DID, did);
            setResult(RESULT_OK, intent);
            finish();
        }else{
            ToastUtils.custom(Toast.makeText(DidSelectWebActivity.this, "지원하지 않는 DID 입니다.  ", Toast.LENGTH_SHORT)).show();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }


//    private ECKeyPair getManagementECKeyPair(String mnemonic){
//        if(mnemonic != null){
//            byte[] seed = MnemonicUtils.generateSeed(MnemonicUtils.generateMnemonic(MnemonicUtils.generateEntropy(mnemonic)), (String) null);
//            Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
//            return Bip32ECKeyPair.deriveKeyPair(master, BIP44_META_PATH);
//        }else{
//            return null;
//        }
//    }

    /**
     * startPincodeActivity
     */
    private void startPincodeActivity(){
        Intent intent;
        intent = new Intent(this, PincodeActivity.class);
        intent.putExtra(PIN_SETTING_TYPE, 2);
        startActivityForResult(intent, REQUEST_CODE_VERIFY_PIN_APP_LOCK);
        //finish();
    }

    @Override
    public void onBackPressed(){
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        PrintLog.e("onDestroy");
    }

    class DidListAdapter extends BaseAdapter{

        private int mSelectedRadioPosition;
        private RadioButton mLastSelectedRadioButton;

        DidListAdapter(){

        }

        @Override
        public int getCount(){
            return didList.size();
        }

        @Override
        public Object getItem(int i){
            return null;
        }

        @Override
        public long getItemId(int i){
            return i;
        }

        public int getSelectIndex(){
            return mSelectedRadioPosition;
        }


        @Override
        public View getView(int i, View convertView, ViewGroup parent){
            DidListAdapter.Holder holder;
            DidDataVo data = didList.get(i);
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_did_2, parent, false);
                holder = new DidListAdapter.Holder();
                holder.nickName = convertView.findViewById(R.id.nickname);
                holder.chainName = convertView.findViewById(R.id.blockChain);
                holder.radioButton = convertView.findViewById(R.id.radioBtn);
                convertView.setTag(holder);
            }else{
                holder = (DidListAdapter.Holder) convertView.getTag();
            }
            if(mSelectedRadioPosition == i){
                holder.radioButton.setChecked(true);
            }else{
                holder.radioButton.setChecked(false);
            }
            holder.nickName.setText(data.getNickName());
            holder.chainName.setText(data.getBlackChain().toString());
            holder.radioButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    PrintLog.e("index = " + i);
                    if(mSelectedRadioPosition == i){
                        return;
                    }
                    mSelectedRadioPosition = i;
                    if(mLastSelectedRadioButton != null){
                        mLastSelectedRadioButton.setChecked(false);
                    }
                    mLastSelectedRadioButton = (RadioButton) v;
                    notifyDataSetChanged();
                    PrintLog.e("mSelectedRadioPosition = " + mSelectedRadioPosition);

                }
            });
            return convertView;
        }

        /**
         * holder
         */
        class Holder{
            TextView nickName;
            TextView chainName;
            RadioButton radioButton;
        }
    }

    /**
     * fingerBioFactory
     *
     * @param ctx context
     */
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
                if(messageId == null){
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            selectDID();
                        }
                    }, 200);
                }else{
                    sendDid();
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                if(messageId == null){
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            selectDID();
                        }
                    }, 200);
                }else{
                    sendDid();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * sendDid
     */
    private void sendDid(){
        int index = adapter.getSelectIndex();
        did = didList.get(index).getDid();
        PrintLog.e("Did = " + did);
        SendDIDVo sendDid = new SendDIDVo(did, messageId);
        PreVCAPI preVc = PreVCAPI.getInstance();
        runOnUiThread(() -> layout.progresslayout.setVisibility(View.VISIBLE));
        Call<ResponseBody> rtn = preVc.preVCAPIInfo.sendDID(requestUrl, sendDid);
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(response.code() == 200){
                    if(response.body() != null){
                        try{
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                            DidvoResponse didRespionse = IITPApplication.gson.fromJson(responseJson, DidvoResponse.class);
                            PrintLog.e("result = " + didRespionse.getResult());
                            if(didRespionse.getResult()){
                                PrintLog.e("getVc = " + didRespionse.getMsgid().getVc());
                                PrintLog.e("getOffer = " + didRespionse.getMsgid().getOffer());
                                String vc = null;
                                DidResponseDataVo datavo = didRespionse.getMsgid();
                                vc = datavo.getVc();
                                if(vc != null){
                                    layout.progresslayout.setVisibility(View.INVISIBLE);
                                    saveVC(vc);
                                }else{
                                    getIndyOffer(datavo);
                                }
                            }else{
                                ToastUtils.custom(Toast.makeText(DidSelectWebActivity.this, didRespionse.getMsg(), Toast.LENGTH_SHORT)).show();
                            }

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
                ToastUtils.custom(Toast.makeText(DidSelectWebActivity.this, "serverError", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * saveVC
     *
     * @param vc vc
     */
    private void saveVC(String vc){
        PrintLog.e("VC = " + vc);
        CommonPreference.getInstance(DidSelectWebActivity.this).getSecureSharedPreferences().edit().putString(OfficeCredential, vc).apply();
        deleteIndyVC();
        sendPush("사원증 발급 완료", vc);
        finish();
    }

    /**
     * sendPush
     *
     * @param text text
     * @param vc   vc
     */
    private void sendPush(String text, String vc){
        ArrayList<IndyCredentialVo> indyCredentialVos;
        String indyData;
        String jwt = null;
        if(vc != null){
            PrintLog.e("VC = " + vc);
            MetadiumVerifier verifierTemp = new MetadiumVerifier();
            String payload;
            payload = preferenceUtil.getPayload(vc);
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("sendPush error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            String issuer = credential.getIssuer().toString();
            List<String> listTypes = new ArrayList<>(credential.getTypes());
            PrintLog.e("type = " + listTypes.get(1));
            String credentialType = listTypes.get(1);

            PrintLog.e("issuer = " + issuer);
            Map<String, String> claims = (Map<String, String>) credential.getCredentialSubject();
            IndyCredentialVo temp = new IndyCredentialVo("", issuer, "", "", "", claims);
            jwt = IITPApplication.gson.toJson(temp);
        }else{
            try{
                indyData = indy.getCredentialsWorksForEmptyFilter();
                PrintLog.e("indyData = " + indyData);
                indyCredentialVos = IITPApplication.gson.fromJson(indyData, new TypeToken<ArrayList<IndyCredentialVo>>(){
                }.getType());
                int size = indyCredentialVos.size();
                PrintLog.e("size = " + size);
                for(IndyCredentialVo temp : indyCredentialVos){
                    PrintLog.e("iss = " + temp.getSchema_id());
                    if(temp.getSchema_id().contains("company")){
                        jwt = IITPApplication.gson.toJson(temp);
                    }
                }

            }catch(Exception e){
                PrintLog.e("sendPush error");
            }
        }
        final Intent intent = new Intent(DidSelectWebActivity.this.getApplicationContext(), CredentialDetailUniActivity.class);
        PrintLog.e("jwt = " + jwt);
        intent.putExtra(JWT_DATA, jwt);
        intent.putExtra(DID, CompanyType);
        intent.putExtra("indyVc", true);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 11, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "Channel ID";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("SSI-PUSH")
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelName = "Channel Name";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(11, notificationBuilder.build());
    }

    /**
     * @param data
     */
    private void getIndyOffer(DidResponseDataVo data){
        indy = Indy.getInstance(DidSelectWebActivity.this);
        CommonPreference.getInstance(DidSelectWebActivity.this).getSecureSharedPreferences().edit().remove(OfficeCredential).apply();
        String json = IITPApplication.gson.toJson(data);
        DidResponseDataVo offerData = IITPApplication.gson.fromJson(json, DidResponseDataVo.class);
        if(offerData.getResult()){
            String indyDID = preferenceUtil.getIndyDID();
            String credentialRequest = indy.createCredentialRequest(indyDID, offerData.getCredid(), offerData.getOffer(), offerData.getSchemaid());
            PrintLog.e("credentialRequest = " + credentialRequest);
            requestZKPSetCredentialRequest(credentialRequest, offerData);
        }
//        else{
//            sendResponseVp(offerData.getResult(), offerData.getExist());
//        }
    }

    /**
     * requestZKPSetCredentialRequest
     * @param requestJson requestJson
     * @param offerData offerData
     */
    private void requestZKPSetCredentialRequest(String requestJson, DidResponseDataVo offerData){
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
                            String indydid = preferenceUtil.getIndyDID();
                            String responseJson = response.body().string();
                            PrintLog.e("response = " + responseJson);
                            CredentialRequestResponseVo vcData = IITPApplication.gson.fromJson(responseJson, CredentialRequestResponseVo.class);
                            PrintLog.e("vcData = " + vcData.getVc());
                            PrintLog.e("result = " + vcData.getResult());
                            savePreVCList(ZKP_IdentificationCredential, vcData.getVc());
                            String vc = getCredential(vcData.getVc());
                            PrintLog.e("vc = " + vc);
                            PrintLog.e("did = " + did);
                            deleteIndyVC();
                            String storeRequestId = indy.storeJobCredential(offerData.getId(), vc, indydid);
                            PrintLog.e("storeRequestId = " + storeRequestId);
                            String storeData = indy.getCredentialsWorksForEmptyFilter();
                            PrintLog.e("storeData = " + storeData);
                            indy.setRequestJobCredentialData(DidSelectWebActivity.this);
                            sendPush("사원증 발급 완료", null);
                            layout.progresslayout.setVisibility(View.INVISIBLE);
                            finish();
//                            sendResponseVp(offerData.getResult(), offerData.getExist());
                        }catch(Exception e){
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
                ToastUtils.custom(Toast.makeText(DidSelectWebActivity.this, "IDCredential Server error", Toast.LENGTH_SHORT)).show();
            }
        });
    }

    /**
     * getCredential
     * @param vc vc
     * @return credentialData
     */
    private String getCredential(String vc){
        String credentialData = null;
        String temp = preferenceUtil.getPayload(vc);
        ZkpClaimVo zkpdagta = IITPApplication.gson.fromJson(temp, ZkpClaimVo.class);
        String ddd = zkpdagta.getClaim();
        credentialData = new String(android.util.Base64.decode(ddd, 0));
        return credentialData;
    }

    /**
     * savePreVCList
     * @param key key
     * @param jwt jwt
     */
    private void savePreVCList(String key, String jwt){
        PrintLog.e("VC = " + jwt);
        PrintLog.e("save Key = " + key);
        if(jwt != null){
            CommonPreference.getInstance(DidSelectWebActivity.this).getSecureSharedPreferences().edit().putString(key, jwt).apply();
        }
    }

    /**
     * deleteIndyVC
     */
    private void deleteIndyVC(){
        Indy indy = Indy.getInstance(DidSelectWebActivity.this);
        ArrayList<IndyCredentialVo> indyCredentialVos;
        String indyData = null;
        try{
            indyData = indy.getCredentialsWorksForEmptyFilter();
        }catch(Exception e){
            PrintLog.e("deleteIndyVC error");
        }
//        PrintLog.e("indyData = " + indyData);
        indyCredentialVos = IITPApplication.gson.fromJson(indyData, new TypeToken<ArrayList<IndyCredentialVo>>(){
        }.getType());
        int size = indyCredentialVos.size();
        PrintLog.e("size = " + size);
        if(size != 0){
            for(IndyCredentialVo temp : indyCredentialVos){
                PrintLog.e("iss = " + temp.getSchema_id());
                if(temp.getSchema_id().contains("company")){
                    indy.deleteCredential(temp.getReferent());
                }
            }
        }
    }

//    private void savevc(JobDataVo data){
//        String platform = data.getPlatform();
//        String vc = null;
//        if(platform.equals("meta") || platform.equals("icon")){
//            vc = data.getVc();
//            PrintLog.e("VC = " + vc);
//            CommonPreference.getInstance(DidSelectWebActivity.this).getSecureSharedPreferences().edit().putString(OfficeCredential, vc).apply();
//            deleteIndyVC();
//            sendPush("사원증 발급 완료", vc);
//        }else{
//            CommonPreference.getInstance(DidSelectWebActivity.this).getSecureSharedPreferences().edit().remove(OfficeCredential).apply();
//            String json = IITPApplication.gson.toJson(data);
//            ZkpResponse offerData = IITPApplication.gson.fromJson(json, ZkpResponse.class);
//            if(offerData.getResult()){
//                String indyDID = preferenceUtil.getIndyDID();
//                String credentialRequest = indy.createCredentialRequest(indyDID, offerData.getCredid(), offerData.getOffer(), offerData.getSchemaid());
//                PrintLog.e("credentialRequest = " + credentialRequest);
//                requestZKPSetCredentialRequest(credentialRequest, offerData);
//            }else{
//                sendResponseVp(offerData.getResult(), offerData.getExist());
//            }
//
//        }
//
//    }
}
