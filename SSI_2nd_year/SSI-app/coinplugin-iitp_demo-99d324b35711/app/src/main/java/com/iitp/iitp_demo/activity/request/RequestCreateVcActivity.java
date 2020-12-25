package com.iitp.iitp_demo.activity.request;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.model.CredentialVo;
import com.iitp.iitp_demo.databinding.ActivityDidsSelectBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Set;

public class RequestCreateVcActivity extends BaseActivity{

    private ActivityDidsSelectBinding layout;
    private int index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent != null){
            Uri uri = getIntent().getData();
            if(uri != null){
                String uriDecode = null;
                try{
                    uriDecode = URLDecoder.decode(uri.toString(), "UTF-8");
                }catch(UnsupportedEncodingException e){
                    PrintLog.e("requestCreateVcActivity error");
                }

                PrintLog.e( "uri : " + uriDecode);
                Set<String> queryList = uri.getQueryParameterNames();
                PrintLog.e( "query : " + uri.getQuery());
                if(queryList.size() > 2){
                }else{
                }
                String jwt = uri.getQueryParameter("jwt");
            }
        }
        layout = DataBindingUtil.setContentView(this, R.layout.activity_dids_select);
        setSupportActionBar(layout.toolbar.appbar);
        setActionBarSet(layout.toolbar,getString(R.string.vc_list_title),true);
        init();
    }

    private void init(){

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


}

