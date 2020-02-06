package com.iitp.iitp_demo.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.iitp.core.protocol.MetaProxy;
import com.iitp.core.protocol.data.RegistryAddress;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.data.RegistryManager;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;

public class SplashActivity extends AppCompatActivity{

    private CommonPreference commPref;
    String metaId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        PrintLog.e("splsh");
        setContentView(R.layout.activity_splash);
        boolean hasid = false;
        commPref = CommonPreference.getInstance(this);
        metaId = commPref.getStringValue(Constants.ALL_DID_DATA, null);


// get system address
        new Thread(new Runnable(){
            @Override
            public void run(){
                // Meta  Proxy 에서 contract address 정보를 얻는다.
                RegistryAddress registryAddress;
                try{
                    registryAddress = new MetaProxy(Constants.PROXY_URL).getAllServiceAddress();
                    RegistryManager.setRegistryAddress(SplashActivity.this, registryAddress);
                }catch(Exception e){
                    registryAddress = RegistryManager.getRegistryAddress(SplashActivity.this);
                    if(registryAddress == null){
                        try{
                            RegistryManager.setRegistryAddress(SplashActivity.this, Constants.DEFAULT_REGISTRY_ADDRESS);
                        }catch(Exception e2){
                            // not occur
                        }
                    }
                }finally{
                    startMainActivity();
                }
            }
        }).start();


    }

    private void startMainActivity(){
        Intent intent;
        if(metaId == null){
            intent = new Intent(this, SelectDidActivity.class);
        }else{
            intent = new Intent(this, MainActivity2.class);
        }

        startActivity(intent);
        finish();
    }

}
