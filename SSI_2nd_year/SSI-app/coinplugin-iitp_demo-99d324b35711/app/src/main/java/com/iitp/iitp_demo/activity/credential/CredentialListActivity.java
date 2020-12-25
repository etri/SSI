package com.iitp.iitp_demo.activity.credential;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.model.CredentialData;
import com.iitp.iitp_demo.activity.model.ProductVC;
import com.iitp.iitp_demo.api.AgentAPI;
import com.iitp.iitp_demo.api.model.delegaterVCVo;
import com.iitp.iitp_demo.api.model.pushResponseVo;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.chain.VCVPVerifier;
import com.iitp.iitp_demo.databinding.ActivityCredentailListBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.iitp.iitp_demo.Constants.DELEGATOR_TOKEN;
import static com.iitp.iitp_demo.Constants.DELEGATOR_TOKEN_SET;
import static com.iitp.iitp_demo.Constants.DID;
import static com.iitp.iitp_demo.Constants.DID_DATA;
import static com.iitp.iitp_demo.Constants.JWT_DATA;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.CardTokenType;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.GoverType;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.MobileType;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.PostType;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.ProductType;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.StockType;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.UniType;
import static com.iitp.iitp_demo.chain.VCVPCreater.DELEGATOR_VC;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreIdCard;

public class CredentialListActivity extends BaseActivity{

    private ActivityCredentailListBinding layout;

