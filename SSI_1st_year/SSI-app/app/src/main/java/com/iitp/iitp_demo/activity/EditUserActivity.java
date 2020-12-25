package com.iitp.iitp_demo.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.databinding.ActivityEditUserBinding;
import com.iitp.iitp_demo.model.UserDataVo;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.ArrayList;
import java.util.List;

public class EditUserActivity extends BaseActivity{


    private ActivityEditUserBinding layout;
    private Gson gson = new Gson();
    private Intent intent;

    public static final String USER_ADD = "add";
    public static final String USER_EDIT = "edit";
    private List<UserDataVo> userDataList;
    private int index = 0;
    private CommonPreference pref;
    private String type;

    private int userIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_edit_user);
        layout.setActivity(this);
        intent = getIntent();
        type = intent.getStringExtra(Constants.USER_TYPE);
        setSupportActionBar(layout.toolbar.appbar);
        pref = CommonPreference.getInstance(this);
        userDataList = getUserData(this);
        if(userDataList == null){
            userDataList = new ArrayList<>();
        }
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            layout.toolbar.appbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            if(type.equals(USER_ADD)){
                titleText.setText(R.string.adduser);
                layout.saveBtn.setText(R.string.adduser);
                layout.linearLayout3.setVisibility(View.GONE);
                layout.yesRipple.setVisibility(View.VISIBLE);
            }else if(type.equals(USER_EDIT)){
                titleText.setText(R.string.finish_add_user);
                UserDataVo data = (UserDataVo) intent.getSerializableExtra(Constants.USER_DATA_DETAIL);
                index = intent.getIntExtra("index", 0);
                layout.saveBtn.setText(R.string.finish_edit_user);
                layout.linearLayout3.setVisibility(View.VISIBLE);
                layout.yesRipple.setVisibility(View.GONE);
                SetData(data);
            }

        }
    }

    private void SetData(UserDataVo data){
        layout.nameEdit.setText(data.getName());
        if(data.getId().length() ==13){
            layout.idnumberEdit1.setText(data.getId().substring(0, 6));
            layout.idnumberEdit2.setText(data.getId().substring(6, 13));
        }
        layout.addressEdit.setText(data.getAddress());
    }

    public void saveData(){
        String name;
        String idNum;
        String address;

        name = layout.nameEdit.getText().toString();
        idNum = layout.idnumberEdit1.getText().toString() + layout.idnumberEdit2.getText().toString();
        address = layout.addressEdit.getText().toString();
        if(!(name.length() == 0 && idNum.length() == 0 && address.length() == 0) ){
            if(type.equals(USER_ADD)){
                userDataList.add(new UserDataVo(name, idNum, address, null));
                userIndex = userDataList.size() - 1;
            }else if(type.equals(USER_EDIT)){
                userDataList.remove(index);
                userDataList.add(index, new UserDataVo(name, idNum, address, null));
            }
            PrintLog.e("index : " + index);
            for(UserDataVo a : userDataList){
                PrintLog.e("" + a.getName());
            }
            String userData = gson.toJson(userDataList);
            PrintLog.e("user data = " + userData);
            pref.setValue(Constants.USER_DATA, userData);
            send();
        }else{
//            Toast.makeText(this, getString(R.string.dialog_no_data), Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_title))
                    .setMessage(getString(R.string.dialog_no_data))
                    .setPositiveButton(getString(R.string.yes), null);
            builder.show();
        }
    }

    public void deleteData(){
        userDataList.remove(index);
        userIndex = 0;
        String userData = gson.toJson(userDataList);
        PrintLog.e("user data = " + userData);
        pref.setValue(Constants.USER_DATA, userData);
        send();
    }

    public void click1(){

        Intent i = new Intent(EditUserActivity.this, RequestPageActivity.class);
        startActivity(i);
        finish();

    }
    public void click2(){

        Intent i = new Intent(EditUserActivity.this, MainActivity.class);
        startActivity(i);
        finish();

    }

    public void click3(){
        Intent i = new Intent(EditUserActivity.this, SettingActivity.class);
        startActivity(i);
        finish();
    }

    public void send(){
        Intent i = new Intent(EditUserActivity.this, UserListActivity.class);
        i.putExtra("userIndex",userIndex);
        startActivity(i);
        finish();
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}
