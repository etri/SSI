package com.iitp.iitp_demo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.KeyManager;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.credential.CredentialDetailUniActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DataVo;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.activity.model.IndyCredentialVo;
import com.iitp.iitp_demo.activity.model.MsgidVo;
import com.iitp.iitp_demo.activity.model.RequestDataVo;
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
import com.iitp.iitp_demo.activity.setting.SettingBackupActivity;
import com.iitp.iitp_demo.api.MarketAPI;
import com.iitp.iitp_demo.api.PreVCAPI;
import com.iitp.iitp_demo.api.model.ChallengeVo;
import com.iitp.iitp_demo.api.model.CredentialRequestResponseVo;
import com.iitp.iitp_demo.api.model.CredentialRequestVo;
import com.iitp.iitp_demo.api.model.WebResultVo;
import com.iitp.iitp_demo.api.model.ZkpResponse;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.chain.indy.ZkpClaimVo;
import com.iitp.iitp_demo.databinding.ActivityAppWebviewBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.iitp_demo.util.ViewUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.util.ECKeyUtils;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import java.io.IOException;
import java.security.interfaces.ECPrivateKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.iitp.iitp_demo.Constants.DID;
import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.Constants.JWT_DATA;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.UniType;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreDelegationToken;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreIdCard;
import static com.iitp.iitp_demo.chain.VCVPCreater.ZKP_IdentificationCredential;
import static com.iitp.iitp_demo.chain.VCVPCreater.ZKP_UniCredential;

/**
 * AA 가 VC 를 발급한 WebView <p/>
 * 해당 화면으로 오기전 필요한 VC 는 모두 발급이 된 상태이어야 한다.
 */
public class WebViewUniActivity extends BaseActivity{

    private static final int PAGE_STARTED = 1;
    private static final int PAGE_LOADED = 2;

    private ActivityAppWebviewBinding binding;
    private WebView webView;
    private int webPageStatus;
    private MarketAPI marketAPI = MarketAPI.getInstance();
    private String presentationRequestId = null;

    private String did;
    private String url;
    private String responseURL;
    private Indy indy;
    private PreferenceUtil preferenceUtil;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;

