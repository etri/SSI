package com.iitp.iitp_demo.activity.setting;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.model.CredentialListVo;
import com.iitp.iitp_demo.api.AgentAPI;
import com.iitp.iitp_demo.api.VaultAPI;
import com.iitp.iitp_demo.api.model.pushResponseVo;
import com.iitp.iitp_demo.databinding.ActivitySettingSecureBinding;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ViewUtils;

import java.util.ArrayList;

public class SettingResetActivity extends BaseActivity{


    private ActivitySettingSecureBinding layout;
    private ArrayList<CredentialListVo> listItem = new ArrayList<CredentialListVo>();
    private PreferenceUtil preferenceUtil;
    private int type;
    private VaultAPI vaultAPI = VaultAPI.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_setting_secure);
        setActionBarSet(layout.toolbar, "초기화", true);
        preferenceUtil = PreferenceUtil.getInstance(SettingResetActivity.this);
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

    /**
     * init view
     */
    private void initView(){
        addListItem();
        SettingMenuListAdapter adapter = new SettingMenuListAdapter();
        layout.menuList.setAdapter(adapter);
    }

    /**
     * add list item
     */
    private void addListItem(){
        listItem.add(new CredentialListVo(R.drawable.ic_settings_reset, "친구목록 초기화", "친구목록 초기화", null, 1));
        listItem.add(new CredentialListVo(R.drawable.ic_settings_reset, "VC/DID 초기화", "VC/DID 초기화", null, 2));
    }

    /**
     * dialog
     */
    private void showDialogReset(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom2, null);
        AlertDialog customDialog = new AlertDialog.Builder(SettingResetActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(SettingResetActivity.this, 300);
        float height = ViewUtils.dp2px(SettingResetActivity.this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btPositive = dialogView.findViewById(R.id.positive);
        Button btNegative = dialogView.findViewById(R.id.negative);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        twoBtn.setVisibility(View.VISIBLE);
        oneBtn.setVisibility(View.GONE);
        title.setText("초기화");
        btPositive.setText(R.string.ok);
        btNegative.setText(R.string.cancel);
        if(type == 0){
            textview.setText(getString(R.string.setting_friend_reset));
        }else if(type == 1){
            textview.setText(getString(R.string.setting_reset));
        }
        btPositive.setOnClickListener(v -> {
            if(type == 0){
                resetFriends();
                customDialog.dismiss();
            }else if(type == 1){
                preferenceUtil.resetData();
                customDialog.dismiss();
                showDialogFinish();
            }
        });
        btNegative.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                customDialog.dismiss();

            }
        });
        customDialog.show();
    }

    private void showDialogFinish(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog customDialog = new AlertDialog.Builder(SettingResetActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(SettingResetActivity.this, 300);
        float height = ViewUtils.dp2px(SettingResetActivity.this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btPositive = dialogView.findViewById(R.id.cancel1);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        oneBtn.setVisibility(View.VISIBLE);
        twoBtn.setVisibility(View.INVISIBLE);
        title.setText("초기화");
        btPositive.setText(R.string.ok);
        if(type == 0){
            textview.setText("친구목록 초기화가  완료되었습니다.");
        }else if(type == 1){
            textview.setText("초기화가 완료되었습니다.");
        }

        btPositive.setOnClickListener(v -> {
            customDialog.dismiss();
        });
        customDialog.show();
    }

    private void resetFriends(){
        PrintLog.e("reset friends");
        MutableLiveData<pushResponseVo> response = new MutableLiveData<>();
        response.observe(this, pushAPIResponseVo -> {
            if(pushAPIResponseVo != null){
                showDialogFinish();
            }else{
//                        networkErrorDialog(SplashActivity.this);
            }
        });
        AgentAPI.getInstance().requestResetFriendsList(response);
    }



    /**
     * list adapter
     */
    class SettingMenuListAdapter extends BaseAdapter{

        SettingMenuListAdapter(){

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
                    if(i == 0){
                        type = 0;
                    }else{
                        type = 1;
                    }
                    showDialogReset();
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
