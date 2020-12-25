package com.iitp.iitp_demo.activity.request;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;

import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.PincodeActivity;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.chain.VCVPVerifier;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.databinding.ActivityDidsSelectBinding;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.OnSingleClickListener;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.metadium.did.protocol.MetaDelegator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;

public class DidSelectStockActivity extends BaseActivity{

    private ActivityDidsSelectBinding layout;
    private int index = 0;
    private ArrayList<DidDataVo> didList = new ArrayList<DidDataVo>();
    private DidListAdapter adapter;
    private PreferenceUtil preferenceUtil;
    private MetaDelegator delegator;
    private String hash = null;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;
    private boolean checkBio;
    Indy indy = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_dids_select);
        setSupportActionBar(layout.toolbar.appbar);
        setActionBarSet(layout.toolbar, getString(R.string.did_select_title), false);
        preferenceUtil = PreferenceUtil.getInstance(this);
        checkBio = false;
        Intent intent = getIntent();
        Indy indy = Indy.getInstance(DidSelectStockActivity.this);
        if(intent != null){
            Uri uri = getIntent().getData();
            if(uri != null){
                String uriDecode = null;
                try{
                    uriDecode = URLDecoder.decode(uri.toString(), "UTF-8");
                }catch(UnsupportedEncodingException e){
                    PrintLog.e("DidSelectActivity error");
                }
                Log.e("TEST", "uri : " + uriDecode);
//                Set<String> queryList = uri.getQueryParameterNames();
                hash = uri.getQueryParameter("hash");
                PrintLog.e("hash = " + hash);
            }
        }
        init();
    }

    @Override
    protected void onResume(){
        super.onResume();
        checkBio = false;
    }

    private void init(){
        didList = MainActivity.getDidList(DidSelectStockActivity.this);
        adapter = new DidListAdapter();
        layout.didList.setAdapter(adapter);
        layout.selectBtn.setOnClickListener(new OnSingleClickListener(){
            @Override
            public void onSingleClick(View v){
                BiometricUtils.hasBiometricEnrolled(DidSelectStockActivity.this);
                if((BiometricUtils.getPincode(DidSelectStockActivity.this) == null && !(BiometricUtils.isFingerPrint(DidSelectStockActivity.this)))){
                    if(BiometricUtils.checkFingerprint(DidSelectStockActivity.this)){
                        fingerBioFactory(DidSelectStockActivity.this);
                    }else{
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                startPincodeActivity();
                            }
                        }, 1000);
                    }
                }else{
                    if(BiometricUtils.isFingerPrint(DidSelectStockActivity.this)){
                        fingerBioFactory(DidSelectStockActivity.this);
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

    private void selectDID(){
        PrintLog.e("selectDID");

        layout.progresslayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent();
        int index = adapter.getSelectIndex();
        DidDataVo did = didList.get(index);
        VCVPCreater vcvpCreater = VCVPCreater.getInstance();
        VCVPVerifier vcvpVerifier = VCVPVerifier.getInstance();
        PrintLog.e("did= " + did.getDid());
        PrintLog.e("getPrivateKey= " + did.getPrivateKey());
        PrintLog.e("getPublicKey= " + did.getPublicKey());
        if(did.getDid().contains("meta")){
            PrintLog.e("meta");
            delegator = new MetaDelegator("https://testdelegator.metadium.com", "https://testdelegator.metadium.com");
            new Thread(() -> {
                String vc = vcvpCreater.vcCreateConfirmMeta(DidSelectStockActivity.this, did, hash);
//                VerifiableCredential credential = vcvpVerifier.verifyVC(vc);
//                PrintLog.e("credential meta issuer = " + credential.getIssuer());
                intent.putExtra("vc", vc);
                setResult(RESULT_OK, intent);
                finish();
            }).start();
        }else if(did.getDid().contains("icon")){
            new Thread(() -> {
                String vc = vcvpCreater.vcCreateConfirmIcon(did, hash);
//                VerifiableCredential credential = vcvpVerifier.verifyVC(vc);
//                PrintLog.e("credential icon issuer = " + credential.getIssuer());
                intent.putExtra("vc", vc);
                setResult(RESULT_OK, intent);
                finish();
            }).start();
        }else if(did.getDid().contains("sov")){
            String vc = makeIndyVC(did.getDid());
            PrintLog.e("vc = " + vc);
            intent.putExtra("vc", vc);
            setResult(RESULT_OK, intent);
            finish();
        }else{
            ToastUtils.custom(Toast.makeText(DidSelectStockActivity.this, "지원하지 않는 DID 입니다.  ", Toast.LENGTH_SHORT)).show();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private String makeIndyHeader(String did){

        IndyHeader header = new IndyHeader(did, "EdDSA");
        String json = IITPApplication.gson.toJson(header);
        PrintLog.e("json = " + json);
        return json;
    }

    private String makeIndyVC(String did){

        String jwt = null;
        String nonce = UUID.randomUUID().toString();
        String kid;
        PrintLog.e("did = " + did);
        if(!did.contains("sov")){
            kid = "did:sov:" + did + "#key-1";
        }else{
            kid = did + "#key-1";
        }
        String header = makeIndyHeader(kid);
        ArrayList<String> type = new ArrayList<String>();
        Map<String, Object> claim = new HashMap<>();
        type.add("VerifiableCredential");
        type.add("StockReportCredential");
        claim.put("id", did);
        claim.put("hash", hash);
        long iat = System.currentTimeMillis() / 1000L;

        IndyPayload payload = new IndyPayload(did, did, claim, type, "1.0", nonce, iat);
        String payloadData = IITPApplication.gson.toJson(payload);
        PrintLog.e("payloadData = " + payloadData);
        String header64 = new String(Base64.encode(header.getBytes(), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
        String payload64 = new String(Base64.encode(payloadData.getBytes(), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
        PrintLog.e("header64 = " + header64);
        PrintLog.e("payload64 = " + payload64);
        String signingInputString = header64 + '.' + payload64;
        PrintLog.e("signingInputString = " + signingInputString);
        String signmessage = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            signmessage = signIndy(did, signingInputString);
            PrintLog.e("signmessage = " + signmessage);
        }
        jwt = signingInputString + "." + signmessage;
//        verifyIndyJwt(jwt);
        return jwt;
    }

    private boolean verifyIndyJwt(String vc){
        indy = Indy.getInstance(DidSelectStockActivity.this);
        String[] vcTemp = vc.split("\\.");
        String message = vcTemp[0] + "." + vcTemp[1];
        String header = new String(Base64.decode(vcTemp[0], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
        PrintLog.e("header = " + header);
        byte[] signature2 = Base64.decode(vcTemp[2], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
        String base58 = "7SDfFxBTQphXhHLbWw2iU4gnHHGsGGzZrgFpXV9RQFdc";
        boolean rtn = indy.indyVerifyMessage(base58, message.getBytes(), signature2);
        PrintLog.e("rtn = " + rtn);
        return rtn;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private String signIndy(String did, String message){
        String jwt = null;
        indy = Indy.getInstance(DidSelectStockActivity.this);
        DidDataVo didData = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(DidSelectStockActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
        if(json != null){
            didList = IITPApplication.gson.fromJson(json, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
        }
        for(DidDataVo data : didList){
            if(data.getDid().equals(did)){
                didData = data;
                break;
            }
        }
        String signMessage = null;
        if(didData != null){
            PrintLog.e("PublicKey() : " + didData.getPublicKey());
            PrintLog.e("DID : " + didData.getDid());
            byte[] signature = indy.indySignMessage(didData.getPublicKey(), message.getBytes());
            signMessage = new String(Base64.encode(signature, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
            PrintLog.e("signMessage = " + signMessage);
        }


//        jwt = message+"."+signMessage;
//        PrintLog.e("jwt = "+jwt);
//        byte[] signature2 = Base64.decode(signMessage, Base64.URL_SAFE | Base64.NO_PADDING);
//        String base58  = "F1uox2dzp7UkPGbG2GW994V2kxVo8EtRLXTE6FZ3vVE2";
//        PrintLog.e("PublicKey() : " + didData.getPublicKey());
//        boolean rtn = indy.indyVerifyMessage(base58, message.getBytes(), signature2);
//        PrintLog.e("rtn() : " + rtn);
        return signMessage;
    }


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
                        runOnUiThread(() -> layout.progresslayout.setVisibility(View.VISIBLE));
                        selectDID();
                    }
                }, 200);
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
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        runOnUiThread(() -> layout.progresslayout.setVisibility(View.VISIBLE));
                        selectDID();
                    }
                }, 200);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