    private MsgidVo messageId;
    private String func;
    private DataVo dataVo;
    private String action;
    private String storeRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_webview);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        PrintLog.e("URL = " + url);
        preferenceUtil = PreferenceUtil.getInstance(this);
        initWeb();
        indy = Indy.getInstance(this);
        new Thread(() -> {
            indy.createPoolWorksForConfigJSON();
            indy.createWallet();
        }).start();
        did = getDID();
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

    private void startGetIDCredential(){
        String indyDID = preferenceUtil.getIndyDID();
        if(indyDID == null){
            showDialog("Indy DID가 없습니다.");
        }else{
            Intent intent = new Intent(WebViewUniActivity.this, RequestWebVpActivity.class);
            intent.putExtra("requestVP", dataVo.getJwt());
            startActivityForResult(intent, 4000);
        }
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

            String FUNC_REQUEST_DID = "requestDID";
            String FUNC_REQUEST_VP = "requestVP";
            String FUNC_REQUEST_INFO = "requestInfo";
            String FUNC_SHOW_VCLIST = "showVCList";
            String FUNC_EXIT = "exit";
            if(func.equals(FUNC_REQUEST_DID)){
                startDidSelect();
            }else if(func.equals(FUNC_REQUEST_VP)){
                startGetIDCredential();
            }else if(func.equals(FUNC_REQUEST_INFO)){
                new Thread(WebViewUniActivity.this::sendNameResponseVp).start();
            }else if(func.equals(FUNC_SHOW_VCLIST)){
                sendVCDetail();
            }else if(func.equals(FUNC_EXIT)){
                finish();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void sendResponse(Boolean result){
        PrintLog.e("sendResponse");
        if(result){
            DataVo data = new DataVo(null, result, null, null, null, null, null, null, null, false);
            RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
            String json = IITPApplication.gson.toJson(responseVo);
            PrintLog.e("json = " + json);
            webView.loadUrl("javascript:Manager.respMessage('" + json + "')");
            sendPush("즐업증명서 발급완료", 0);

        }else{
//            showDialog("증명서발급에 실패하였습니다.");
            DataVo data = new DataVo(null, result, null, null, null, null, null, null, null, false);
            RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
            String json = IITPApplication.gson.toJson(responseVo);
            PrintLog.e("json = " + json);
            webView.loadUrl("javascript:Manager.respMessage('" + json + "')");
        }
    }


    private String signMessage(String did){
        String message = "test_message";
        KeyManager keyManager = new KeyManager(did);
        String signMessage = keyManager.signMessage(WebViewUniActivity.this, message.getBytes());
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
        String json = CommonPreference.getInstance(WebViewUniActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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
     * requestvp parsing
     *
     * @param json
     * @return
     */
    private String parseRequestVp(String json){
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
        responseURL = request.getPresentationURL();
        list = request.getPresentationRequest();
        List<VpRequestDataVo> templist = list.getCriteria();
        Map<String, VpRequestZKPReferentDataVo> requestAttributes = null;
        Map<String, VpRequestZKPDelegateDataVo> delegateAttributes = null;
        VpRequestZKPDataVo zkpdataVo = null;
        for(VpRequestDataVo temp : templist){
            VpRequestNoneZKPDataVo dataVo = temp.getNonZKP();
            zkpdataVo = temp.getZKP();

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
        }
        String requestvp = IITPApplication.gson.toJson(zkpdataVo);
        PrintLog.e("requestvp = " + requestvp);

        return requestvp;
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
            String vc = CommonPreference.getInstance(WebViewUniActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreDelegationToken, null);
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
                        String vc = CommonPreference.getInstance(WebViewUniActivity.this).getSecureSharedPreferences().getString(preKey, null);
                        vclist.add(vc);
                    }
                }
            }
        }

        return vclist;
    }

    private void startDidSelect(){
        Intent intent = new Intent(WebViewUniActivity.this, DidSelectWebActivity.class);
        startActivityForResult(intent, 3000);
    }

    private void sendResponseVp(Boolean result, Map<String, Object> claims){
        PrintLog.e("sendResponse");
//        for(String key : claims.keySet()){
//            PrintLog.e(key + " : " + claims.get(key));
//        }
        DataVo data = new DataVo(null, true, null, null, null, null, null, null, null, false);
        RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
        String json = IITPApplication.gson.toJson(responseVo);
        PrintLog.e("json = " + json);
        webView.loadUrl("javascript:Manager.respMessage('" + json + "')");
    }

    private void sendNameResponseVp(){
        PrintLog.e("sendResponse");
        String idCredentialVC = CommonPreference.getInstance(WebViewUniActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreIdCard, null);
        String name = null;
        String idCard = null;
        String payload = preferenceUtil.getPayload(idCredentialVC);
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        JWTClaimsSet jwtClaimsSet = null;
        try{
            jwtClaimsSet = JWTClaimsSet.parse(payload);
        }catch(ParseException e){
           PrintLog.e("sendNameResponseVp error");
        }
        VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
        PrintLog.e("did = " + credential.toJSONString());
        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
        for(String temp : claims.keySet()){
            PrintLog.e(temp + " : " + claims.get(temp));
            if(temp.equals("name")){
                if(claims.get("name") != null){
                    name = claims.get("name").toString();
                }
            }else if(temp.equals("idcardnum")){
                if(claims.get("idcardnum") != null){
                    idCard = claims.get("idcardnum").toString();
                }
            }
        }


        PrintLog.e("name = " + name);
        PrintLog.e("idcardnum = " + idCard);
        DataVo data = new DataVo(null, true, null, null, null, name, null, null, idCard, false);
        RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
        String json = IITPApplication.gson.toJson(responseVo);
        PrintLog.e("json = " + json);
        webView.post(new Runnable(){
            @Override
            public void run(){
                webView.loadUrl("javascript:Manager.respMessage('" + json + "')");
            }
        });

    }

    private void sendNoneZKPVP(){
        String jwt = dataVo.getJwt();
        Map<String, Object> claims = null;
        List<String> vclist = parseNonZKPRequestVp(jwt);
        for(String listdata : vclist){
            PrintLog.e("data = " + listdata);
            String payload = preferenceUtil.getPayload(listdata);
            MetadiumVerifier verifierTemp = new MetadiumVerifier();
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("sendNoneZKPVP error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            credential.getCredentialSubject();
            claims = (Map<String, Object>) credential.getCredentialSubject();
        }
        String vp = makeVpData(vclist);
        String uuid = makeUUID();
        String indyDID = preferenceUtil.getIndyDID();
        String metaDID = getDid();
        PrintLog.e("responseURL = " + responseURL);
        PrintLog.e("metaDID = " + metaDID);
        PrintLog.e("indyDID = " + indyDID);
        VpResponseVo responseVp = new VpResponseVo(uuid, presentationRequestId, vp, null, metaDID);
        Call<ResponseBody> rtn = marketAPI.marketAPIInfo.resposneUniVp(responseURL, responseVp);
        Map<String, Object> finalClaims = claims;
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(response.code() == 200){
                    if(response.body() != null){
                        String responseJson = null;
                        try{
                            responseJson = response.body().string();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                        PrintLog.e("response = " + responseJson);
//                        sendCredentialRequests();
                        ZkpResponse offerData = IITPApplication.gson.fromJson(responseJson, ZkpResponse.class);
//                        sendResponseVp(offerData.getResult(), finalClaims);
                        String credentialRequest = indy.createCredentialRequest(indyDID, offerData.getCredid(), offerData.getOffer(), offerData.getSchemaid());
                        PrintLog.e("credentialRequest = " + credentialRequest);
                        requestZKPSetCredentialRequest(credentialRequest, offerData);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(WebViewUniActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
            }
        });
    }


    /**
     * requestvp parsing
     *
     * @param json
     * @return
     */
    private List<String> parseNonZKPRequestVp(String json){
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
        responseURL = request.getPresentationURL();
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

    private void sendVP(){
        String jwt = dataVo.getJwt();
        Map<String, Object> claims = null;
        String requestVp = parseRequestVp(jwt);

        indy.createWallet();
        String did = preferenceUtil.getIndyDID();
        String vp = indy.createProofRequest(requestVp, did, "id");
        String uuid = makeUUID();
        String fcm = null;
        if(action.equals("job")){
            String token = FirebaseInstanceId.getInstance().getToken();
            PrintLog.e("token = " + token);
            fcm = token;
        }
        String indyDID = preferenceUtil.getIndyDID();
        VpResponseVo responseVp = new VpResponseVo(uuid, presentationRequestId, vp, fcm, indyDID);
        Call<ResponseBody> rtn = marketAPI.marketAPIInfo.resposneUniVp(responseURL, responseVp);
        Map<String, Object> finalClaims = claims;
        rtn.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(response.code() == 200){
                    if(response.body() != null){
                        String responseJson = null;
                        try{
                            responseJson = response.body().string();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                        PrintLog.e("response = " + responseJson);
                        ZkpResponse offerData = IITPApplication.gson.fromJson(responseJson, ZkpResponse.class);
                        sendResponseVp(offerData.getResult(), finalClaims);
                        String credentialRequest = indy.createCredentialRequest(indyDID, offerData.getCredid(), offerData.getOffer(), offerData.getSchemaid());
                        PrintLog.e("credentialRequest = " + credentialRequest);
                        requestZKPSetCredentialRequest(credentialRequest, offerData);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(WebViewUniActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
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
                            savePreVCList(ZKP_UniCredential, vcData.getVc());
                            String vc = getCredential(vcData.getVc());
//                            deleteIndyVC();
                            PrintLog.e("vc = " + vc);
                            storeRequestId = indy.storeCredential(offerData.getId(), vc);
                            PrintLog.e("storeRequestId = " + storeRequestId);
                            indy.setRequestUniCredentialData(WebViewUniActivity.this);
                            sendResponse(true);
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
                ToastUtils.custom(Toast.makeText(WebViewUniActivity.this, "IDCredential Server error", Toast.LENGTH_SHORT)).show();
            }
        });
    }

    private void savePreVCList(String key, String jwt){
        PrintLog.e("VC = " + jwt);
        PrintLog.e("save Key = " + key);
        if(jwt != null){
            CommonPreference.getInstance(WebViewUniActivity.this).getSecureSharedPreferences().edit().putString(key, jwt).apply();
        }
    }

    private String getCredential(String vc){
        String credentialData = null;
        String temp = preferenceUtil.getPayload(vc);
        ZkpClaimVo zkpdagta = IITPApplication.gson.fromJson(temp, ZkpClaimVo.class);
        String ddd = zkpdagta.getClaim();
        credentialData = new String(android.util.Base64.decode(ddd, 0));
        return credentialData;
    }

    private String getCredential(){
        String credentialData = null;
        String zkpcredData = CommonPreference.getInstance(WebViewUniActivity.this).getSecureSharedPreferences().getString(ZKP_IdentificationCredential, null);
        ZkpResponse data = IITPApplication.gson.fromJson(zkpcredData, ZkpResponse.class);
        String temp = preferenceUtil.getPayload(data.getCredid());
        ZkpClaimVo zkpdagta = IITPApplication.gson.fromJson(temp, ZkpClaimVo.class);
        String ddd = zkpdagta.getClaim();
        credentialData = new String(android.util.Base64.decode(ddd, 0));
        return credentialData;
    }

    private String getMeta(){
        String zkpcredData = CommonPreference.getInstance(WebViewUniActivity.this).getSecureSharedPreferences().getString(ZKP_IdentificationCredential, null);
        ZkpResponse data = IITPApplication.gson.fromJson(zkpcredData, ZkpResponse.class);
        return data.getCredid();
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
//        PrintLog.e("did = " + did);
        String myMetaDID = null;
        ArrayList<DidDataVo> didList;
        String didListJson = CommonPreference.getInstance(WebViewUniActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
        if(didListJson != null){
            didList = IITPApplication.gson.fromJson(didListJson, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
        }else{
            didList = new ArrayList<>();
        }
        for(DidDataVo temp : didList){
            if(temp.getFavorite()){
                myMetaDID = temp.getDid();
            }
        }
        KeyManager keyManager = new KeyManager(myMetaDID);
        String keyid = keyManager.getManagementKeyId(WebViewUniActivity.this);
        ECPrivateKey privatKey = ECKeyUtils.toECPrivateKey(keyManager.getPrivate(WebViewUniActivity.this), "secp256k1"); // PrivateKey load


        VCVPCreater creator = VCVPCreater.getInstance();
        String vp = creator.vpCreate(myMetaDID, keyid, privatKey, vcList);
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
                senddid(did, true);
            }else if(requestCode == 4000){
                sendNoneZKPVP();
            }

            if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        sendVP();
                    }
                }, 200);
            }
        }else{
            if(requestCode == 4000){
                sendResponse(false);
            }else if(requestCode == 3000){
                senddid("", false);
            }
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
        title.setText("증명서 발급");
        btPositive.setOnClickListener(v -> {
            customDialog.dismiss();
            finish();
        });
        customDialog.show();
    }


//    private void sendCredentialRequests(){
//        String uuid = makeUUID();
//        String indyDID = preferenceUtil.getIndyDID();
//        PrintLog.e("responseURL = "+responseURL);
//        VpResponseVo responseVp = new VpResponseVo(uuid, presentationRequestId, "proof json string", null, indyDID);
//        Call<ResponseBody> rtn = marketAPI.marketAPIInfo.resposneUniVp(responseURL, responseVp);
//        rtn.enqueue(new Callback<ResponseBody>(){
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
//                if(response.code() == 200){
//                    if(response.body() != null){
//                        String responseJson = null;
//                        try{
//                            responseJson = response.body().string();
//                        }catch(IOException e){
//                            e.printStackTrace();
//                        }
//                        PrintLog.e("response = " + responseJson);
//
//                        ZkpResponse offerData = IITPApplication.gson.fromJson(responseJson, ZkpResponse.class);
////                        sendResponseVp(offerData.getResult(), finalClaims);
////                        String credentialRequest = indy.createCredentialRequest(indyDID, offerData.getCredid(), offerData.getOffer(), offerData.getSchemaid());
////                        PrintLog.e("credentialRequest = " + credentialRequest);
////                        requestZKPSetCredentialRequest(credentialRequest, offerData);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t){
//                PrintLog.e("fail = " + t.getMessage());
//                ToastUtils.custom(Toast.makeText(WebViewUniActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
//            }
//        });
//    }

    private String getDid(){
        String did = null;
        ArrayList<DidDataVo> didList;
        String didListJson = CommonPreference.getInstance(WebViewUniActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
        if(didListJson != null){
            didList = IITPApplication.gson.fromJson(didListJson, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
            for(DidDataVo temp : didList){
                if(temp.getFavorite()){
                    did = temp.getDid();
                }
            }

        }
        return did;
    }

    private void sendPush(String text, int type){

        Indy indy = Indy.getInstance(WebViewUniActivity.this);
        ArrayList<IndyCredentialVo> indyCredentialVos;
        String indyData;
        String jwt = null;
        try{
            indyData = indy.getCredentialsWorksForEmptyFilter();
            PrintLog.e("indyData = " + indyData);
            indyCredentialVos = IITPApplication.gson.fromJson(indyData, new TypeToken<ArrayList<IndyCredentialVo>>(){
            }.getType());
            int size = indyCredentialVos.size();
            PrintLog.e("size = " + size);
            for(IndyCredentialVo temp : indyCredentialVos){
                PrintLog.e("iss = " + temp.getSchema_id());
                if(type == 1){
                    if(temp.getSchema_id().contains("company")){
                        jwt = IITPApplication.gson.toJson(temp);
                    }
                }else{
                    if(temp.getSchema_id().contains("university")){
                        jwt = IITPApplication.gson.toJson(temp);
                    }
                }

            }
        }catch(Exception e){
            PrintLog.e("sendPush error");
        }
        final Intent intent = new Intent(WebViewUniActivity.this.getApplicationContext(), CredentialDetailUniActivity.class);
        PrintLog.e("jwt = " + jwt);
        intent.putExtra(JWT_DATA, jwt);
        intent.putExtra(DID, UniType);
        intent.putExtra("indyVc", true);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_ONE_SHOT);
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
        notificationManager.notify(10, notificationBuilder.build());
    }

    private void senddid(String did, boolean result){
        DataVo data = new DataVo(null, result, did, did, null, null, null, null, null, false);
        RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
        String json = IITPApplication.gson.toJson(responseVo);
        PrintLog.e("json = " + json);
        webView.loadUrl("javascript:Manager.respMessage('" + json + "')");
    }

    private void deleteIndyVC(){
        Indy indy = Indy.getInstance(WebViewUniActivity.this);
        ArrayList<IndyCredentialVo> indyCredentialVos;
        String indyData = null;
        try{
            indyData = indy.getCredentialsWorksForEmptyFilter();
        }catch(Exception e){
            PrintLog.e("deleteIndyVC error");
        }
        PrintLog.e("indyData = " + indyData);
        indyCredentialVos = IITPApplication.gson.fromJson(indyData, new TypeToken<ArrayList<IndyCredentialVo>>(){
        }.getType());
        int size = indyCredentialVos.size();
        PrintLog.e("size = " + size);
        if(size != 0){
            for(IndyCredentialVo temp : indyCredentialVos){
                PrintLog.e("iss = " + temp.getSchema_id());
                if(temp.getSchema_id().contains("university")){
                    indy.deleteCredential(temp.getReferent());
                }
            }
        }
    }

    private void sendVCDetail(){
        String indyData = null;
        String jwt = null;
        ArrayList<IndyCredentialVo> indyCredentialVos;
        try{
            indyData = indy.getCredentialsWorksForEmptyFilter();
            PrintLog.e("indyData = " + indyData);
            indyCredentialVos = IITPApplication.gson.fromJson(indyData, new TypeToken<ArrayList<IndyCredentialVo>>(){
            }.getType());
            int size = indyCredentialVos.size();
            PrintLog.e("size = " + size);
            for(IndyCredentialVo temp : indyCredentialVos){

                PrintLog.e("iss = " + temp.getSchema_id());
                if(temp.getReferent().equals(storeRequestId)){
                    PrintLog.e("store id = "+storeRequestId);
                    PrintLog.e("getReferent id = "+temp.getReferent());
                    jwt = IITPApplication.gson.toJson(temp);
                }

            }
        }catch(Exception e){
            PrintLog.e("sendVCDetail error");
        }
        final Intent intent = new Intent(WebViewUniActivity.this.getApplicationContext(), CredentialDetailUniActivity.class);
        PrintLog.e("jwt = " + jwt);
        intent.putExtra(JWT_DATA, jwt);
        intent.putExtra(DID, UniType);
        intent.putExtra("indyVc", true);
        startActivity(intent);
    }

}




