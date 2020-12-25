package com.iitp.iitp_demo.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.api.AgentAPI;
import com.iitp.iitp_demo.api.model.delegaterVCVo;
import com.iitp.iitp_demo.api.model.pushFriendVo;
import com.iitp.iitp_demo.api.model.pushResponseVo;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.databinding.ActivityFriendsBinding;
import com.iitp.iitp_demo.util.BiometricDialog;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;

public class FriendsListActivity extends BaseActivity{

    private ActivityFriendsBinding layout;
    private ArrayList<pushFriendVo> friendsList = new ArrayList<>();
    private ArrayList<pushFriendVo> originnFriendsList = new ArrayList<>();
    private FriendsListAdapter adapter;
    private PreferenceUtil preferenceUtil;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;
    private boolean checkBio;

    pushFriendVo pushFriendVoData;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_friends);
        setSupportActionBar(layout.toolbar.appbar);
        setActionBarSet(layout.toolbar, getString(R.string.friends), true);
        preferenceUtil = PreferenceUtil.getInstance(this);
        String did = getDid();
        getFriendsList(did);

        layout.searchView.setIconifiedByDefault(false);
        //friendsList
        layout.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query){
                friendsList = serchFriendList(query);
                adapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText){
                if(newText.isEmpty() || newText == ""){
                    friendsList = originnFriendsList;
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });
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

    private ArrayList<pushFriendVo> serchFriendList(String serchNickName){
        List<pushFriendVo> originalFriendsList = friendsList;

        ArrayList<pushFriendVo> serchFriendList = new ArrayList<>();

        for(pushFriendVo vo : originalFriendsList){
            if(vo.getFriends().contains(serchNickName)){
                serchFriendList.add(vo);
            }
        }
        return serchFriendList;
    }


    /**
     * 친구 목록 받아오기
     *
     * @param did
     */
    private void getFriendsList(String did){
        if(did != null){
            MutableLiveData<pushResponseVo> response = new MutableLiveData<>();
            response.observe(this, pushAPIResponseVo -> {
                if(pushAPIResponseVo != null){
                    PrintLog.e("status = " + pushAPIResponseVo.getStatus());
                    friendsList.addAll(pushAPIResponseVo.getFriends());
//                    friendsList = pushAPIResponseVo.getFriends();
                    originnFriendsList.addAll(pushAPIResponseVo.getFriends());
//                    originnFriendsList = pushAPIResponseVo.getFriends();
                    Collections.reverse(friendsList);
                    Collections.reverse(originnFriendsList);
                    adapter = new FriendsListAdapter();
                    layout.friendsList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }else{
//                        networkErrorDialog(SplashActivity.this);
                }
            });
            AgentAPI.getInstance().getFriendsList(did, response);
        }else{
            ToastUtils.custom(Toast.makeText(FriendsListActivity.this, "did가 없습니다. ", Toast.LENGTH_SHORT)).show();
        }

    }

    /**
     * did 받아오기
     *
     * @return
     */
    private String getDid(){
        String did = null;
        ArrayList<DidDataVo> didList;
        String didListJson = CommonPreference.getInstance(FriendsListActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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

    /**
     * 위임 VC 생성
     *
     * @param delegaterDid
     * @return
     */
    private String makeDelegateVC(String delegaterDid){
        VCVPCreater creator = VCVPCreater.getInstance();
        String verifier = null;
        switch(delegatorCheck){
            case 0:
                verifier = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001d4c";
                break;
            case 1:
                verifier = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001d50";
                break;
            case 2:
                verifier = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001d50";
                break;
            default:
                verifier = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001d50";
                break;
        }
        String vc = creator.vcCreateDelegationIdCard(FriendsListActivity.this, getDid(), delegaterDid, verifier);
        return vc;

    }

    /**
     * 위임 VC 전달
     *
     * @param holder    holder
     * @param delegeter deletgater
     * @param holderVc  holdervc
     * @param posvc     posvc
     */
    private void sendVCData(String holder, String delegeter, String holderVc, String posvc){
        delegaterVCVo vc = new delegaterVCVo(holder, delegeter, holderVc, posvc, null);
        MutableLiveData<pushResponseVo> response = new MutableLiveData<>();
        response.observe(this, pushAPIResponseVo -> {
            if(pushAPIResponseVo != null){
                PrintLog.e("status = " + pushAPIResponseVo.getStatus());
                finish();
            }else{
//                        networkErrorDialog(SplashActivity.this);
            }
        });

        AgentAPI.getInstance().sendcredential(vc, response);
    }

    /**
     * 친구 목록 리스트  adapter
     */
    class FriendsListAdapter extends BaseAdapter{

        FriendsListAdapter(){

        }

        @Override
        public int getCount(){
            return friendsList.size();
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
            FriendsListAdapter.Holder holder;
            pushFriendVo data = friendsList.get(i);
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_friends, parent, false);
                holder = new FriendsListAdapter.Holder();
                holder.desc1 = convertView.findViewById(R.id.nickname);
                holder.desc2 = convertView.findViewById(R.id.did);
                holder.button = convertView.findViewById(R.id.useBtn);
                convertView.setTag(holder);
            }else{
                holder = (FriendsListAdapter.Holder) convertView.getTag();
            }


            holder.desc1.setText(data.getFriends());
            holder.desc2.setText(data.getDid());
            holder.button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    showDialogSelectDelegator(data);

                }
            });

            return convertView;
        }

        /**
         * holder
         */
        class Holder{
            TextView desc1;
            TextView desc2;

            Button button;
        }


    }

    /**
     * get VC data
     *
     * @return vc
     */
    private String getVCData(){
        List<String> list = preferenceUtil.getAllVCData();
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        String idJwt = null;
        for(String jwt : list){
            String payload = preferenceUtil.getPayload(jwt);
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
                PrintLog.e("getVCData error");
            }
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            List<String> types = new ArrayList<String>(credential.getTypes());
            if(types.get(1).equals("IdentificationCredential")){
                PrintLog.e("types = " + types);
                idJwt = jwt;
                break;
            }

        }
        return idJwt;
    }


    private void setBio(pushFriendVo data){
        checkBio = false;
        pushFriendVoData = data;
        BiometricUtils.hasBiometricEnrolled(this);
        if((BiometricUtils.getPincode(this) == null && !(BiometricUtils.isFingerPrint(this)))){
            if(BiometricUtils.checkFingerprint(this)){
                fingerBioFactory(this, data);
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
                fingerBioFactory(this, data);
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

    private void fingerBioFactory(Context ctx, pushFriendVo data){
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
                        String posvc = makeDelegateVC(data.getDid());
                        String idVc = getVCData();
                        PrintLog.e("getId = " + getDid());
                        PrintLog.e("data.getDid = " + data.getDid());
                        PrintLog.e("idVc = " + idVc);
                        PrintLog.e("posvc = " + posvc);
                        sendVCData(getDid(), data.getDid(), idVc, posvc);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intentData){
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        String posvc = makeDelegateVC(pushFriendVoData.getDid());
                        String idVc = getVCData();
                        PrintLog.e("getId = " + getDid());
                        PrintLog.e("data.getDid = " + pushFriendVoData.getDid());
                        PrintLog.e("idVc = " + idVc);
                        PrintLog.e("posvc = " + posvc);
                        sendVCData(getDid(), pushFriendVoData.getDid(), idVc, posvc);
                    }
                }, 200);
            }
        }
        super.onActivityResult(requestCode, resultCode, intentData);
    }

    private void fingerPrintFactory(pushFriendVo data){
        BiometricDialog biometricDialog = new BiometricDialog(FriendsListActivity.this,
                getString(R.string.did_fingerprint_title),
                getString(R.string.did_fingerprint_desc),
                new BiometricDialog.OnAuthenticationListener(){
                    @Override
                    public void onSuccess(FingerprintManager.AuthenticationResult result){
                        String posvc = makeDelegateVC(data.getDid());
                        String idVc = getVCData();
                        PrintLog.e("getId = " + getDid());
                        PrintLog.e("data.getDid = " + data.getDid());
                        PrintLog.e("idVc = " + idVc);
                        PrintLog.e("posvc = " + posvc);
                        sendVCData(getDid(), data.getDid(), idVc, posvc);
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


    /**
     * finish dialog
     *
     * @param text
     */
    private int delegatorCheck = 0;

    private void showDialogSelectDelegator(pushFriendVo data){
        LayoutInflater inflater = (LayoutInflater) FriendsListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_select_delegator, null);
        AlertDialog password1Dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .show();
        Button next = dialogView.findViewById(R.id.select);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId){
                if(checkedId == R.id.radio1){
                    delegatorCheck = 0;
                }else if(checkedId == R.id.radio2){
                    delegatorCheck = 1;
                }else if(checkedId == R.id.radio3){
                    delegatorCheck = 3;
                }
                PrintLog.e("check = " + delegatorCheck);
            }
        });
        next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setBio(data);
                password1Dialog.dismiss();
            }
        });

        password1Dialog.show();
    }
}

