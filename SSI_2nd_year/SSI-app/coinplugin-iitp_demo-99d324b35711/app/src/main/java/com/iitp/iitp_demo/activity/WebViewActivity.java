package com.iitp.iitp_demo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;

import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.KeyManager;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DataVo;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.activity.model.MsgidVo;
import com.iitp.iitp_demo.activity.model.RequestDataVo;
import com.iitp.iitp_demo.activity.model.RequestWebViewInterfaceVo;
import com.iitp.iitp_demo.activity.model.VpRequestDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestDataVoList;
import com.iitp.iitp_demo.activity.model.VpRequestNoneZKPDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPDelegateDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPReferentDataVo;
import com.iitp.iitp_demo.activity.model.VpResponseVo;
import com.iitp.iitp_demo.activity.request.DidSelectWebActivity;
import com.iitp.iitp_demo.activity.request.RequestWebVpActivity;
import com.iitp.iitp_demo.api.MarketAPI;
import com.iitp.iitp_demo.api.model.ChallengeVo;
import com.iitp.iitp_demo.api.model.WebResultVo;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.databinding.ActivityAppWebviewBinding;
import com.iitp.iitp_demo.util.BiometricDialog;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.iitp_demo.util.ViewUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.util.ECKeyUtils;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import foundation.icon.did.core.Algorithm;
import foundation.icon.did.core.AlgorithmProvider;
import foundation.icon.did.core.DidKeyHolder;
import foundation.icon.did.document.EncodeType;
import foundation.icon.did.exceptions.AlgorithmException;
import foundation.icon.did.exceptions.KeyPairException;
import foundation.icon.icx.crypto.IconKeys;
import foundation.icon.icx.data.Bytes;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.iitp.iitp_demo.Constants.DID;
import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreDelegationToken;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreIdCard;

/**
 * AA 가 VC 를 발급한 WebView <p/>
 * 해당 화면으로 오기전 필요한 VC 는 모두 발급이 된 상태이어야 한다.
 */
public class WebViewActivity extends BaseActivity{
    private static final int REQUEST_CODE_GET_VP = 0x0101;

    private static final String STATE_CALLBACK_URL = "callback_url";
    private static final String STATE_CALLBACK_METHOD = "callback_method";

    private static final int PAGE_STARTED = 1;
    private static final int PAGE_LOADED = 2;

    private ActivityAppWebviewBinding binding;
    private WebView webView;
    private int webPageStatus;
    private MarketAPI marketAPI = MarketAPI.getInstance();
    private List<String> vcList;
    private String presentationRequestId = null;

