package com.iitp.iitp_demo.activity;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableInt;

import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.databinding.ActivitySelectChainBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;

public class SelectChainActivity extends BaseActivity{

    ActivitySelectChainBinding layout;
    public ObservableInt radioCheck = new ObservableInt();
    private CommonPreference commPref;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_select_chain);
        layout.setActivity(this);
        setSupportActionBar(layout.toolbar.appbar);
        commPref = CommonPreference.getInstance(this);
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            layout.toolbar.appbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            titleText.setText(R.string.select_chain);

        }
        setCheck();

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

    public void onClickRadioBtn(){

        PrintLog.e("index= " + radioCheck);
//        finish();
    }

    public void onSplitTypeChanged(RadioGroup radioGroup, int id){
        commPref.setValue(Constants.BLOCKCHAIN_CHECK, id);
        PrintLog.e("index = " + id);
    }

    private void setCheck(){
        int index = commPref.getIntValue(Constants.BLOCKCHAIN_CHECK, R.id.rg_btn1);
        radioCheck.set(index);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}
