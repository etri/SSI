package com.iitp.iitp_demo.activity;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.model.DidVo;
import com.iitp.iitp_demo.model.UserDataVo;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseActivity extends AppCompatActivity{

    private static final String TAG = BaseActivity.class.getSimpleName();
    // Views
    /**
     * title 제목
     */
    private TextView titleView;

    private ImageView titleImage;

    private Toolbar toolbar;

    private View baseline;

    static String activityName = null;

    String didlist = null;
    int didIndex = 0;
    DidVo did = null;
    List<DidVo> didVoList = new ArrayList<>();

    /************************************************************************************************************************
     * Activity Life Cycle
     ************************************************************************************************************************/

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


    /************************************************************************************************************************
     * Child Activity Control
     ************************************************************************************************************************/
    /**
     * Setting Activity Contents View and Toolbar
     *
     * @param activity      Child Activity
     * @param Layout        Child Activiry ContentView
     * @param homeAsUp      ToolBar Option
     * @param titleResource Title String Address
     */
    public void setActivityView(Activity activity, int Layout, boolean homeAsUp, boolean showTitle, boolean showHome, int titleResource, boolean title){
        PrintLog.e("activity name : " + activity.getLocalClassName());
        activityName = activity.getLocalClassName();
        View view = LayoutInflater.from(activity).inflate(Layout, null);
        activity.setContentView(view);

        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(showTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(homeAsUp);
            getSupportActionBar().setDisplayShowHomeEnabled(showHome);
            titleImage = findViewById(R.id.appbar_title_image);
            titleView = findViewById(R.id.appbar_title);
            baseline = findViewById(R.id.appbar_bottom_line);
            if(title){
                titleImage.setVisibility(View.VISIBLE);
            }else{
                titleImage.setVisibility(View.GONE);
            }
            if(titleResource != -1){
                titleView.setText(titleResource);
            }

        }


    }

    public void setSupportActionBarShow(boolean show){
        if(show){
            getSupportActionBar().show();
        }else{
            getSupportActionBar().hide();
        }
    }

    public void setToolbarVisibility(int visible){
        toolbar.setVisibility(visible);
    }

    public void setBaselineVisibility(int visible){
        baseline.setVisibility(visible);
    }

    public void setTitleText(int titleResource){
        titleView.setText(titleResource);
    }

    public void setTitleText(CharSequence text){
        titleView.setText(text);
    }


    public String getActivityName(){
        return activityName;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private DidVo getDidData(Context ctx){
        Gson gson = new Gson();
        DidVo didData = null;

        didVoList = gson.fromJson(didlist, new TypeToken<ArrayList<DidVo>>(){
        }.getType());
        didData = didVoList.get(didIndex);
        return didData;
    }
}
