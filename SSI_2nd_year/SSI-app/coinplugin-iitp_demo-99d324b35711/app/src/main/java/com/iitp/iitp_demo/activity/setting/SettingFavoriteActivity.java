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
import com.iitp.iitp_demo.activity.WebViewActivity;
import com.iitp.iitp_demo.activity.WebViewJobActivity;
import com.iitp.iitp_demo.activity.WebViewUniActivity;
import com.iitp.iitp_demo.activity.model.CredentialListVo;
import com.iitp.iitp_demo.databinding.ActivitySettingSecureBinding;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.ArrayList;

public class SettingFavoriteActivity extends BaseActivity{


    private ActivitySettingSecureBinding layout;
    private ArrayList<CredentialListVo> listItem = new ArrayList<CredentialListVo>();



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_setting_secure);
        setActionBarSet(layout.toolbar, getString(R.string.dids_tab_favorite), true);
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
        listItem.add(new CredentialListVo(R.drawable.ic_settings_vc, "대학", "", null, 1));
        listItem.add(new CredentialListVo(R.drawable.ic_settings_vc, "채용사","", null, 2));
        listItem.add(new CredentialListVo(R.drawable.ic_settings_vc, "포털","", null, 3));
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
                    switch(i){
                        case 0:{
                            Intent intent = new Intent(SettingFavoriteActivity.this,  WebViewUniActivity.class);
                            intent.putExtra("url","http://129.254.194.112:9003/");
                            startActivity(intent);
                        }
                        break;
                        case 1:{
                            Intent intent = new Intent(SettingFavoriteActivity.this,  WebViewJobActivity.class);
                            intent.putExtra("url","http://129.254.194.112:9002/");
                            startActivity(intent);
                        }
                        break;
                        case 2:{
                            Intent intent = new Intent(SettingFavoriteActivity.this,  WebViewActivity.class);
                            intent.putExtra("url","http://129.254.194.112:9004/");
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