    private static String FUNC_REQUEST_DID = "requestDID";
    private static String FUNC_REQUEST_VP = "requestVP";
    private static String FUNC_REQUEST_INFO = "requestInfo";
    private String did;
    private String url;
    private PreferenceUtil preferenceUtil;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;
    private boolean checkBio = false;
    private MsgidVo messageId;
    private String func;
    private DataVo dataVo;
    private String action;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_webview);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        PrintLog.e("URL = " + url);
        preferenceUtil = PreferenceUtil.getInstance(this);
        initWeb();
        checkBio = false;

    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        webView.restoreState(savedInstanceState);
    }

    @Override
    public void onBackPressed(){
        if(binding.webView.canGoBack()){
            binding.webView.goBack();
        }else{
            super.onBackPressed();
        }
    }

    private void initWeb(){
        init();

    }

    @Override
    protected void onResume(){
        super.onResume();
        checkBio = false;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(){
        webView = binding.webView;
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){

                String scheme = request.getUrl().getScheme();
                String host = request.getUrl().getHost();
                String vcs = request.getUrl().getQueryParameter("vcs");
                String callbackUrl = request.getUrl().getQueryParameter("callback");

                PrintLog.e("url = " + request.getUrl().toString());
                PrintLog.e("scheme = " + scheme);
                PrintLog.e("host = " + host);
                PrintLog.e("vcs = " + vcs);
                PrintLog.e("callbackUrl = " + callbackUrl);


                if(scheme != null && scheme.equals("ssi") && host != null){
                    if(host.equals("request_vp") && callbackUrl != null){
                        return true;
                    }else if(host.equals("issued_vc") && vcs != null){
                        return true;
                    }else if(host.equals("cancel")){
                        return true;
                    }else if(host.equals("fail")){
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);

                webPageStatus = PAGE_STARTED;
            }

            @Override
            public void onPageFinished(WebView view, String url){
                super.onPageFinished(view, url);
                PrintLog.e("onPageFinished = " + url);
                webPageStatus = PAGE_LOADED;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
                super.onReceivedError(view, request, error);
                PrintLog.e("onReceivedError = " + error.getDescription());

            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse){
                super.onReceivedHttpError(view, request, errorResponse);
                PrintLog.e("onReceivedHttpError = " + errorResponse.getStatusCode());
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JsInterface(), "SSIBridge");
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl(url);
    }


    class JsInterface{
        /**
         * AA 에서 VP 요청
         *
         * @param responseFuncName VP 응답받을 함수 이
         */
        @SuppressWarnings("unused")
        @JavascriptInterface
        public void requestData(String requestData){
            PrintLog.e("requestData = " + requestData);
            RequestDataVo data = IITPApplication.gson.fromJson(requestData, RequestDataVo.class);
            messageId = data.getMsgid();
            func = data.getFunc();
            dataVo = data.getData();
            action = messageId.getAction();

            if(func.equals(FUNC_REQUEST_DID)){
                startDidSelect();
            }else if(func.equals(FUNC_REQUEST_VP)){
                if(messageId.getAction().equals("register")){
//                    sendVP();
                    new Handler(Looper.getMainLooper()).post(new Runnable(){
                        @Override
                        public void run(){
                            Map<String, Object> map = getClaim();
                            sendResponseVp(true, map);
                        }
                    });

                }else{
                    startGetIDCredentail();
                }
            }else if(func.equals(FUNC_REQUEST_INFO)){
                new Handler(Looper.getMainLooper()).post(new Runnable(){
                    @Override
                    public void run(){
                        Map<String, Object> map = getIdClaim();
                        sendResponseVp(true, map);
                    }
                });
            }
        }

    }

    private String response(String msgid, String func, String data){
        String response = null;
        RequestWebViewInterfaceVo vo = new RequestWebViewInterfaceVo(msgid, func, data);
        response = IITPApplication.gson.toJson(vo);
        return response;
    }


    private String signIcon(String did){
        DidDataVo didData = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(WebViewActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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

        String message = "test_message";
        String b64Signature = null;
        if(didData != null){
            PrintLog.e("PrivateKey() : " + didData.getPrivateKey());
            PrintLog.e("PublicKey() : " + didData.getPublicKey());
            PrintLog.e("DID : " + didData.getDid());
            PrintLog.e("KeyId : " + didData.getIconKeyId());
            DidKeyHolder didKeyHolder = makeDidKeyHolder(did, didData.getIconKeyId(), didData.getPrivateKey());
            PrivateKey privateKey = didKeyHolder.getPrivateKey();
            String b64Message = EncodeType.BASE64URL.encode(message.getBytes(StandardCharsets.UTF_8));
            System.out.println("[SIGN] message=" + message);
            System.out.println("[SIGN] base64Message=" + b64Message);
            Algorithm algorithm = AlgorithmProvider.create(AlgorithmProvider.Type.fromName("ES256K"));
            byte[] bSignature = new byte[0];
            try{
                bSignature = algorithm.sign(privateKey, b64Message.getBytes(StandardCharsets.UTF_8));
            }catch(AlgorithmException e){
                PrintLog.e("signIcon error");
            }

            //verify test
            b64Signature = EncodeType.BASE64URL.encode(bSignature);
            System.out.println("[SIGN] base64Signature=" + b64Signature);
            PublicKey publicKey = null;
            try{
                publicKey = getPublicKey(privateKey);
            }catch(KeyPairException e){
                PrintLog.e("signIcon error");
            }
//        boolean verifyResult = false;
//        try{
//            verifyResult = verifySignature(publicKey,b64Signature,message);
//        }catch(AlgorithmException e){
//            e.printStackTrace();
//        }
//        PrintLog.e("verifyResult = "+verifyResult);
        }




        return b64Signature;
    }

    public static boolean verifySignature(PublicKey publicKey, String b64Signature, String message) throws AlgorithmException {
        System.out.println("[VERIFY] base64Signature=" + b64Signature);
        System.out.println("[VERIFY] message=" + message);
        byte[] bSignature = EncodeType.BASE64URL.decode(b64Signature);
        Algorithm algorithm = AlgorithmProvider.create(AlgorithmProvider.Type.fromName("ES256K"));
        String b64Message = EncodeType.BASE64URL.encode(message.getBytes(StandardCharsets.UTF_8));
        System.out.println("[VERIFY] base64Message=" + b64Message);
        boolean isVerify = algorithm.verify(publicKey, b64Message.getBytes(StandardCharsets.UTF_8), bSignature);
        return isVerify;
    }
    public static PublicKey getPublicKey(PrivateKey privateKey) throws KeyPairException {
        Algorithm algorithm = AlgorithmProvider.create(AlgorithmProvider.Type.ES256K);
        Bytes publicKeyBytes = IconKeys.getPublicKey(new Bytes(algorithm.privateKeyToByte(privateKey)));
        PublicKey publicKey = algorithm.byteToPublicKey(publicKeyBytes.toByteArray());
        return publicKey;
    }

    private String signMessage(String did){
        String message = "test_message";

        KeyManager keyManager = new KeyManager(did);
        String signMessage = keyManager.signMessage(WebViewActivity.this, message.getBytes());
        return signMessage;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String signIndy(String did){
        Indy indy = Indy.getInstance(WebViewActivity.this);
        DidDataVo didData = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(WebViewActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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

//        String sig = null;
        String signMessage = null;
        if(didData != null){
            PrintLog.e("PublicKey() : " + didData.getPublicKey());
            PrintLog.e("DID : " + didData.getDid());
            String message = "test_message";

            byte[] signature = indy.indySignMessage(didData.getPublicKey(), message.getBytes());
            signMessage = Base64.getEncoder().encodeToString(signature);
            PrintLog.e("signMessage = " + signMessage);
        }

        return signMessage;
    }

    /**
     * get meta did
     *
     * @return did
     */
    private String getDID(){
        String did = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(WebViewActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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

//    private void parseVp(String requestVP){
//        String json = preferenceUtil.getPayload(requestVP);
//        vcList = parseRequestVp(json);
//    }

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
        String payload = preferenceUtil.getPayload(json);
        PrintLog.e("payload = " + payload);
        VpRequestVo request = IITPApplication.gson.fromJson(payload, VpRequestVo.class);
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
            VpRequestZKPDataVo zkpdataVo = temp.getZKP();

            if(dataVo != null){
                PrintLog.e("name = " + dataVo.getName());
                PrintLog.e("nonce = " + dataVo.getNonce());
                PrintLog.e("version = " + dataVo.getVersion());
                requestAttributes = dataVo.getRequested_attributes();
                delegateAttributes = dataVo.getDelegated_attributes();
            }
            if(zkpdataVo != null){
                PrintLog.e("name = " + zkpdataVo.getName());
                PrintLog.e("nonce = " + zkpdataVo.getNonce());
                PrintLog.e("version = " + zkpdataVo.getVersion());
                requestAttributes = zkpdataVo.getRequested_attributes();
            }
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
            String vc = CommonPreference.getInstance(WebViewActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreDelegationToken, null);
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
                        String vc = CommonPreference.getInstance(WebViewActivity.this).getSecureSharedPreferences().getString(preKey, null);
                        vclist.add(vc);
                    }
                }
            }
        }

        return vclist;
    }

    private void startDidSelect(){
        Intent intent = new Intent(WebViewActivity.this, DidSelectWebActivity.class);
        startActivityForResult(intent, 3000);
    }

    private void startGetIDCredentail(){
        Intent intent = new Intent(WebViewActivity.this, RequestWebVpActivity.class);
        intent.putExtra("requestVP", dataVo.getJwt());
        startActivityForResult(intent, 4000);
    }

    private void senddid(String did){
        if(did.contains("meta")){
            String signature = signMessage(did);
            ChallengeVo challengeVo = new ChallengeVo(did, signature);
            Call<WebResultVo> rtn = marketAPI.marketAPIInfo.requestChallenge(dataVo.getUrl(), challengeVo);
            rtn.enqueue(new Callback<WebResultVo>(){
                @Override
                public void onResponse(Call<WebResultVo> call, Response<WebResultVo> response){
                    if(response.code() == 200){
                        if(response.body() != null){
                            WebResultVo authResponseVo = response.body();
                            PrintLog.e("result = " + authResponseVo.getResult());
                            sendResponseDid(authResponseVo.getResult());
                        }
                    }else{
                        PrintLog.e("response = " + response.errorBody().toString());
                    }
                }

                @Override
                public void onFailure(Call<WebResultVo> call, Throwable t){
                    PrintLog.e("fail = " + t.getMessage());
                    ToastUtils.custom(Toast.makeText(WebViewActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
                }
            });
        }else if(did.contains("icon")){
            String signature = null;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                signature = signIcon(did);
            }
            ChallengeVo challengeVo = new ChallengeVo(did, signature);
            Call<WebResultVo> rtn = marketAPI.marketAPIInfo.requestChallenge(dataVo.getUrl(), challengeVo);
            rtn.enqueue(new Callback<WebResultVo>(){
                @Override
                public void onResponse(Call<WebResultVo> call, Response<WebResultVo> response){
                    if(response.code() == 200){
                        if(response.body() != null){
                            WebResultVo authResponseVo = response.body();
                            PrintLog.e("result = " + authResponseVo.getResult());
                            sendResponseDid(authResponseVo.getResult());
                        }
                    }else{
                        PrintLog.e("response = " + response.errorBody().toString());
                    }
                }

                @Override
                public void onFailure(Call<WebResultVo> call, Throwable t){
                    PrintLog.e("fail = " + t.getMessage());
                    ToastUtils.custom(Toast.makeText(WebViewActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
                }
            });
        }else if(did.contains("sov")){
            String signature = null;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                signature = signIndy(did);
            }
            ChallengeVo challengeVo = new ChallengeVo(did, signature);
            Call<WebResultVo> rtn = marketAPI.marketAPIInfo.requestChallenge(dataVo.getUrl(), challengeVo);
            rtn.enqueue(new Callback<WebResultVo>(){
                @Override
                public void onResponse(Call<WebResultVo> call, Response<WebResultVo> response){
                    if(response.code() == 200){
                        if(response.body() != null){
                            WebResultVo authResponseVo = response.body();
                            PrintLog.e("result = " + authResponseVo.getResult());
                            sendResponseDid(authResponseVo.getResult());
                        }
                    }else{
                        PrintLog.e("response = " + response.errorBody().toString());
                    }
                }

                @Override
                public void onFailure(Call<WebResultVo> call, Throwable t){
                    PrintLog.e("fail = " + t.getMessage());
                    ToastUtils.custom(Toast.makeText(WebViewActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
                }
            });
        }else{

            showDialog("지원하지 않는 DID 입니다. ");
//            ToastUtils.custom(Toast.makeText(WebViewActivity.this, "지원하지 않는 DID 입니다. ", Toast.LENGTH_SHORT)).show();
        }
    }

    private void sendResponseDid(Boolean result){
        PrintLog.e("sendResponse");
        if(result){
            DataVo data = new DataVo(null, result, did, did, null, null, null, null, null, false);
            RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
            String json = IITPApplication.gson.toJson(responseVo);
            PrintLog.e("json = " + json);
            webView.loadUrl("javascript:Manager.respMessage('" + json + "')");

        }else{
            showDialog("로그인에 실패하였습니다.");
        }
    }

    private void sendResponseDid(String did){
        PrintLog.e("sendResponse");
            DataVo data = new DataVo(null, true, did, did, null, null, null, null, null, false);
            RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
            String json = IITPApplication.gson.toJson(responseVo);
            PrintLog.e("json = " + json);
            webView.loadUrl("javascript:Manager.respMessage('" + json + "')");
    }

    private void sendResponseVp(Boolean result, Map<String, Object> claims){
        PrintLog.e("sendResponse");
        for(String key : claims.keySet()){
            PrintLog.e(key + " : " + claims.get(key));
        }
        DataVo data = new DataVo(null, result, did, did, null, claims.get("name").toString(), claims.get("birth").toString(), claims.get("address").toString(), null, false);
        RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
        String json = IITPApplication.gson.toJson(responseVo);
        PrintLog.e("json = " + json);
        webView.loadUrl("javascript:Manager.respMessage('" + json + "')");
    }

    private Map<String, Object> getClaim(){
        String jwt = dataVo.getJwt();
        Map<String, Object> claims = null;
        List<String> vclist = parseRequestVp(jwt);
        for(String listdata : vclist){
            PrintLog.e("data = " + listdata);
            String payload = preferenceUtil.getPayload(listdata);
            MetadiumVerifier verifierTemp = new MetadiumVerifier();
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("getClaim error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            credential.getCredentialSubject();
            claims = (Map<String, Object>) credential.getCredentialSubject();

        }
        return claims;
    }

    private Map<String, Object> getIdClaim(){
        Map<String, Object> claims = new HashMap<>();
        String idCredentialVC = CommonPreference.getInstance(WebViewActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreIdCard, null);
        String name = null;
        String birth = null;
        String address = null;
        String payload = preferenceUtil.getPayload(idCredentialVC);
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        JWTClaimsSet jwtClaimsSet = null;
        try{
            jwtClaimsSet = JWTClaimsSet.parse(payload);
        }catch(ParseException e){
            PrintLog.e("getIdClaim error");
        }
        VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
        PrintLog.e("did = " + credential.toJSONString());
        Map<String, Object> ctempCaims = (Map<String, Object>) credential.getCredentialSubject();
        for(String temp : ctempCaims.keySet()){
            PrintLog.e(temp + " : " + ctempCaims.get(temp));
            switch(temp){
                case "name":
                    if(ctempCaims.get(temp) != null){
                        name = ctempCaims.get(temp).toString();
                    }
                    break;
                case "birth_date":
                    if(ctempCaims.get(temp) != null){
                        birth = ctempCaims.get(temp).toString();
                    }
                    break;
                case "address":
                    if(ctempCaims.get(temp) != null){
                        address = ctempCaims.get(temp).toString();
                    }
                    break;
                default:
                    break;
            }
        }
        claims.put("name", name);
        claims.put("birth", birth);
        claims.put("address", address);

        return claims;
    }

    private void sendVP(){
        String jwt = dataVo.getJwt();
        Map<String, Object> claims = null;
        List<String> vclist = parseRequestVp(jwt);
        for(String listdata : vclist){
            PrintLog.e("data = " + listdata);
            String payload = preferenceUtil.getPayload(listdata);
            MetadiumVerifier verifierTemp = new MetadiumVerifier();
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("sendVP error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            credential.getCredentialSubject();
            claims = (Map<String, Object>) credential.getCredentialSubject();
        }


        String vp = makeVpData(vclist);
        String uuid = makeUUID();
        VpResponseVo responseVp = new VpResponseVo(uuid, presentationRequestId, vp, null, null);
        Call<WebResultVo> rtn = marketAPI.marketAPIInfo.resposneVp(responseVp);
        Map<String, Object> finalClaims = claims;
        rtn.enqueue(new Callback<WebResultVo>(){
            @Override
            public void onResponse(Call<WebResultVo> call, Response<WebResultVo> response){
                if(response.code() == 200){
                    if(response.body() != null){
                        WebResultVo authResponseVo = response.body();
                        PrintLog.e("result = " + authResponseVo.getResult());
                        sendResponseVp(authResponseVo.getResult(), finalClaims);
                    }
                }
            }

            @Override
            public void onFailure(Call<WebResultVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(WebViewActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
            }
        });
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

    private String makeVpData(List<String> vcList){
        PrintLog.e("did = " + did);
        KeyManager keyManager = new KeyManager(did);
        String keyid = keyManager.getManagementKeyId(WebViewActivity.this);
        ECPrivateKey privatKey = ECKeyUtils.toECPrivateKey(keyManager.getPrivate(WebViewActivity.this), "secp256k1"); // PrivateKey load


        VCVPCreater creator = VCVPCreater.getInstance();
        String vp = creator.vpCreate(did, keyid, privatKey, vcList);
        PrintLog.e("vpData : " + vp);
        return vp;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 3000){
                String did = data.getStringExtra(DID);
                PrintLog.e("did = " + did);
                this.did = did;
                if(action.equals("register")){
                    sendResponseDid(did);
                }else if(action.equals("login")){
                    senddid(did);
                }
            }else if(requestCode == 4000){
                sendVP();
            }
            if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                sendVP();
            }
        }else{
            ToastUtils.custom(Toast.makeText(WebViewActivity.this, "취소 하였습니다.", Toast.LENGTH_SHORT)).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * dialog
     */
    private void showDialog(String message){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        textview.setText(message);
        title.setText("로그인 실패");
        btPositive.setOnClickListener(v -> {
            customDialog.dismiss();
        });
        customDialog.show();
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
                        sendVP();
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

    private void startPincodeActivity(){
        Intent intent;
        intent = new Intent(this, PincodeActivity.class);
        intent.putExtra(PIN_SETTING_TYPE, 2);
        startActivityForResult(intent, REQUEST_CODE_VERIFY_PIN_APP_LOCK);
        //finish();
    }

    private void fingerPrintFactory(boolean on){
        BiometricDialog biometricDialog = new BiometricDialog(WebViewActivity.this,
                getString(R.string.did_fingerprint_title),
                getString(R.string.did_fingerprint_desc),
                new BiometricDialog.OnAuthenticationListener(){
                    @Override
                    public void onSuccess(FingerprintManager.AuthenticationResult result){
                        if(func.equals(FUNC_REQUEST_DID)){
                            startDidSelect();
                        }else if(func.equals(FUNC_REQUEST_VP)){
                            sendVP();
                        }
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


    private void showDialogSetFingerPrint(int type){
        LayoutInflater inflater = (LayoutInflater) WebViewActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog customDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(WebViewActivity.this, 300);
        float height = ViewUtils.dp2px(WebViewActivity.this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btPositive = dialogView.findViewById(R.id.cancel1);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        title.setText(R.string.did_setting_security_edit_bio);
        twoBtn.setVisibility(View.GONE);
        oneBtn.setVisibility(View.VISIBLE);
        btPositive.setText(R.string.ok);
        if(type == 0){
            textview.setText(getString(R.string.did_fingerprint_bio_set_dialog));
        }else{
            textview.setText(getString(R.string.did_fingerprint_bio_set_dialog));
        }
        btPositive.setOnClickListener(v -> {
            customDialog.dismiss();
            finish();
        });
        customDialog.show();
    }

    public static DidKeyHolder makeDidKeyHolder(String did, String keyId, String privateKey) {
        try {
            Algorithm algorithm = AlgorithmProvider.create(AlgorithmProvider.Type.ES256K);
            PrivateKey holderPrivKey = algorithm.byteToPrivateKey(EncodeType.HEX.decode(privateKey));

            DidKeyHolder didKeyHolder = new DidKeyHolder.Builder()
                    .did(did)
                    .keyId(keyId)
                    .type(AlgorithmProvider.Type.ES256K)
                    .privateKey(holderPrivKey)
                    .build();

            return didKeyHolder;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}




