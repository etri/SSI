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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.KeyManager;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.credential.CredentialDetailUniActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DataVo;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.activity.model.IndyCredentialVo;
import com.iitp.iitp_demo.activity.model.JobDataVo;
import com.iitp.iitp_demo.activity.model.MsgidVo;
import com.iitp.iitp_demo.activity.model.RequestDataVo;
import com.iitp.iitp_demo.activity.model.RequestJobDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestDataVoList;
import com.iitp.iitp_demo.activity.model.VpRequestNoneZKPDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPPriReferentDataVo;
import com.iitp.iitp_demo.activity.model.VpRequestZKPReferentDataVo;
import com.iitp.iitp_demo.activity.model.VpResponseVo;
import com.iitp.iitp_demo.activity.request.DidSelectWebActivity;
import com.iitp.iitp_demo.activity.request.RequestWebVpCompanyActivity;
import com.iitp.iitp_demo.api.MarketAPI;
import com.iitp.iitp_demo.api.PreVCAPI;
import com.iitp.iitp_demo.api.model.CredentialRequestResponseVo;
import com.iitp.iitp_demo.api.model.CredentialRequestVo;
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
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.CompanyType;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreDelegationToken;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreIdCard;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreMobile;
import static com.iitp.iitp_demo.chain.VCVPCreater.OfficeCredential;
import static com.iitp.iitp_demo.chain.VCVPCreater.ZKP_IdentificationCredential;

/**
 * AA 가 VC 를 발급한 WebView <p/>
 * 해당 화면으로 오기전 필요한 VC 는 모두 발급이 된 상태이어야 한다.
 */
public class WebViewJobActivity extends BaseActivity{

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
    private String selectDid;
    private MsgidVo messageId;
    private String func;
    private JobDataVo dataVo;
    private String action;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_webview);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        preferenceUtil = PreferenceUtil.getInstance(this);
        PrintLog.e("URL = " + url);
        initWeb();
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

    /**
     * Webview init
     */
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

    @Override
    protected void onResume(){
        super.onResume();
        PrintLog.e("On Resume");
        indy = Indy.getInstance(this);
        new Thread(() -> {
            indy.createPoolWorksForConfigJSON();
            indy.createWallet();
        }).start();
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
            RequestJobDataVo data = IITPApplication.gson.fromJson(requestData, RequestJobDataVo.class);
            messageId = data.getMsgid();
            func = data.getFunc();
            dataVo = data.getData();
            action = messageId.getAction();


            String FUNC_REQUEST_DID = "requestDID";
            String FUNC_REQUEST_VP = "requestVP";
            String FUNC_OFFER = "offer";
            String FUNC_REQUEST_ISSUER = "requestISSUER";
            String FUNC_REQUEST_EXIT = "exit";
            if(func.equals(FUNC_REQUEST_VP)){
                startGetCompanyData();
            }else if(func.equals(FUNC_OFFER)){
                PrintLog.e("request offer");
                savevc(dataVo);
            }else if(func.equals(FUNC_REQUEST_ISSUER)){
                PrintLog.e("request issuer");
                sendDID();
            }else if(func.equals(FUNC_REQUEST_DID)){
                PrintLog.e("request did");
                startDidSelect();
            }else if(func.equals(FUNC_REQUEST_EXIT)){
                finish();
            }

        }
    }
