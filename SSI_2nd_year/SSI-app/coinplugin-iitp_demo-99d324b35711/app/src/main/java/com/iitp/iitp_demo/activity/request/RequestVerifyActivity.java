package com.iitp.iitp_demo.activity.request;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.iid.FirebaseInstanceId;
import com.iitp.iitp_demo.PublicKeyListener;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.chain.Icon;
import com.iitp.iitp_demo.databinding.ActivityVerifyBinding;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.VerifyJwt;
import com.iitp.verifiable.util.ECKeyUtils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.abi.datatypes.Int;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Set;

public class RequestVerifyActivity extends BaseActivity{

    private ActivityVerifyBinding layout;
    private PreferenceUtil preferenceUtil;
    private String did = null;
    private String sign = null;
    private String url = null;
    private String hash = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_verify);
        setSupportActionBar(layout.toolbar.appbar);
        setActionBarSet(layout.toolbar, "검증 중", false);
        preferenceUtil = PreferenceUtil.getInstance(this);
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
                Set<String> queryList = uri.getQueryParameterNames();
                if(queryList.size() == 3){
                    did = uri.getQueryParameter("did");
                    sign = uri.getQueryParameter("sign");
                    url = uri.getQueryParameter("url");
                    PrintLog.e("did = " + did);
                    PrintLog.e("sign = " + sign);
                    PrintLog.e("url = " + url);
                    checkVerify();
                }else if(queryList.size() == 1){
                    hash = uri.getQueryParameter("hash");
                    PrintLog.e("hash = " + hash);
                    checkHash();
                }else{
                    PrintLog.e("sise = 0");
                    String token = FirebaseInstanceId.getInstance().getToken();
                    PrintLog.e("token = " + token);
                    intent.putExtra("token", token);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }


    private void checkHash(){
        if(hash != null){
            new Thread(() -> {
                Icon icon = Icon.getInstance();
                boolean result = icon.checkHash(hash);
                PrintLog.e("result = " + result);
                Intent intent = new Intent();
                intent.putExtra("result", result);
                setResult(RESULT_OK, intent);
                finish();
            }).start();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkVerify(){
        VerifyJwt verifyJwt = new VerifyJwt();
        PublicKeyListener listener = new PublicKeyListener(){
            @Override
            public void requestComplete(String publicKeyHex){
                PrintLog.e("publicKeyHex = " + publicKeyHex);
                boolean result = true;
//                try{
//                    Security.removeProvider("BC");
//                    Security.insertProviderAt(new BouncyCastleProvider(), 1);
//                    Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", "BC");
////                    byte[] publicKeyDecode = android.util.Base64.decode(publicKeyHex, android.util.Base64.NO_WRAP);
////                    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyDecode);
////                    KeyFactory keyFactory = KeyFactory.getInstance("EC","BC");
////                    PublicKey publicKey = null;
////                    try{
////                        publicKey = keyFactory.generatePublic(publicKeySpec);
////                    }catch(InvalidKeySpecException e){
////                        e.printStackTrace();
////                    }
//                    byte[] publicKeyDecode = android.util.Base64.decode(publicKeyHex, android.util.Base64.NO_WRAP);
//                    PublicKey publicKey = (PublicKey)ECKeyUtils.toECPublicKey(publicKeyDecode, "secp256k1");
//                    ecdsaVerify.initVerify(publicKey);
//                    ecdsaVerify.update(url.getBytes("UTF-8"));
////                    byte[] signDecode = android.util.Base64.decode(sign, Base64.URL_SAFE);
//                    PrintLog.e("a = "+sign);
//                    PrintLog.e("b = "+sign.replace(" ",""));
//                    byte[] signDecode = Base64.getDecoder().decode(sign);
//                    result = ecdsaVerify.verify(signDecode);
//                    PrintLog.e("result = " + result);
//                }catch(NoSuchAlgorithmException | UnsupportedEncodingException | SignatureException | InvalidKeyException | NoSuchProviderException e){
//                    e.printStackTrace();
//                }
                Intent intent = new Intent();
                intent.putExtra("result", result);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void error(String error){
                PrintLog.e("error = " + error);
            }
        };
        verifyJwt.getPublicKey(listener, did, "sampleKey1");


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
}

