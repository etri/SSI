package com.iitp.iitp_demo.activity.request;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.icu.text.NumberFormat;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.internal.$Gson$Preconditions;
import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.KeyManager;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.PincodeActivity;
import com.iitp.iitp_demo.activity.credential.CredentialDetailActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
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
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.databinding.ActivityProductRequestBinding;
import com.iitp.iitp_demo.databinding.ActivityVpRequestBinding;
import com.iitp.iitp_demo.util.BiometricDialog;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.util.ECKeyUtils;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.metadium.did.MetadiumWallet;
import com.metadium.did.crypto.MetadiumKey;
import com.metadium.did.exception.DidException;
import com.metadium.did.protocol.MetaDelegator;
import com.nimbusds.jwt.JWTClaimsSet;

import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.interfaces.ECPrivateKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.iitp.iitp_demo.Constants.DID;
import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.Constants.JWT_DATA;
import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreDelegationToken;

public class RequestProductVCListActivity extends BaseActivity{
    //    private List<RequestCredentialData> listData = new ArrayList<>();
    private ActivityProductRequestBinding layout;
    private ArrayList<ProductVC> productVC = new ArrayList<>();
    private CredentialListAdapter adapter = new CredentialListAdapter();
    private String presentationRequestId = null;
    private String sellDID = null;
    private String buyDID = null;
    private String sign = null;
    private String productDID = null;
    private String publicKey = null;
    private String price = null;
    private String date = null;
    private PreferenceUtil preferenceUtil;
    private MutableLiveData<String> vcData;
    private List<String> vcList = new ArrayList<>();
    private MetaDelegator delegator;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;
    private boolean checkBio;

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
                    PrintLog.e("RequestProductVCListActivity error");
                }

                Log.e("TEST", "uri : " + uriDecode);
                Set<String> queryList = uri.getQueryParameterNames();
                productDID = uri.getQueryParameter("productDID");
                sign = uri.getQueryParameter("sign");
                buyDID = uri.getQueryParameter("buyer_id");
                sellDID = uri.getQueryParameter("seller_id");
                publicKey = uri.getQueryParameter("publicKey");
                price = uri.getQueryParameter("price");
                date = uri.getQueryParameter("date");

                PrintLog.e("productDID = " + productDID);
                PrintLog.e("sign = " + sign);
                PrintLog.e("buyDID = " + buyDID);
                PrintLog.e("publicKey = " + publicKey);
                PrintLog.e("price = " + price);
                PrintLog.e("date = " + date);
            }
        }
        layout = DataBindingUtil.setContentView(this, R.layout.activity_product_request);
        setSupportActionBar(layout.toolbar.appbar);
        delegator = new MetaDelegator("https://testdelegator.metadium.com", "https://api.metadium.com/dev");
        setActionBarSet(layout.toolbar, "물품보증서/거래증명서 발급", false);
        layout.sendBtn.setVisibility(View.VISIBLE);
        productVC = preferenceUtil.getProductVC();
        if(productVC == null){
            productVC = new ArrayList<>();
        }
        PrintLog.e("size = " + productVC.size());

        layout.sendBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int index = adapter.getSelectIndex();
                PrintLog.e("index = " + index);
                makeTxData(productVC.get(index));
