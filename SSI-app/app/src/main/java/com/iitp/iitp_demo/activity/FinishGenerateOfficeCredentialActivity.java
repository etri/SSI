package com.iitp.iitp_demo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.model.ClaimVo;
import com.iitp.iitp_demo.activity.model.CredentialVo;
import com.iitp.iitp_demo.databinding.ActivityFinishOfficeCredentialBinding;
import com.iitp.iitp_demo.model.DidVo;
import com.iitp.iitp_demo.util.CommonPreference;

import java.util.ArrayList;
import java.util.List;

import static com.iitp.iitp_demo.Constants.CHECK_CREDENTIAL;

public class FinishGenerateOfficeCredentialActivity extends BaseActivity{

    ActivityFinishOfficeCredentialBinding layout;
    public ObservableField<String> metaId = new ObservableField<>();
    public String name;
    public String title1;
    public String department;
    public String address;
    public String startDate;
    public String issuer;
    private Gson gson = new Gson();
    private String check = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_finish_office_credential);
        layout.setActivity(this);
        setSupportActionBar(layout.toolbar.appbar);
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            titleText.setText(R.string.requst_presentation_title1);
        }
        Intent intent = getIntent();
        check = intent.getStringExtra(CHECK_CREDENTIAL);
        did = getDidData();
        String credential = did.getOfficeCredentialJson();
        CredentialVo credentialVo = gson.fromJson(credential, CredentialVo.class);
        ClaimVo claimVo = credentialVo.getClaim();
        name = claimVo.getName();
        title1 = claimVo.getTitle();
        department = claimVo.getDepartment();
        address = claimVo.getAddress();
        startDate = claimVo.getStartDate();
        issuer = claimVo.getIssuer();

    }

    public void btnClick(){
        Intent intent = new Intent(FinishGenerateOfficeCredentialActivity.this, MainActivity2.class);
        if(check != null){
            intent.putExtra(Constants.TAB_INDEX, 0);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }

    String didlist = null;
    int didIndex = 0;
    DidVo did = null;
    List<DidVo> didVoList = new ArrayList<>();

    private DidVo getDidData(){
        CommonPreference commPref = CommonPreference.getInstance(this);
        DidVo didData = null;
        didlist = commPref.getStringValue(Constants.ALL_DID_DATA, null);
        didIndex = commPref.getIntValue(Constants.DID_DATA_INDEX, 0);
        didVoList = gson.fromJson(didlist, new TypeToken<ArrayList<DidVo>>(){
        }.getType());
        didData = didVoList.get(didIndex);
        return didData;
    }
}
