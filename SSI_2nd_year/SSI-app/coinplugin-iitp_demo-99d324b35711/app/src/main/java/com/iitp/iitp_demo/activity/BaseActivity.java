package com.iitp.iitp_demo.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.databinding.LayoutAppbarNewBinding;
import com.iitp.iitp_demo.model.UserDataVo;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseActivity extends AppCompatActivity{

    /**
     * title 제목
     */
    private TextView titleView;
    static String activityName = null;


    /************************************************************************************************************************
     * Activity Life Cycle
     ************************************************************************************************************************/

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }


    /**
     * Setting Activity Contents View and Toolbar
     *
     * @param activity      Child Activity
     * @param Layout        Child Activiry ContentView
     * @param homeAsUp      ToolBar Option
     * @param titleResource Title String Address
     */
    public void setActivityView(Activity activity, int Layout, boolean homeAsUp, boolean showTitle, boolean showHome, int titleResource){
        PrintLog.e("activity name : " + activity.getLocalClassName());
        activityName = activity.getLocalClassName();
        View view = LayoutInflater.from(activity).inflate(Layout, null);
        activity.setContentView(view);

        Toolbar toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(showTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(homeAsUp);
            getSupportActionBar().setDisplayShowHomeEnabled(showHome);
            titleView = findViewById(R.id.appbar_title);
            if(titleResource != -1){
                titleView.setText(titleResource);
            }
        }


    }

    // Views
    LayoutAppbarNewBinding appBarBinding;

    public void setActionBarSet(LayoutAppbarNewBinding appBarBinding, String title, boolean showBackButton){
        this.appBarBinding = appBarBinding;
        setSupportActionBar(appBarBinding.appbar);
        appBarBinding.back.setOnClickListener(v -> onBackPressed());
        appBarBinding.back.setVisibility(showBackButton ? View.VISIBLE : View.GONE);
        appBarBinding.appbarTitle.setText(title);
    }

    public void setTitleText(int titleResource){
        titleView.setText(titleResource);
    }

    public void setTitleText(CharSequence text){
        titleView.setText(text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public List<UserDataVo> getUserData(Context ctx){
        Gson gson = new Gson();
        CommonPreference pref = CommonPreference.getInstance(ctx);
        List<UserDataVo> list = new ArrayList<>();
        String userData = pref.getStringValue(Constants.USER_DATA, null);
        if(userData != null){
            list = gson.fromJson(userData, new TypeToken<List<UserDataVo>>(){
            }.getType());
        }

        return list;
    }

}
