package com.iitp.iitp_demo.activity.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.PincodeActivity;
import com.iitp.iitp_demo.activity.model.CredentialListVo;
import com.iitp.iitp_demo.databinding.ActivitySettingSecureBinding;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.ArrayList;

import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;

public class SettingSecurityActivity extends BaseActivity{


    private ActivitySettingSecureBinding layout;


    ArrayList<CredentialListVo> listItem = new ArrayList<CredentialListVo>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_setting_secure);
        setActionBarSet(layout.toolbar, getString(R.string.did_setting_security), true);
        initView();
    }

    @Override
    protected void onResume(){

        super.onResume();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    private void initView(){
        addListItem();
        SettingMenuListAdapter adapter = new SettingMenuListAdapter();
        layout.menuList.setAdapter(adapter);
    }

    private void addListItem(){
        listItem.add(new CredentialListVo(R.drawable.ic_settings_safety, getString(R.string.did_setting_security_edit_pin),getString(R.string.did_setting_security_edit_pin_desc),null, 1));
        listItem.add(new CredentialListVo(R.drawable.ic_settings_finger_print, getString(R.string.did_setting_security_edit_bio),getString(R.string.did_setting_security_edit_bio_desc),null, 2));
    }


    class SettingMenuListAdapter extends BaseAdapter{

        public SettingMenuListAdapter(){

        }

        @Override
        public int getCount(){
            return listItem.size();
        }

        @Override
        public Object getItem(int i){
            return null;
        }

        @Override
        public long getItemId(int i){
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent){
            SettingMenuListAdapter.Holder holder;
            CredentialListVo data = listItem.get(i);
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_credential, parent, false);
                holder = new SettingMenuListAdapter.Holder();
                holder.icon = convertView.findViewById(R.id.icon);
                holder.desc1 = convertView.findViewById(R.id.desc1);
                holder.desc2 = convertView.findViewById(R.id.desc2);
                holder.layout = convertView.findViewById(R.id.layout);
                convertView.setTag(holder);
            }else{
                holder = (SettingMenuListAdapter.Holder) convertView.getTag();
            }


            holder.icon.setBackgroundResource(data.getImageIcon());
            holder.desc1.setText(data.getDesc1());
            holder.desc2.setText(data.getDesc2());
            holder.layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    switch(i){
                        case 0:{
                            Intent intent= new Intent(SettingSecurityActivity.this, PincodeActivity.class);
                            intent.putExtra(PIN_SETTING_TYPE, 1);
                            startActivity(intent);
                        }
                        break;
                        case 1:{
                            Intent intent= new Intent(SettingSecurityActivity.this, SettingSecurityFingerprintActivity.class);
                            startActivity(intent);

                        }
                        break;

                    }
                    PrintLog.e("click : " + i);
                }
            });
            return convertView;
        }

        /**
         * holder
         */
        class Holder{
            ImageView icon;
            TextView desc1;
            TextView desc2;
            ConstraintLayout layout;
        }


    }
}