//
//    private String response(String msgid, String func, String data){
//        String response = null;
//        RequestWebViewInterfaceVo vo = new RequestWebViewInterfaceVo(msgid, func, data);
//        response = IITPApplication.gson.toJson(vo);
//        return response;
//    }

    /**
     * send did
     * @param did did
     * @param result result
     */
    private void senddid(String did, boolean result){
        DataVo data = new DataVo(null, result, did, did, null, null, null, null, null, false);
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

    /**
     * sendResponse
     * @param result result
     */
    private void sendResponse(Boolean result){
        PrintLog.e("sendResponse");
        if(result){
            DataVo data = new DataVo(null, result, null, null, null, null, null, null, null, false);
            RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
            String json = IITPApplication.gson.toJson(responseVo);
            PrintLog.e("json = " + json);
            webView.loadUrl("javascript:Manager.respMessage('" + json + "')");

        }else{
            DataVo data = new DataVo(null, result, null, null, null, null, null, null, null, false);
            RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
            String json = IITPApplication.gson.toJson(responseVo);
            PrintLog.e("json = " + json);
            webView.loadUrl("javascript:Manager.respMessage('" + json + "')");
        }
    }


    /**
     * get meta did
     *
     * @return did
     */
    private String getDID(){
        String did = null;
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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
    private ArrayList<String> parseRequestVp(String json){
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
        Map<String, VpRequestZKPReferentDataVo> noneZKPprequestAttributes = null;
        Map<String, VpRequestZKPReferentDataVo> ZKPprequestAttributes = null;
        Map<String, VpRequestZKPReferentDataVo> zkpprequestAttributes = null;
        Map<String, VpRequestZKPPriReferentDataVo> prirequestAttributes = null;
        VpRequestZKPDataVo zkpdataVo = null;
        VpRequestNoneZKPDataVo dataVo = null;
        VpRequestZKPDataVo zkpsmallDataVo = null;
        ArrayList<String> dataList = new ArrayList<>();
        for(VpRequestDataVo temp : templist){
            dataVo = temp.getNonZKP();
            zkpdataVo = temp.getZKP();
            zkpsmallDataVo = temp.getZkp();
            if(dataVo != null){
                PrintLog.e("name = " + dataVo.getName());
                PrintLog.e("nonce = " + dataVo.getNonce());
                PrintLog.e("version = " + dataVo.getVersion());
                noneZKPprequestAttributes = dataVo.getRequested_attributes();
            }
            if(zkpdataVo != null){
                PrintLog.e("name = " + zkpdataVo.getName());
                PrintLog.e("nonce = " + zkpdataVo.getNonce());
                PrintLog.e("version = " + zkpdataVo.getVersion());
            }
            if(zkpsmallDataVo != null){
                PrintLog.e("name = " + zkpsmallDataVo.getName());
                PrintLog.e("nonce = " + zkpsmallDataVo.getNonce());
                PrintLog.e("version = " + zkpsmallDataVo.getVersion());
            }
            if(noneZKPprequestAttributes != null){
                for(String key : noneZKPprequestAttributes.keySet()){
                    VpRequestZKPReferentDataVo restrictions = noneZKPprequestAttributes.get(key);
                    List<Map<String, String>> tempData = restrictions.getRestrictions();
                    for(Map<String, String> data : tempData){
                        for(String key1 : data.keySet()){
                            PrintLog.e(key1 + " : " + data.get(key1));
                            if(key1.equals("type")){
                                dataList.add(data.get(key1));
                            }
                        }
                    }
                }
            }

        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        if(zkpdataVo != null){
            String requestvp = gson.toJson(zkpdataVo);
            PrintLog.e("requestvp = " + requestvp);
            dataList.add(requestvp);
        }
        if(zkpsmallDataVo != null){
            String requestvp = gson.toJson(zkpsmallDataVo);
            PrintLog.e("requestvp = " + requestvp);
            dataList.add(requestvp);
        }
        for(String a : dataList){
            PrintLog.e("datalist = " + a);
        }
        return dataList;
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
            String vc = CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreDelegationToken, null);
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
                        String vc = CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().getString(preKey, null);
                        vclist.add(vc);
                    }
                }
            }
        }

        return vclist;
    }

    /**
     * sendResponseVp
     * @param result result
     * @param exist exist
     */
    private void sendResponseVp(Boolean result, Boolean exist){
        PrintLog.e("sendResponseVp");
        DataVo data = new DataVo(null, result, null, null, null, null, null, null, null, exist);
        RequestDataVo responseVo = new RequestDataVo(messageId, func, data);
        String json = IITPApplication.gson.toJson(responseVo);
        PrintLog.e("json = " + json);
        webView.loadUrl("javascript:Manager.respMessage('" + json + "')");
        PrintLog.e("action = " + action);
        if(!action.equals("login") && result){
            sendPush("사원증 발급 완료", null);
        }
    }

    /**
     * send Vp
     */
    private void sendVP(){
        String jwt = dataVo.getJwt();
        Map<String, Object> claims = null;
        ArrayList<String> requestVp = parseRequestVp(jwt);
        ArrayList<String> requestVpList = new ArrayList<>();
        String indyDID = null;
        PrintLog.e("requestVp = " + requestVp);
        for(String temp : requestVp){
            if(temp.equals("IdentificationCredential")){
                String idCredential = CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreIdCard, null);
                String phoneCredential = CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreMobile, null);
                requestVpList.add(idCredential);
                requestVpList.add(phoneCredential);
            }
        }
        indy.createWallet();
        String did = preferenceUtil.getIndyDID();
        int size = requestVp.size();
        String vp = null;

        if(action.equals("job")){
            indyDID = preferenceUtil.getIndyDID();
            vp = indy.createProofUniRequest(requestVp.get(size - 1), did);
        }else if(action.equals("login")){

            PrintLog.e(requestVp.get(0));
            if(requestVp.get(0).contains("{")){
                indyDID = preferenceUtil.getIndyDID();
                vp = indy.createProofJobRequest(requestVp.get(size - 1), did);
            }else{
                MetadiumVerifier verifierTemp = new MetadiumVerifier();
                vp = CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().getString(OfficeCredential, null);
                String payload = preferenceUtil.getPayload(vp);
                JWTClaimsSet jwtClaimsSet = null;
                try{
                    jwtClaimsSet = JWTClaimsSet.parse(payload);
                }catch(ParseException e){
                    e.printStackTrace();
                }
                VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
                indyDID = credential.getIssuer().toString();
            }
        }
        requestVpList.add(vp);
        String vpData = null;
        PrintLog.e("indyDID = " + indyDID);
        if(!action.equals("login")){
            vpData = makeVpData(requestVpList);
        }else{
            if(indyDID.contains("meta") || indyDID.contains("icon")){
                vpData = makeVpData(requestVpList);
            }else{
                vpData = requestVpList.get(0);
            }
        }
        PrintLog.e("vpData = " + vpData);
        String uuid = makeUUID();
        String fcm = null;
        if(action.equals("job")){
            String token = FirebaseInstanceId.getInstance().getToken();
            PrintLog.e("token = " + token);
            fcm = token;
        }

        if(!indyDID.contains("did")){
            indyDID = "did:svo:" + indyDID;
        }
        PrintLog.e("indyDID = " + indyDID);
        VpResponseVo responseVp = new VpResponseVo(uuid, presentationRequestId, vpData, fcm, indyDID);
        String testGson = IITPApplication.gson.toJson(responseVp);
        PrintLog.e("testGson = " + testGson);
        Call<ResponseBody> rtn = marketAPI.marketAPIInfo.resposneUniVp(responseURL, responseVp);
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
                        if(responseJson.contains("false")){
                            sendResponse(false);
                        }else{
                            sendResponse(true);
                        }
                    }
                }else{
                    sendResponse(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                sendResponse(false);
                ToastUtils.custom(Toast.makeText(WebViewJobActivity.this, "Server error", Toast.LENGTH_SHORT)).show();
            }
        });
    }

    /**
     * requestZKPSetCredentialRequest
     * @param requestJson requestJson
     * @param offerData offerData
     */
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
                            indy.setRequestJobCredentialData(WebViewJobActivity.this);
                            sendResponseVp(offerData.getResult(), offerData.getExist());
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
                ToastUtils.custom(Toast.makeText(WebViewJobActivity.this, "IDCredential Server error", Toast.LENGTH_SHORT)).show();
            }
        });
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
            CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().edit().putString(key, jwt).apply();
        }
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


