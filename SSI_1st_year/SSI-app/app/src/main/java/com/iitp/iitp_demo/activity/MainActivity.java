package com.iitp.iitp_demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.databinding.ActivityMainBinding;
import com.iitp.iitp_demo.model.DidVo;
import com.iitp.iitp_demo.util.CommonPreference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private static Context ctx;
    private static Activity activity;
    public String btnString = "";

    private static ActivityMainBinding layout;
    private CommonPreference commPref;

    public String idCredential = null;
    public String officeCredential = null;
    public int didIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_main);
        layout.setActivity(this);
        ctx = this;
        activity = this;
        setSupportActionBar(layout.toolbar.appbar);
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            titleText.setText(R.string.credential_list);
//            layout.toolbar.appbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        }

        commPref = CommonPreference.getInstance(ctx);
        setMetaId();
    }

    @Override
    protected void onResume(){
        super.onResume();
        setMetaId();
    }

    private void setMetaId(){
//        String did = KeyManagerUtil.getDid(this);
        Gson gson = new Gson();
        String didString = commPref.getStringValue(Constants.ALL_DID_DATA, null);
        didIndex = commPref.getIntValue(Constants.DID_DATA_INDEX, 0);
        List<DidVo> didList= new ArrayList<>();
        DidVo did;
        if(didString != null){
            didList = gson.fromJson(didString, new TypeToken<List<DidVo>>(){
            }.getType());

            if(didList.size() != 0){
                did = didList.get(didIndex);
                if(did != null){
                    idCredential = did.getIdCredential();
                    officeCredential = did.getOfficeCredential();

                    if(idCredential == null){
                        layout.credential1.setVisibility(View.INVISIBLE);
                    }else{
                        layout.credential1.setVisibility(View.VISIBLE);
                    }

                    if(officeCredential == null){
                        layout.credential2.setVisibility(View.INVISIBLE);
                    }else{
                        layout.credential2.setVisibility(View.VISIBLE);
                    }
                    if(idCredential == null && officeCredential == null){
                        layout.hasCredentialLayout.setVisibility(View.INVISIBLE);
                    }else{
                        layout.hasCredentialLayout.setVisibility(View.VISIBLE);
                    }

                }

            }



        }

    }

    public void btnTitleClick(){
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    private void sendActivity(){
        Intent intent = new Intent(MainActivity.this, FinishGenerateDidActivity.class);
        startActivity(intent);
        finish();
    }

    public void credential1Click(){
        Intent intent = new Intent(MainActivity.this, FinishGenerateIdCredentialActivity.class);
        startActivity(intent);
        finish();
    }


    public void credential2Click(){
        Intent intent = new Intent(MainActivity.this, FinishGenerateOfficeCredentialActivity.class);
        startActivity(intent);
        finish();
    }

    public void click1(){

        Intent i = new Intent(MainActivity.this, RequestPageActivity.class);
        startActivity(i);
        finish();

    }

    public void click3(){
        Intent i = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(i);
        finish();
    }
    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }
}