    private String type = null;
    private ArrayList<String> jwtData = new ArrayList<>();
    private List<CredentialData> listData = new ArrayList<>();
    private String delgateVC = null;
    private PreferenceUtil preferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        type = intent.getStringExtra(DID);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_credentail_list);
        preferenceUtil = PreferenceUtil.getInstance(this);
        setTitle(type);
        layout.back.setOnClickListener(v -> onBackPressed());

    }

    @Override
    protected void onResume(){
        listData.clear();
        PrintLog.e("type = " + type);
        PrintLog.e("onResume()");
        if(type.equals(GoverType)){
            setVCData();
            boolean receive = CommonPreference.getInstance(CredentialListActivity.this).getSecureSharedPreferences().getBoolean(DELEGATOR_TOKEN_SET, false);
            if(receive){
                setDelegaterVC();
            }else{
                String vc = CommonPreference.getInstance(this).getSecureSharedPreferences().getString(DELEGATOR_VC, null);
                PrintLog.e("vc = " + vc);
                setDelegateVCData(vc);
            }
        }else if(type.equals(ProductType)){
            ArrayList<ProductVC> productVC = preferenceUtil.getProductVC();
            setLoginVCData();
            if(productVC != null){
                setProductVCData(productVC);
            }
        }else{
            setVCData();
        }
        super.onResume();
    }

    /**
     * 위임 VC 처리
     */
    private void setDelegaterVC(){
        String token = CommonPreference.getInstance(this).getSecureSharedPreferences().getString(DELEGATOR_TOKEN, null);
        PrintLog.e("token = " + token);

        if(token != null){
            layout.progresslayout.setVisibility(View.VISIBLE);
            MutableLiveData<pushResponseVo> response = new MutableLiveData<>();
            response.observe(this, pushAPIResponseVo -> {
                if(pushAPIResponseVo != null){
                    PrintLog.e("status = " + pushAPIResponseVo.getStatus());
                    PrintLog.e("holderDid = " + pushAPIResponseVo.getHolderDid());
                    PrintLog.e("holderVc = " + pushAPIResponseVo.getHolderVc());
                    PrintLog.e("poaVc = " + pushAPIResponseVo.getPoaVc());
                    delgateVC = IITPApplication.gson.toJson(pushAPIResponseVo);
                    PrintLog.e("delgateVC = " + delgateVC);
                    CommonPreference.getInstance(this).getSecureSharedPreferences().edit().putString(DELEGATOR_VC, delgateVC).commit();
                    setDelegateVCData(delgateVC);
                    layout.progresslayout.setVisibility(View.GONE);
                }
//                else{
////                    CommonPreference.getInstance(CredentialListActivity.this).getSecureSharedPreferences().edit().putBoolean(DELEGATOR_TOKEN_SET, false).apply();
////                        networkErrorDialog(SplashActivity.this);
//                }
            });

            AgentAPI.getInstance().requestGetVC(token, response);
        }
    }

    /**
     * 위임 VC claim data 처리
     *
     * @param delgateV vc
     */
    private void setDelegateVCData(String delgateVC){
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        pushResponseVo data = IITPApplication.gson.fromJson(delgateVC, pushResponseVo.class);
        List<String> list = new ArrayList<>();
        if(data == null){
            CredentialListAdapter adapter = new CredentialListAdapter();
            layout.credentialList.setAdapter(adapter);
            return;
        }
        list.add(data.getPoaVc());
        String desc = null;
        for(String jwt : list){
            String payload = preferenceUtil.getPayload(jwt);
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("setDelegateVCData error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            String issuer = credential.getIssuer().toString();
            List<String> listTypes = new ArrayList<>(credential.getTypes());
            PrintLog.e("type = " + listTypes.get(1));
            String credentialType = listTypes.get(1);

            PrintLog.e("issuer = " + issuer);
            jwtData.add(jwt);
            Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
//            for(String key : claims.keySet()){
////                PrintLog.e(key + " : " + claims.get(key));
////                if(desc.length() == 0){
////                    desc = VCVPCreater.getInstance().getKor(key);
////                }else{
////                    String dataName = VCVPCreater.getInstance().getKor(key);
////                    if(dataName != null){
////                        desc = desc + ", " + dataName;
////                    }
////                }
////            }
            PrintLog.e("desc = " + desc);
            MutableLiveData<String> unReadCount;
            unReadCount = new MutableLiveData<>();
            new Thread(() -> {
                setDelgateDescData(unReadCount);
            }).start();
            unReadCount.observe(this, s -> {
                listData.add(new CredentialData(credential, s, issuer, credentialType));
                CredentialListAdapter adapter = new CredentialListAdapter();
                layout.credentialList.setAdapter(adapter);
            });
        }
    }

    private String myName = null;
    private String delgatorName = null;

    /**
     * set claim data
     */
    private void setDelgateDescData(MutableLiveData<String> desc){

        VCVPVerifier verifier = VCVPVerifier.getInstance();
        ArrayList<String> vcList = new ArrayList<>();
        String idCredentialVC = CommonPreference.getInstance(CredentialListActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreIdCard, null);
        String vc = CommonPreference.getInstance(this).getSecureSharedPreferences().getString(DELEGATOR_VC, null);
        if(vc != null){
            delegaterVCVo vcVo = IITPApplication.gson.fromJson(vc, delegaterVCVo.class);
            vcList.add(idCredentialVC);
            vcList.add(vcVo.getHolderVc());
            vcList.add(vcVo.getPoaVc());
        }

        List<VerifiableCredential> credentialsList = verifier.verifyVCList(vcList);
        for(int i = 0; i < 3; i++){
            List<String> types = new ArrayList<>(credentialsList.get(i).getTypes());
            PrintLog.e("type = " + types.get(1));
            Map<String, Object> claims = (Map<String, Object>) credentialsList.get(i).getCredentialSubject();
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(i == 0){
                    if(key.equals("name")){
                        myName = claims.get(key).toString();
                    }
                }else if(i == 1){
                    if(key.equals("name")){
                        delgatorName = claims.get(key).toString();
                    }
                }else{
//                    if(key.equals("verifier")){
//                        runOnUiThread(() -> layout.birth1.setText(claims.get(key).toString()));
//                    }else if(key.equals("type")){
//                        runOnUiThread(() -> layout.vcType.setText(claims.get(key).toString()));
//                    }
                }
            }
            PrintLog.e("--------------------------");
        }
        runOnUiThread(() -> {
            desc.setValue(myName + ", " + delgatorName + "");
        });

//        desc = myName + ", " + delgatorName + "";
//        return desc;
    }

    /**
     * 일반 VC 처리
     */
    private void setVCData(){
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        List<String> list = preferenceUtil.getAllVCData();
        for(String jwt : list){
            if(jwt.contains("{")){
                pushResponseVo vo = IITPApplication.gson.fromJson(jwt, pushResponseVo.class);
                jwt = preferenceUtil.getPayload(vo.getPoaVc());
            }
            String payload = null;
            if(jwt != null){
                if(!jwt.contains("{")){
                    payload = preferenceUtil.getPayload(jwt);
                }else{
                    payload = jwt;
                }
            }
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("setDelegateVCData error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            String issuer = credential.getIssuer().toString();
            List<String> listTypes = new ArrayList<>(credential.getTypes());
            PrintLog.e("type = " + listTypes.get(1));
            String credentialType = listTypes.get(1);
            String desc = "";

            switch(type){
                case GoverType:
                    if(credentialType.equals("IdentificationCredential")){
                        PrintLog.e("credentialType = " + credentialType);
                        jwtData.add(jwt);
                        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                        String name = null;
                        String birth = null;
                        String address = null;
                        for(String key : claims.keySet()){
                            PrintLog.e(key + " : " + claims.get(key));
                            if(key.equals("name")){
                                name = claims.get(key).toString();
                            }else if(key.equals("birth_date")){
                                birth = claims.get(key).toString();
                            }else if(key.equals("address")){
                                address = claims.get(key).toString();
                            }
                        }
                        desc = name + ", " + birth + ", " + address;
                        PrintLog.e("desc = " + desc);
                        listData.add(new CredentialData(credential, desc, issuer, credentialType));
                    }
                    break;
                case CardTokenType:
                    if(credentialType.equals("CardTokenCredential")){
                        PrintLog.e("credentialType = " + credentialType);
                        jwtData.add(jwt);
                        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                        String token = null;
                        for(String key : claims.keySet()){
                            if(key.equals("token")){
                                token = claims.get(key).toString();
                            }
                        }
                        desc = token;
                        PrintLog.e("desc = " + desc);
                        if(listData.size() == 0){
                            listData.add(new CredentialData(credential, desc, issuer, credentialType));
                        }else{
                            listData.add(new CredentialData(credential, desc, issuer, credentialType));
                        }
                    }
                    break;
                case PostType:
                    if(credentialType.equals("AddressCredential")){
                        PrintLog.e("credentialType = " + credentialType);
                        jwtData.add(jwt);
                        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                        String name = null;
                        String address = null;
                        for(String key : claims.keySet()){
                            if(key.equals("name")){
                                name = claims.get(key).toString();
                            }else if(key.equals("address")){
                                address = claims.get(key).toString();
                            }
                        }
                        desc = name + ", " + address;
                        PrintLog.e("desc = " + desc);
                        listData.add(new CredentialData(credential, desc, issuer, credentialType));
                    }
                    break;
                case UniType:
                    if(credentialType.equals("GraduationCredential") || credentialType.equals("EmployeeCredential")){
                        PrintLog.e("credentialType = " + credentialType);
                        jwtData.add(jwt);
                        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                        for(String key : claims.keySet()){
                            PrintLog.e(key + " : " + claims.get(key));
                            if(desc.length() == 0){
                                desc = VCVPCreater.getInstance().getKor(key);
                            }else{
                                desc = desc + ", " + VCVPCreater.getInstance().getKor(key);
                            }
                        }
                        PrintLog.e("desc = " + desc);
                        listData.add(new CredentialData(credential, desc, issuer, credentialType));
                    }
                    break;
                case ProductType:
                    if(credentialType.equals("ProductProofCredential") || credentialType.equals("ProductCredential")){
                        PrintLog.e("credentialType = " + credentialType);
                        jwtData.add(jwt);
                        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                        for(String key : claims.keySet()){
                            PrintLog.e(key + " : " + claims.get(key));
                            if(desc.length() == 0){
                                desc = VCVPCreater.getInstance().getKor(key);
                            }else{
                                desc = desc + ", " + VCVPCreater.getInstance().getKor(key);
                            }
                        }
                        PrintLog.e("desc = " + desc);
                        listData.add(new CredentialData(credential, desc, issuer, credentialType));
                    }
                    break;
                case StockType:
                    if(credentialType.equals("StockServiceCredential")){
                        PrintLog.e("credentialType = " + credentialType);
                        jwtData.add(jwt);
                        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                        String id = null;
                        String name = null;
                        String address = null;
                        String start_date = null;

                        for(String key : claims.keySet()){
                            if(key.equals("register_id")){
                                id = claims.get(key).toString();
                            }else if(key.equals("name")){
                                name = claims.get(key).toString();
                            }else if(key.equals("start_date")){
                                start_date = claims.get(key).toString();
                            }else if(key.equals("address")){
                                address = claims.get(key).toString();
                            }
                        }
                        PrintLog.e("desc = " + desc);
                        desc = id + ", " + name + ", " + address + ", " + start_date;
                        listData.add(new CredentialData(credential, desc, issuer, credentialType));
                    }
                    break;
                case MobileType:
                    if(credentialType.equals("PhoneCredential")){
                        PrintLog.e("credentialType = " + credentialType);
                        jwtData.add(jwt);
                        Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                        String name = null;
                        String address = null;

                        for(String key : claims.keySet()){
                            PrintLog.e(key + " : " + claims.get(key));
                            if(key.equals("name")){
                                name = claims.get(key).toString();
                            }else if(key.equals("phone_num")){
                                address = claims.get(key).toString();
                            }
                        }
                        PrintLog.e("desc = " + desc);
                        desc = name + ", " + address;
                        listData.add(new CredentialData(credential, desc, issuer, credentialType));
                    }
                    break;
                default:
                    break;
            }
        }
        CredentialListAdapter adapter = new CredentialListAdapter();
        layout.credentialList.setAdapter(adapter);
    }

    /**
     * 일반 VC 처리
     */
    private void setLoginVCData(){
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        List<String> list = preferenceUtil.getAllVCData();
        for(String jwt : list){
            if(jwt.contains("{")){
                pushResponseVo vo = IITPApplication.gson.fromJson(jwt, pushResponseVo.class);
                jwt = preferenceUtil.getPayload(vo.getPoaVc());
            }
            String payload;
            if(!jwt.contains("{")){
                payload = preferenceUtil.getPayload(jwt);
            }else{
                payload = jwt;
            }
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("setLoginVCData error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            String issuer = credential.getIssuer().toString();
            List<String> listTypes = new ArrayList<>(credential.getTypes());
            PrintLog.e("type = " + listTypes.get(1));
            String credentialType = listTypes.get(1);
            String desc = "";
            if(credentialType.equals("LoginCredential")){
                PrintLog.e("credentialType = " + credentialType);
                jwtData.add(jwt);
                Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                String name = null;
                String birth = null;
                String address = null;
                String mobile = null;
                for(String key : claims.keySet()){
                    PrintLog.e(key + " : " + claims.get(key));
                    if(key.equals("name")){
                        name = claims.get(key).toString();
                    }else if(key.equals("birth_date")){
                        birth = claims.get(key).toString();
                    }else if(key.equals("address")){
                        address = claims.get(key).toString();
                    }else if(key.equals("phone_num")){
                        mobile = claims.get(key).toString();
                    }
                }
                desc = name + ", " + birth + ", " + address + ", " + mobile;
                PrintLog.e("desc = " + desc);
                listData.add(new CredentialData(credential, desc, issuer, credentialType));
            }
        }

        CredentialListAdapter adapter = new CredentialListAdapter();
        layout.credentialList.setAdapter(adapter);
    }

    private void setProductVCData(ArrayList<ProductVC> productVCData){

        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        if(productVCData.size() != 0){
            for(ProductVC data : productVCData){
                if(data != null){
                    String productVc = data.getProductVC();
                    String productProofVc = data.getProductProofVC();
                    String productVcPayload = preferenceUtil.getPayload(productVc);
                    String productProofVcPayload = preferenceUtil.getPayload(productProofVc);
                    JWTClaimsSet jwtClaimsSet = null;
                    try{
                        jwtClaimsSet = JWTClaimsSet.parse(productVcPayload);
                    }catch(ParseException e){
                        PrintLog.e("setProductVCData error");
                    }
                    VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
                    String issuer = credential.getIssuer().toString();
                    List<String> listTypes = new ArrayList<>(credential.getTypes());
                    PrintLog.e("type = " + listTypes.get(1));
                    String credentialType = listTypes.get(1);
                    String desc = "";
                    PrintLog.e("issuer = " + issuer);
                    jwtData.add(productVc);
                    Map<String, Object> claims = (Map<String, Object>) credential.getCredentialSubject();
                    for(String key : claims.keySet()){
                        PrintLog.e(key + " : " + claims.get(key));
                        if(desc.length() == 0){
                            desc = VCVPCreater.getInstance().getKor(key);
                        }else{
                            String dataName = VCVPCreater.getInstance().getKor(key);
                            if(dataName != null){
                                desc = desc + ", " + dataName;
                            }
                        }
                    }
                    PrintLog.e("desc = " + desc);
                    listData.add(new CredentialData(credential, desc, issuer, credentialType));
                    try{
                        jwtClaimsSet = JWTClaimsSet.parse(productProofVcPayload);
                    }catch(ParseException e){
                        PrintLog.e("setProductVCData error");
                    }
                    credential = verifierTemp.toCredential(jwtClaimsSet);
                    issuer = credential.getIssuer().toString();
                    listTypes = new ArrayList<>(credential.getTypes());
                    PrintLog.e("type = " + listTypes.get(1));
                    credentialType = listTypes.get(1);
                    desc = "";
                    PrintLog.e("issuer = " + issuer);
                    jwtData.add(productProofVc);
                    claims = (Map<String, Object>) credential.getCredentialSubject();
                    for(String key : claims.keySet()){
                        PrintLog.e(key + " : " + claims.get(key));
                        if(desc.length() == 0){
                            desc = VCVPCreater.getInstance().getKor(key);
                        }else{
                            String dataName = VCVPCreater.getInstance().getKor(key);
                            if(dataName != null){
                                desc = desc + ", " + dataName;
                            }
                        }
                    }
                    PrintLog.e("desc = " + desc);
                    listData.add(new CredentialData(credential, desc, issuer, credentialType));
                }

            }
        }
        CredentialListAdapter adapter = new CredentialListAdapter();
        layout.credentialList.setAdapter(adapter);
    }

    /**
     * title 설정
     *
     * @param did did
     */
    private void setTitle(String did){
        switch(did){
            case GoverType:
                layout.appbarTitle.setText("행정안전부");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_govern);
                break;
            case CardTokenType:
                layout.appbarTitle.setText("카드사");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_card);
                break;
            case PostType:
                layout.appbarTitle.setText("우체국");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_post);
                break;
            case UniType:
                layout.appbarTitle.setText("대학 컨소시엄");
                layout.icon.setBackgroundResource(R.drawable.uni);
                break;
            case DELEGATOR_VC:
                layout.appbarTitle.setText("신분증 위임장");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_vc);
                break;
            case ProductType:
                layout.appbarTitle.setText("고가품마켓");
                layout.icon.setBackgroundResource(R.drawable.market);
                break;
            case StockType:
                layout.appbarTitle.setText("증권사");
                layout.icon.setBackgroundResource(R.drawable.stock);
                break;
            case MobileType:
                layout.appbarTitle.setText("통신사");
                layout.icon.setBackgroundResource(R.drawable.phone);
                break;
            default:
                break;

        }
    }


    /**
     * list adapter
     */
    class CredentialListAdapter extends BaseAdapter{

        CredentialListAdapter(){

        }

        @Override
        public int getCount(){
            return listData.size();
        }

        @Override
        public Object getItem(int i){
            return null;
        }

        @Override
        public long getItemId(int i){
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent){
            CredentialListAdapter.Holder holder;
            CredentialData data = listData.get(i);
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_credential_2, parent, false);
                holder = new CredentialListAdapter.Holder();
                holder.icon = convertView.findViewById(R.id.icon);
                holder.desc1 = convertView.findViewById(R.id.desc1);
                holder.desc2 = convertView.findViewById(R.id.desc2);
                holder.layout = convertView.findViewById(R.id.layout);
                holder.newMessage = convertView.findViewById(R.id.newMessage);
                convertView.setTag(holder);
            }else{
                holder = (CredentialListAdapter.Holder) convertView.getTag();
            }
            PrintLog.e("data.getDid() = " + data.getType());
            if(data.getType().equals("DelegatedVC")){
                boolean receive = CommonPreference.getInstance(CredentialListActivity.this).getSecureSharedPreferences().getBoolean(DELEGATOR_TOKEN_SET, false);
                if(receive){
                    holder.newMessage.setVisibility(View.VISIBLE);
                }else{
                    holder.newMessage.setVisibility(View.GONE);
                }
            }else{
                holder.newMessage.setVisibility(View.GONE);
            }

            holder.desc2.setText(data.getDesc1());
            holder.layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent intent = new Intent(CredentialListActivity.this, CredentialListDetailActivity.class);
                    VerifiableCredential credential = data.getCredential();
                    if(data.getType().equals("DelegatedVC")){
                        CommonPreference.getInstance(CredentialListActivity.this).getSecureSharedPreferences().edit().putBoolean(DELEGATOR_TOKEN_SET, false).apply();
                    }
                    if(data.getType().equals("CardTokenCredential")){
                        intent.putExtra("index", i);
                    }
                    LinkedHashMap<String, Object> jsonData = credential.getJsonObject();
                    intent.putExtra(DID_DATA, jsonData);
                    PrintLog.e("jwtDagta = " + jwtData.get(i));
                    intent.putExtra(JWT_DATA, jwtData.get(i));
                    startActivity(intent);
                }
            });

            switch(type){
                case GoverType:
                    PrintLog.e("data.getType()= " + data.getType());
                    if(data.getType().equals("IdentificationCredential")){
                        holder.icon.setBackgroundResource(R.drawable.ic_list_item_govern);
                        holder.desc1.setText(R.string.idcredentialVC);
                    }else if(data.getType().equals("DelegatedVC")){
                        holder.desc1.setText("위임신분증VC");
                        holder.icon.setBackgroundResource(R.drawable.ic_list_item_vc);
                    }
                    break;
                case CardTokenType:
                    holder.icon.setBackgroundResource(R.drawable.ic_list_item_card);
                    if(i == 0){
                        holder.desc1.setText(R.string.cardVC1);
                    }else{
                        holder.desc1.setText(R.string.cardVC2);
                    }

                    break;
                case PostType:
                    holder.icon.setBackgroundResource(R.drawable.ic_list_item_post);
                    holder.desc1.setText(R.string.addressVC);
                    break;
                case UniType:
                    holder.icon.setBackgroundResource(R.drawable.uni);
                    break;
                case DELEGATOR_VC:
                    holder.desc1.setText("위임장");
                    holder.icon.setBackgroundResource(R.drawable.ic_list_item_vc);
                    break;
                case ProductType:
                    if(data.getType().equals("LoginCredential")){
                        holder.desc1.setText("Men's Watch VC");
                    }else if(data.getType().equals("ProductCredential")){
                        holder.desc1.setText("물품정보VC");
                    }else if(data.getType().equals("ProductProofCredential")){
                        holder.desc1.setText("거래증명VC");
                    }
                    holder.icon.setBackgroundResource(R.drawable.market);
                    break;
                case StockType:
                    holder.desc1.setText("한국증권VC");
                    holder.icon.setBackgroundResource(R.drawable.stock);
                    break;
                case MobileType:
                    holder.desc1.setText("한국통신VC");
                    holder.icon.setBackgroundResource(R.drawable.phone);
                    break;
                default:
                    break;
            }
            return convertView;
        }

        /**
         * holder
         */
        class Holder{
            ImageView icon;
            TextView desc1;
            TextView desc2;
            TextView newMessage;
            ConstraintLayout layout;
        }


    }

}