//    private String getCredential(){
//        String credentialData = null;
//        String zkpcredData = CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().getString(ZKP_IdentificationCredential, null);
//        ZkpResponse data = IITPApplication.gson.fromJson(zkpcredData, ZkpResponse.class);
//        String temp = preferenceUtil.getPayload(data.getCredid());
//        ZkpClaimVo zkpdagta = IITPApplication.gson.fromJson(temp, ZkpClaimVo.class);
//        String ddd = zkpdagta.getClaim();
//        credentialData = new String(android.util.Base64.decode(ddd, 0));
//        return credentialData;
//    }

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
     * makeVpData
     * @param vcList vcList
     * @return vp
     */
    private String makeVpData(List<String> vcList){
        PrintLog.e("did = " + did);
        KeyManager keyManager = new KeyManager(did);
        String keyid = keyManager.getManagementKeyId(WebViewJobActivity.this);
        ECPrivateKey privatKey = ECKeyUtils.toECPrivateKey(keyManager.getPrivate(WebViewJobActivity.this), "secp256k1"); // PrivateKey load


        VCVPCreater creator = VCVPCreater.getInstance();
        String vp = creator.vpCreate(did, keyid, privatKey, vcList);
        PrintLog.e("vpData : " + vp);
        return vp;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 5000){
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        sendVP();
                    }
                }, 500);
            }else if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        sendVP();
                    }
                }, 200);
            }else if(requestCode == 3000){
                String did = data.getStringExtra(DID);
                PrintLog.e("did = " + did);
                this.selectDid = did;
                senddid(did, true);
            }
        }else{
            if(requestCode == 5000){
                sendResponseVp(false, false);
            }else if(requestCode == 3000){
                senddid(null, false);
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    /**
     * showDialogExist
     * @param message message
     */
    private void showDialogExist(String message){
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


//    private String getDid(){
//        String did = null;
//        ArrayList<DidDataVo> didList;
//        String didListJson = CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
//        if(didListJson != null){
//            didList = IITPApplication.gson.fromJson(didListJson, new TypeToken<ArrayList<DidDataVo>>(){
//            }.getType());
//            for(DidDataVo temp : didList){
//                if(temp.getFavorite()){
//                    did = temp.getDid();
//                }
//            }
//
//        }
//        return did;
//    }

    /**
     * startGetCompanyData
     */
    private void startGetCompanyData(){
        String jwt = dataVo.getJwt();
        Map<String, Object> claims = null;
        ArrayList<String> requestVp = parseRequestVp(jwt);
        String vpJson = IITPApplication.gson.toJson(requestVp);
        Intent intent = new Intent(WebViewJobActivity.this, RequestWebVpCompanyActivity.class);
        intent.putExtra("requestVP", vpJson);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
            @Override
            public void run(){
                startActivityForResult(intent, 5000);
            }
        }, 1000);

    }

    /**
     * startDidSelect
     */
    private void startDidSelect(){
        Intent intent = new Intent(WebViewJobActivity.this, DidSelectWebActivity.class);
        startActivityForResult(intent, 3000);
    }

    /**
     * savevc
     * @param data data
     */
    private void savevc(JobDataVo data){
        String platform = data.getPlatform();
        String vc = null;
        if(platform.equals("meta") || platform.equals("icon")){
            vc = data.getVc();
            PrintLog.e("VC = " + vc);
            CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().edit().putString(OfficeCredential, vc).apply();
            deleteIndyVC();
            sendPush("사원증 발급 완료", vc);
        }else{
            CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().edit().remove(OfficeCredential).apply();
            String json = IITPApplication.gson.toJson(data);
            ZkpResponse offerData = IITPApplication.gson.fromJson(json, ZkpResponse.class);
            if(offerData.getResult()){
                if(action.equals("login")){
                    sendResponseVp(offerData.getResult(), offerData.getExist());
                }else{
                    String indyDID = preferenceUtil.getIndyDID();
                    String credentialRequest = indy.createCredentialRequest(indyDID, offerData.getCredid(), offerData.getOffer(), offerData.getSchemaid());
                    PrintLog.e("credentialRequest = " + credentialRequest);
                    requestZKPSetCredentialRequest(credentialRequest, offerData);
                }
            }else{
                sendResponseVp(offerData.getResult(), offerData.getExist());
            }

        }

    }

    /**
     * deleteIndyVC
     */
    private void deleteIndyVC(){
        Indy indy = Indy.getInstance(WebViewJobActivity.this);
        ArrayList<IndyCredentialVo> indyCredentialVos;
        String indyData = null;
        try{
            indyData = indy.getCredentialsWorksForEmptyFilter();
        }catch(Exception e){
            PrintLog.e("getVCData error");
        }
        PrintLog.e("indyData = " + indyData);
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

    /**
     * sendPush
     * @param text text
     * @param vc vc
     *
     */
    private void sendPush(String text, String vc){

        Indy indy = Indy.getInstance(WebViewJobActivity.this);
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
        final Intent intent = new Intent(WebViewJobActivity.this.getApplicationContext(), CredentialDetailUniActivity.class);
        PrintLog.e("jwt = " + jwt);
        intent.putExtra(JWT_DATA, jwt);
        intent.putExtra(DID, CompanyType);
        intent.putExtra("indyVc", true);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 12, intent, PendingIntent.FLAG_ONE_SHOT);
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
        notificationManager.notify(12, notificationBuilder.build());
    }

    /**
     * sendDID
     */
    private void sendDID(){
        String vc = null;
        vc = CommonPreference.getInstance(WebViewJobActivity.this).getSecureSharedPreferences().getString(OfficeCredential, null);
        if(vc != null){
            MetadiumVerifier verifierTemp = new MetadiumVerifier();
            String payload;
            payload = preferenceUtil.getPayload(vc);
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("sendDID error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            String issuer = credential.getIssuer().toString();
            senddid(issuer, true);
        }else{
            Indy indy = Indy.getInstance(WebViewJobActivity.this);
            ArrayList<IndyCredentialVo> indyCredentialVos;
            String indyData = null;
            try{
                indyData = indy.getCredentialsWorksForEmptyFilter();
            }catch(Exception e){
                PrintLog.e("sendDID error");
            }
            PrintLog.e("indyData = " + indyData);
            indyCredentialVos = IITPApplication.gson.fromJson(indyData, new TypeToken<ArrayList<IndyCredentialVo>>(){
            }.getType());
            int size = indyCredentialVos.size();
            PrintLog.e("size = " + size);
            if(size != 0){
                for(IndyCredentialVo temp : indyCredentialVos){
                    PrintLog.e("iss = " + temp.getSchema_id());
                    if(temp.getSchema_id().contains("company")){
                        PrintLog.e("temp = " + IITPApplication.gson.toJson(temp));
                        String schemeId = temp.getSchema_id();
                        String[] schemeTemp = schemeId.split(":");
                        String did = "did:sov:" + schemeTemp[0];
                        PrintLog.e("did = " + did);
                        senddid(did, true);
                        break;
                    }
                }
            }
        }
    }
}




