package com.iitp.iitp_demo.activity.request;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.PincodeActivity;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.databinding.ActivityDidsSelectBinding;
import com.iitp.iitp_demo.util.BiometricDialog;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.OnSingleClickListener;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.metadium.did.MetadiumWallet;
import com.metadium.did.crypto.MetadiumKey;
import com.metadium.did.exception.DidException;
import com.metadium.did.protocol.MetaDelegator;

import org.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;

import static com.iitp.iitp_demo.Constants.DID;
import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;

public class DidSelectActivity extends BaseActivity{

    private ActivityDidsSelectBinding layout;
    private int index = 0;
    private ArrayList<DidDataVo> didList = new ArrayList<DidDataVo>();
    private DidListAdapter adapter;
    private PreferenceUtil preferenceUtil;
    private String productDID = null;
    private String walletJson;
    private MetaDelegator delegator;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;
    private boolean checkBio;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_dids_select);
        setSupportActionBar(layout.toolbar.appbar);
        setActionBarSet(layout.toolbar, getString(R.string.did_select_title), false);
        preferenceUtil = PreferenceUtil.getInstance(this);
        checkBio = false;
        Intent intent = getIntent();
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
                productDID = uri.getQueryParameter("productDID");
                PrintLog.e("productDID = " + productDID);
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
        didList = MainActivity.getDidList(DidSelectActivity.this);
        adapter = new DidListAdapter();
        layout.didList.setAdapter(adapter);
        layout.selectBtn.setOnClickListener(new OnSingleClickListener(){
            @Override
            public void onSingleClick(View v){
                BiometricUtils.hasBiometricEnrolled(DidSelectActivity.this);
                if((BiometricUtils.getPincode(DidSelectActivity.this) == null && !(BiometricUtils.isFingerPrint(DidSelectActivity.this)))){
                    if(BiometricUtils.checkFingerprint(DidSelectActivity.this)){
                        fingerBioFactory(DidSelectActivity.this);
                    }else{
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                startPincodeActivity();
                            }
                        }, 1000);
                    }
                }else{
                    if(BiometricUtils.isFingerPrint(DidSelectActivity.this)){
                        fingerBioFactory(DidSelectActivity.this);
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
        Intent intent = new Intent();
        int index = adapter.getSelectIndex();
        String did = didList.get(index).getDid();

        if(did.contains("meta")||did.contains("icon")||did.contains("sov")){
            delegator = new MetaDelegator("https://testdelegator.metadium.com", "https://testdelegator.metadium.com");
            if(productDID != null){
                if(productDID.length() != 0){
                    new Thread(() -> {
                        runOnUiThread(() -> layout.progresslayout.setVisibility(View.VISIBLE));
                        BigInteger publicKey = null;
                        String signature = null;
                        MetadiumKey newKey;
                        try{
                            PrintLog.e("productDID= " + productDID);
//                        MetadiumWallet wallet = MetadiumWallet.createDid(delegator);
                            newKey = new MetadiumKey(); // Getting key
                            MetadiumWallet wallet = new MetadiumWallet(productDID, newKey);
                            signature = delegator.signAddAssocatedKeyDelegate(productDID, newKey);
                            walletJson = wallet.toJson();
                            PrintLog.e("wallet json = " + walletJson);
                            publicKey = newKey.getPublicKey();
                            PrintLog.e("wallet did = " + wallet.getDid());
                        }catch(DidException | InvalidAlgorithmParameterException e){
                            e.printStackTrace();
                        }
                        String publicKeyString = Numeric.toHexStringWithPrefix(publicKey);
                        PrintLog.e("did= " + did);
                        PrintLog.e("productDID= " + productDID);
                        PrintLog.e("publicKey= " + publicKeyString);
                        PrintLog.e("signature= " + signature);
                        intent.putExtra("did", did);
                        intent.putExtra("productdid", productDID);
                        intent.putExtra("publicKey", publicKeyString);
                        intent.putExtra("sign", signature);
                        preferenceUtil.setMetaDID(did);
                        preferenceUtil.setWalletJson(productDID, walletJson);
                        runOnUiThread(() -> layout.progresslayout.setVisibility(View.GONE));
                        setResult(RESULT_OK, intent);
                        finish();
                    }).start();
                }else{
                    ToastUtils.custom(Toast.makeText(DidSelectActivity.this, "Product DID 가 없습니다. ", Toast.LENGTH_SHORT)).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }

            }else{
                String publicKey = didList.get(index).getPublicKey();
                intent.putExtra("publicKey", publicKey);
                intent.putExtra("sign", "");
                intent.putExtra(DID, did);
                preferenceUtil.setMetaDID(did);
                setResult(RESULT_OK, intent);
                finish();
            }
        }else{
            ToastUtils.custom(Toast.makeText(DidSelectActivity.this, "지원하지 않는 DID 입니다.  ", Toast.LENGTH_SHORT)).show();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private void setProgress(){

    }

    private void startPincodeActivity(){
        Intent intent;
        intent = new Intent(this, PincodeActivity.class);
        intent.putExtra(PIN_SETTING_TYPE, 2);
        startActivityForResult(intent, REQUEST_CODE_VERIFY_PIN_APP_LOCK);
        //finish();
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

    private void fingerPrintFactory(boolean on){
        BiometricDialog biometricDialog = new BiometricDialog(DidSelectActivity.this,
                getString(R.string.did_fingerprint_title),
                getString(R.string.did_fingerprint_desc),
                new BiometricDialog.OnAuthenticationListener(){
                    @Override
                    public void onSuccess(FingerprintManager.AuthenticationResult result){

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
                }, 1
        );
        biometricDialog.setCancelable(false);
        biometricDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        selectDID();
                    }
                }, 200);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