//                pinCode();
            }
        });

        layout.didList.setAdapter(adapter);
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
            if(BiometricUtils.isFingerPrint(this)){
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

        List<String> vcList = getVcList(delegated_attributes, requestAttributes);
        return vcList;
    }

    /**
     * get meta did
     *
     * @return did
     */
    private String getDID(){
        String did = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(RequestProductVCListActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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
        String keyid = keyManager.getManagementKeyId(RequestProductVCListActivity.this);
        ECPrivateKey privatKey = ECKeyUtils.toECPrivateKey(keyManager.getPrivate(RequestProductVCListActivity.this), "secp256k1"); // PrivateKey load

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
            String vc = CommonPreference.getInstance(RequestProductVCListActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreDelegationToken, null);
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
                        String vc = CommonPreference.getInstance(RequestProductVCListActivity.this).getSecureSharedPreferences().getString(preKey, null);
                        PrintLog.e("vc data = " + vc);
                        vclist.add(vc);
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
//                String vp = makeVpData(getDID(), vcList);
//         vp = noneZKPMakeVP(requestVP);
//                String response = makeRequestVpResponse(vp);
//                requestCredential(response_url, response);
            }

            if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        //TO-DO
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


    private void startPinCodeActivity(){
        Intent intent;
        intent = new Intent(RequestProductVCListActivity.this, PincodeActivity.class);
        intent.putExtra(PIN_SETTING_TYPE, 2);
        startActivityForResult(intent, 1000);
    }

    class CredentialListAdapter extends BaseAdapter{
        private int mSelectedRadioPosition;
        private RadioButton mLastSelectedRadioButton;

        CredentialListAdapter(){

        }

        @Override
        public int getCount(){
            return productVC.size();
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
            CredentialListAdapter.Holder holder;
            ProductVC data = productVC.get(i);
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_product, parent, false);
                holder = new CredentialListAdapter.Holder();
                holder.productId = convertView.findViewById(R.id.productNameTv);
                holder.sn = convertView.findViewById(R.id.snTv);
                holder.buyDate = convertView.findViewById(R.id.dateTv);
                holder.price = convertView.findViewById(R.id.priceTv);
                holder.buyer_id = convertView.findViewById(R.id.buyerDIDTv);
                holder.seller_id = convertView.findViewById(R.id.sellerDIDTv);
                holder.layout = convertView.findViewById(R.id.layout);
                holder.radioButton = convertView.findViewById(R.id.radioBtn);
                holder.did1 = convertView.findViewById(R.id.did1text);
                holder.did2 = convertView.findViewById(R.id.did2text);
                holder.productNameText = convertView.findViewById(R.id.productNameText);
                holder.publicKeyTv = convertView.findViewById(R.id.publicKeyTv);
                holder.sellDate = convertView.findViewById(R.id.sellDateTv);
                convertView.setTag(holder);
            }else{
                holder = (CredentialListAdapter.Holder) convertView.getTag();
            }
            if(mSelectedRadioPosition == i){
                holder.radioButton.setChecked(true);
            }else{
                holder.radioButton.setChecked(false);
            }
            Map<String, Object> productMap = getVCData(data.getProductVC());
            Map<String, Object> ProofMap = getVCData(data.getProductProofVC());
            String issuer = getIssuer(data.getProductVC());
            holder.did1.setText(issuer);
            String wallerJson = data.getWalletJson();
            MetadiumWallet sellerwallet = null;
            try{
                sellerwallet = MetadiumWallet.fromJson(wallerJson);
            }catch(ParseException e){
                PrintLog.e("getView error");
            }
            String publicKey = Numeric.toHexStringWithPrefix(sellerwallet.getKey().getPublicKey());
            holder.publicKeyTv.setText(publicKey);
            for(String key1 : productMap.keySet()){
                PrintLog.e(key1 + " : " + productMap.get(key1).toString());
                if(key1.equals("production_date")){
                    holder.buyDate.setText(productMap.get(key1).toString());
                }else if(key1.equals("id")){
                    String id = productMap.get(key1).toString();
                    holder.productId.setText(id);
                    holder.did2.setText(id);
                }else if(key1.equals("SN")){
                    holder.sn.setText((String) productMap.get(key1).toString());
                }else if(key1.equals("name")){
                    holder.productNameText.setText((String) productMap.get(key1).toString());
                }
            }
            for(String key2 : ProofMap.keySet()){
                PrintLog.e(key2 + " : " + ProofMap.get(key2).toString());
                if(key2.equals("price")){
                    String price = ProofMap.get(key2).toString();
                    int priceInt = Integer.parseInt(price);
                    String formattedStringPrice = NumberFormat.getInstance(Locale.getDefault()).format(priceInt) + "원";
                    holder.price.setText(formattedStringPrice);
                }else if(key2.equals("seller_id")){
                    holder.seller_id.setText(ProofMap.get(key2).toString());
                }else if(key2.equals("buyer_id")){
                    holder.buyer_id.setText(ProofMap.get(key2).toString());
                }else if(key2.equals("sell_date")){
                    holder.sellDate.setText(ProofMap.get(key2).toString());
                }
            }
            holder.layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    PrintLog.e("click1");
//                    makeTxData(data);
//                    Intent intent = new Intent(VCListActivity.this, CredentialDetailActivity.class);
//                    intent.putExtra(JWT_DATA,vcList.get(i));

//                    startActivity(intent);
                }
            });
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
            RelativeLayout layout;
            TextView productId;
            TextView sn;
            TextView buyDate;
            TextView price;
            TextView buyer_id;
            TextView seller_id;
            TextView did1;
            TextView did2;
            TextView productNameText;
            TextView publicKeyTv;
            TextView sellDate;
            RadioButton radioButton;
        }
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
//                        String vp = makeVpData(getDID(), vcList);
//                        String response = makeRequestVpResponse(vp);
//                        requestCredential(response_url, response);
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
        BiometricDialog biometricDialog = new BiometricDialog(RequestProductVCListActivity.this,
                getString(R.string.did_fingerprint_title),
                getString(R.string.did_fingerprint_desc),
                new BiometricDialog.OnAuthenticationListener(){
                    @Override
                    public void onSuccess(FingerprintManager.AuthenticationResult result){
                        BiometricUtils.enableFingerPrint(RequestProductVCListActivity.this);
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run(){
//                                String vp = makeVpData(getDID(), vcList);
////         vp = noneZKPMakeVP(requestVP);
//                                String response = makeRequestVpResponse(vp);
//                                requestCredential(response_url, response);
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

    private Map<String, Object> getVCData(String jwt){
        MetadiumVerifier verifierTemp = new MetadiumVerifier();

        String payload = preferenceUtil.getPayload(jwt);
        JWTClaimsSet jwtClaimsSet = null;
        try{
            jwtClaimsSet = JWTClaimsSet.parse(payload);
        }catch(ParseException e){
            PrintLog.e("getVCData error");
        }
        VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
        return claims;
    }

    private String getIssuer(String jwt){
        MetadiumVerifier verifierTemp = new MetadiumVerifier();

        String payload = preferenceUtil.getPayload(jwt);
        JWTClaimsSet jwtClaimsSet = null;
        try{
            jwtClaimsSet = JWTClaimsSet.parse(payload);
        }catch(ParseException e){
            PrintLog.e("getIssuer error");
        }
        VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
        return credential.getIssuer().toString();
    }

    private Map<String, Object> getClaim(String vc){
        String payload = preferenceUtil.getPayload(vc);
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        JWTClaimsSet jwtClaimsSet = null;
        try{
            jwtClaimsSet = JWTClaimsSet.parse(payload);
        }catch(ParseException e){
            PrintLog.e("getClaim error");
        }
        VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
        PrintLog.e("did = " + credential.toJSONString());
        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
        return claims;
    }

    private void makeTxData(ProductVC datavc){

        ArrayList<DidDataVo> didList = MainActivity.getDidList(RequestProductVCListActivity.this);
        DidDataVo didData = null;
        String metadid = preferenceUtil.getMetaDID();

        for(DidDataVo data : didList){
            if(data.getDid().equals(metadid)){
                didData = data;
                PrintLog.e("did = " + didData.getDid());
            }
        }
        PrintLog.e("meta id = " + metadid);
        PrintLog.e("proof vc = " + datavc.getProductProofVC());
        PrintLog.e("product vc = " + datavc.getProductVC());
        String walletJson = preferenceUtil.getWalletJson(productDID);
        DidDataVo finalDidData = didData;
        VCVPCreater vcvpCreater = VCVPCreater.getInstance();
        vcData = new MutableLiveData<>();
        vcData.observe(this, s -> {
            PrintLog.e("vcdata = " + s);
            vcList.add(vcData.getValue());
            vcList.add(datavc.getProductVC());
            Intent intent = new Intent();
            String json = IITPApplication.gson.toJson(vcList);
            PrintLog.e("vc list = " + json);
            changeProductKey(datavc);
            intent.putExtra("vc", json);
            setResult(RESULT_OK, intent);
            finish();

        });

        new Thread(() -> {
            try{
                try{

                    String beforeProof = datavc.getProductProofVC();
                    String vc = null;
                    String temp = price.replace(",", "");
                    String[] dateTemp = date.split(" ");
                    long longPrice = Long.parseLong(temp);
                    if(finalDidData.getDid().contains("meta")){
                        vc = vcvpCreater.TxproofMeta(finalDidData, productDID, buyDID, BigInteger.valueOf(longPrice), delegator.currentBlockNumber(), beforeProof, dateTemp[0]);
                    }else if(finalDidData.getDid().contains("icon")){
                        vc = vcvpCreater.TxproofIcon(finalDidData, productDID, buyDID, BigInteger.valueOf(longPrice), delegator.currentBlockNumber(), beforeProof, dateTemp[0]);
                    }else if(finalDidData.getDid().contains("sov")){
                        vc = makeIndyVC(finalDidData, productDID, buyDID, BigInteger.valueOf(longPrice), delegator.currentBlockNumber(), beforeProof, dateTemp[0]);
                    }
                    PrintLog.e("ProofVC = " + vc);
                    vcData.postValue(vc);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }catch(Exception e){
                // error
                e.printStackTrace();
            }
        }).start();


    }

    private void changeProductKey(ProductVC productData){
        new Thread(() -> {

            try{
                MetadiumWallet sellerwallet = null;
                String sellerWalletJson = productData.getWalletJson();
                PrintLog.e("wallet json = " + sellerWalletJson);
                try{
                    sellerwallet = MetadiumWallet.fromJson(sellerWalletJson);
                }catch(ParseException e){
                    e.printStackTrace();
                }
                PrintLog.e("publicKey = " + publicKey);
                PrintLog.e("sign = " + sign);
                BigInteger bigPublicKey = Numeric.toBigInt(publicKey);
                sellerwallet.updateKeyOfDid(delegator, bigPublicKey, sign);
            }catch(DidException e){
                e.printStackTrace();
            }
        }).start();
    }

    private String makeIndyHeader(String did){

        IndyHeader header = new IndyHeader(did, "EdDSA");
        String json = IITPApplication.gson.toJson(header);
        PrintLog.e("json = " + json);
        return json;
    }

    private String makeIndyVC(DidDataVo did, String productDID, String buyerDid, BigInteger price, BigInteger blockNumber, String beforeSignedVC, String date){

        String jwt = null;
        String nonce = UUID.randomUUID().toString();
        String kid;
        PrintLog.e("did = " + did.getDid());
        if(!did.getDid().contains("sov")){
            kid = "did:sov:" + did.getDid() + "#key-1";
        }else{
            kid = did.getDid() + "#key-1";
        }
        String header = makeIndyHeader(kid);
        ArrayList<String> type = new ArrayList<String>();
        Map<String, String> claims = Stream.of(new String[][]{
                {"ProductCredential_id", productDID},
                {"seller_id", did.getDid()},
                {"buyer_id", buyerDid},
                {"BlockNumber", blockNumber.toString()},
                {"price", price.toString()},
                {"sell_date", date}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        // 이전 거래증명 VC를 claim 에 포함한다.
        if(beforeSignedVC != null){
            claims.put("OldProductProofCredential", beforeSignedVC);
        }
        type.add("VerifiableCredential");
        type.add("ProductProofCredential");

        long iat = System.currentTimeMillis() / 1000L;

        IndyPayload payload = new IndyPayload(did.getDid(), did.getDid(), claims, type, "1.0", nonce, iat);
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
            signmessage = signIndy(did.getDid(), signingInputString);
            PrintLog.e("signmessage = " + signmessage);
        }
        jwt = signingInputString + "." + signmessage;
        return jwt;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String signIndy(String did, String message){
        String jwt = null;
        Indy indy = Indy.getInstance(RequestProductVCListActivity.this);
        DidDataVo didData = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(RequestProductVCListActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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
}

