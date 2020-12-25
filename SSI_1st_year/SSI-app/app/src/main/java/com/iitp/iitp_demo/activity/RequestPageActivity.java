package com.iitp.iitp_demo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.databinding.ActivityRequestPageBinding;


/**
 * 약관 화면.<br/>
 * asset 에 약관 html 있음
 */
public class RequestPageActivity extends BaseActivity {

    ActivityRequestPageBinding layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_request_page);
        layout.setActivity(this);
        setSupportActionBar(layout.toolbar.appbar);
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            titleText.setText(R.string.request_title);
        }

    }

    public void requestIdBtn(){
        String url = "http://129.254.194.112/mreq/index.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
        finish();

    }

    public void requestOfficeBtn(){
        String url = "http://54.180.2.121:90/ereq/index.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
        finish();
    }

    public void click2(){

        Intent i = new Intent(RequestPageActivity.this, MainActivity.class);
        startActivity(i);
        finish();

    }

    public void click3(){
        Intent i = new Intent(RequestPageActivity.this, SettingActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }
}
