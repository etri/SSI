package com.iitp.iitp_demo.activity;

import android.os.Bundle;
import android.webkit.WebView;

import com.iitp.iitp_demo.R;


/**
 * 약관 화면.<br/>
 * asset 에 약관 html 있음
 */
public class TestPageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int titleId = getIntent().getIntExtra("title",0);
        String url = getIntent().getStringExtra("url");
        setActivityView(this, R.layout.activity_webview, true, false, true, R.string.title, false);
        WebView webView = findViewById(R.id.web_view);
//        WebSettings set = webView.getSettings();
//        set.setJavaScriptEnabled(true);
//        set.setBuiltInZoomControls(true);


        webView.loadUrl(getString(R.string.asset_testpage));
    }
}
