package com.iitp.iitp_demo.activity.credential;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.model.IndyCredentialVo;
import com.iitp.iitp_demo.databinding.ActivityCredentailDetailUniBinding;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.Map;

import static com.iitp.iitp_demo.Constants.DID;
import static com.iitp.iitp_demo.Constants.JWT_DATA;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.CompanyType;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.UniType;

public class CredentialDetailUniActivity extends BaseActivity{

    private ActivityCredentailDetailUniBinding layout;
    private String jwt;
    private IndyCredentialVo attr;
    private boolean indyVc = false;
    private String vcType = null;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        layout = DataBindingUtil.setContentView(this, R.layout.activity_credentail_detail_uni);

        layout.back.setOnClickListener(v -> onBackPressed());
        jwt = intent.getStringExtra(JWT_DATA);
        indyVc = intent.getBooleanExtra("indyVc", false);
        vcType = intent.getStringExtra(DID);
        PrintLog.e("tpye = " + vcType);
        PrintLog.e("jwt = " + jwt);
        PrintLog.e("indyVc = " + indyVc);
//        if(indyVc){
            attr = IITPApplication.gson.fromJson(jwt, IndyCredentialVo.class);
//        }else{
//            //todo
//        }
        setData();
        setTitle();
    }

    /**
     * title bar Set
     *
     * @param did issuer did
     */
    private void setTitle(){
        String title1 = "졸업증명VC";
        String title2 = "사원증VC";
        String title3 = "ETRI";

        Map<String, String> attrData = attr.getAttr();
        String issuer = attr.getSchema_id();
        String[] temp = issuer.split(":");
        if(vcType.equals(UniType)){
            layout.vcName.setText(title1);
            layout.issuerName.setText(attrData.get("collage_name"));
            layout.appbarTitle.setText(attrData.get("collage_name"));
            if(!temp[0].contains("did")){
                String did = "did:sov:" + temp[0];
                layout.issuerDID.setText(did);
            }else{
                layout.issuerDID.setText(temp[0]);
            }
            if(attrData.get("id") != null){
                if(!attrData.get("id").contains("did")){
                    String did = "did:sov:" + attrData.get("id");
                    layout.userDID.setText(did);
                }else{
                    layout.userDID.setText(attrData.get("id"));
                }
            }
            layout.icon.setBackgroundResource(R.drawable.uni);
        }else if(vcType.equals(CompanyType)){
            layout.vcName.setText(title2);
            layout.issuerName.setText(title3);
            layout.appbarTitle.setText(title3);
            if(issuer.contains("meta")||issuer.contains("icon")){
                layout.issuerDID.setText(issuer);
            }else{
                if(!temp[0].contains("did")){
                    String did = "did:sov:" + temp[0];
                    layout.issuerDID.setText(did);
                }else{
                    layout.issuerDID.setText(temp[0]);
                }
            }
            if(attrData.get("id") != null){
                if(!attrData.get("id").contains("did")){
                    String did = "did:sov:" + attrData.get("id");
                    layout.userDID.setText(did);
                }else{
                    layout.userDID.setText(attrData.get("id"));
                }
            }
            layout.icon.setBackgroundResource(R.drawable.etri_icon);
        }
    }

    /**
     * set claim data
     */
    private void setData(){

        if(vcType.equals(UniType)){
            Map<String, String> attrData = attr.getAttr();
            layout.uniCredentialLayout.setVisibility(View.VISIBLE);
            layout.uniNameTv.setText(attrData.get("name"));
            layout.uniBirthTv.setText(attrData.get("birth_date"));
            layout.uniCollageTv.setText(attrData.get("collage_name"));
            layout.uniNumTv.setText(attrData.get("number"));
            layout.uniGraduationDateTv.setText(attrData.get("graduation_date"));
            layout.uniGpaTv.setText(attrData.get("gpa"));
            layout.uniIndateTv.setText(attrData.get("admission_date"));
        }else if(vcType.equals(CompanyType)){
                Map<String, String> attrData = attr.getAttr();
                layout.companyLayout.setVisibility(View.VISIBLE);
                layout.comNameTv.setText(attrData.get("name"));
                layout.comGroupTv.setText(attrData.get("group_name"));
                layout.comNumTv.setText(attrData.get("corp-id"));
                layout.comDateTv.setText(attrData.get("issuancedate"));
        }
    }
}

