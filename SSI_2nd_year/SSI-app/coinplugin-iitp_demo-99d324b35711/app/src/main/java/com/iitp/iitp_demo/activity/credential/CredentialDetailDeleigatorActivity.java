package com.iitp.iitp_demo.activity.credential;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.api.model.delegaterVCVo;
import com.iitp.iitp_demo.chain.VCVPVerifier;
import com.iitp.iitp_demo.databinding.ActivityCredentailDetailDeigatorBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.verifiable.VerifiableCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.iitp.iitp_demo.chain.VCVPCreater.DELEGATOR_VC;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreIdCard;

public class CredentialDetailDeleigatorActivity extends BaseActivity{

    private ActivityCredentailDetailDeigatorBinding layout;
    private String holderVC = null;
    private String poaVc = null;
    private String idCredentialVC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_credentail_detail_deigator);
        String vc = CommonPreference.getInstance(CredentialDetailDeleigatorActivity.this).getSecureSharedPreferences().getString(DELEGATOR_VC, null);
        idCredentialVC = CommonPreference.getInstance(CredentialDetailDeleigatorActivity.this).getSecureSharedPreferences().getString(NoneZKPStoreIdCard, null);
        if(vc != null){
            delegaterVCVo vcVo = IITPApplication.gson.fromJson(vc, delegaterVCVo.class);
            holderVC = vcVo.getHolderVc();
            poaVc = vcVo.getPoaVc();
        }
        PrintLog.e("VC  = " + vc);
        layout.back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onBackPressed();
            }
        });
        setTitle();
        new Thread(() -> {
            setData();

            setUI();
        }).start();

    }

    /**
     * data set
     */
    private void setUI(){

//
//        layout.issuerDID.setText(issuer);
//        layout.userDID.setText(myDid);
//        layout.dataName.setText(desc);
//        layout.dataData.setText(data);
    }

    /**
     * title bar Set
     *
     * @param did issuer did
     */
    private void setTitle(){
        layout.appbarTitle.setText("행정안전부");
        layout.icon.setBackgroundResource(R.drawable.ic_list_item_govern);


    }

    /**
     * set claim data
     */
    private void setData(){
        VCVPVerifier verifier = VCVPVerifier.getInstance();
        PrintLog.e("idCredentialVC = " + idCredentialVC);
        PrintLog.e("holderVC = " + holderVC);
        PrintLog.e("poaVc = " + poaVc);
        ArrayList<String> vcList = new ArrayList<>();
        vcList.add(idCredentialVC);
        vcList.add(holderVC);
        vcList.add(poaVc);
        List<VerifiableCredential> credentialsList = verifier.verifyVCList(vcList);
        for(int i = 0; i < 3; i++){
            List<String> types = new ArrayList<>(credentialsList.get(i).getTypes());
            PrintLog.e("type = " + types.get(1));
            Map<String, Object> claims = (Map<String, Object>) credentialsList.get(i).getCredentialSubject();
            for(String key : claims.keySet()){
                PrintLog.e(key + " : " + claims.get(key));
                if(i == 0){
                    if(key.equals("name")){
                        runOnUiThread(() -> layout.deligator1.setText(claims.get(key).toString()));
                    }
                }else if(i == 1){
                    if(key.equals("name")){
                        runOnUiThread(() -> layout.deligator.setText(claims.get(key).toString()));
                    }
                }else{
                    if(key.equals("verifier")){
                        runOnUiThread(() -> layout.birth1.setText(claims.get(key).toString()));
                    }else if(key.equals("type")){
                        runOnUiThread(() -> layout.vcType.setText(claims.get(key).toString()));
                    }
                }
            }
            PrintLog.e("--------------------------");
        }
    }
}

