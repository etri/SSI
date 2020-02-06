package com.iitp.iitp_demo.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.adapter.UserListAdapter;
import com.iitp.iitp_demo.databinding.ActivityUserlistBinding;
import com.iitp.iitp_demo.model.UserDataVo;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;

public class UserListActivity extends BaseActivity{

    private ActivityUserlistBinding layout;
    public ObservableArrayList<UserDataVo> userDataList = new ObservableArrayList<>();
    private CommonPreference pref;
    private UserListAdapter adapter;
    private Gson gson = new Gson();
    public ObservableField<String> name = new ObservableField<>();
    public int index;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_userlist);
        layout.setActivity(this);
        pref = CommonPreference.getInstance(this);
        userDataList = getData();
        setSupportActionBar(layout.toolbar.appbar);
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            layout.toolbar.appbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            titleText.setText(R.string.userlist);

        }
        if(getIntent() != null){
            index = getIntent().getIntExtra("userIndex",0);
        }
        adapter = new UserListAdapter(this, userDataList, getLayoutInflater());
        layout.userList.setAdapter(adapter);

        if(userDataList.size() == 0){
            addClickButton();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        PrintLog.e("onResume");
        userDataList.clear();
        userDataList = getData();
        adapter.updateItemList(userDataList);
        if(userDataList.size() > 0){
            name.set(userDataList.get(index).getName());
        }
        pref.setValue(Constants.USER_DATA_INDEX, index);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        PrintLog.e("onDestroy");
        pref.setValue(Constants.USER_DATA_INDEX, index);
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

    public void addClickButton(){
        Intent intent = new Intent(UserListActivity.this, EditUserActivity.class);
        intent.putExtra(Constants.USER_TYPE, EditUserActivity.USER_ADD);
        startActivity(intent);
        finish();
    }

    private ObservableArrayList<UserDataVo> getData(){

        ObservableArrayList<UserDataVo> list = new ObservableArrayList<>();
        String userData = pref.getStringValue(Constants.USER_DATA, null);
        index = pref.getIntValue(Constants.USER_DATA_INDEX, 0);

        if(userData != null && !userData.equals("[]")){
            list = gson.fromJson(userData, new TypeToken<ObservableArrayList<UserDataVo>>(){
            }.getType());
            name.set(list.get(index).getName());
        }


        return list;
    }

    public void click1(){

        Intent i = new Intent(UserListActivity.this, RequestPageActivity.class);
        startActivity(i);
        finish();

    }

    public void click2(){

        Intent i = new Intent(UserListActivity.this, MainActivity.class);
        startActivity(i);
        finish();

    }

    public void click3(){
        Intent i = new Intent(UserListActivity.this, SettingActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}
