package com.iitp.iitp_demo.activity.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.FriendsListActivity;
import com.iitp.iitp_demo.activity.model.CredentialListVo;
import com.iitp.iitp_demo.activity.setting.SettingAddPreVCDataActivity;
import com.iitp.iitp_demo.activity.setting.SettingBackupActivity;
import com.iitp.iitp_demo.activity.setting.SettingFavoriteActivity;
import com.iitp.iitp_demo.activity.setting.SettingResetActivity;
import com.iitp.iitp_demo.activity.setting.SettingSecurityActivity;
import com.iitp.iitp_demo.databinding.FragmentCredentailListBinding;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ViewUtils;

import java.util.ArrayList;


/**
 * Identity 의 메인화면
 */

public class SettingFragment extends Fragment{

    private FragmentCredentailListBinding binding;
    private ArrayList<CredentialListVo> listItem = new ArrayList<CredentialListVo>();
    private PreferenceUtil preferenceUtil;
    public SettingFragment(){
        // Required empty public constructor
    }


    public static SettingFragment newInstance(){
        Bundle args = new Bundle();
        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_credentail_list, container, false);
        setHasOptionsMenu(true);
        binding.idListLayout.setVisibility(View.GONE);
        preferenceUtil = PreferenceUtil.getInstance(getContext());
        initView();
        return binding.getRoot();
    }

    @Override
    public void onResume(){

        super.onResume();
    }

    /**
     * initview
     */
    private void initView(){
        addListItem();
        CredentialListAdapter adapter = new CredentialListAdapter();
        binding.credentialList.setAdapter(adapter);
    }

    /**
     * add list item
     */
    private void addListItem(){
        listItem.clear();
        listItem.add(new CredentialListVo(R.drawable.ic_settings_friends, getString(R.string.did_setting_friends_list), getString(R.string.did_setting_friends_list_1), null, 1));
        listItem.add(new CredentialListVo(R.drawable.ic_settings_backup, getString(R.string.did_setting_backup), getString(R.string.did_setting_backup_1), null, 2));
        listItem.add(new CredentialListVo(R.drawable.ic_settings_reset, getString(R.string.did_setting_reset), getString(R.string.did_setting_reset_1), null, 3));
        listItem.add(new CredentialListVo(R.drawable.ic_settings_safety, getString(R.string.did_setting_security), getString(R.string.did_setting_security_1), null, 4));
        listItem.add(new CredentialListVo(R.drawable.ic_settings_vc, getString(R.string.did_setting_pre), getString(R.string.did_setting_pre1), null, 5));
        listItem.add(new CredentialListVo(R.drawable.ic_settings_vc, getString(R.string.did_setting_fav), "대학, 채용사, 포털", null, 6));
    }



    private void showDialogFinish(){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog customDialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(getActivity(), 300);
        float height = ViewUtils.dp2px(getActivity(), 252);
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
        textview.setText("초기화 완료 하였습니다.");
        btPositive.setOnClickListener(v -> {
            customDialog.dismiss();
        });
        customDialog.show();
    }


    /**
     * list adapter
     */
    class CredentialListAdapter extends BaseAdapter{

        public CredentialListAdapter(){

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
            CredentialListAdapter.Holder holder;
            CredentialListVo data = listItem.get(i);
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_credential, parent, false);
                holder = new CredentialListAdapter.Holder();
                holder.icon = convertView.findViewById(R.id.icon);
                holder.desc1 = convertView.findViewById(R.id.desc1);
                holder.desc2 = convertView.findViewById(R.id.desc2);
                holder.layout = convertView.findViewById(R.id.layout);
                convertView.setTag(holder);
            }else{
                holder = (CredentialListAdapter.Holder) convertView.getTag();
            }


            holder.icon.setBackgroundResource(data.getImageIcon());
            holder.desc1.setText(data.getDesc1());
            holder.desc2.setText(data.getDesc2());
            holder.layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    switch(i){
                        case 0:{
                            startActivity(new Intent(requireContext(), FriendsListActivity.class));
                        }
                        break;
                        case 1:{
                            startActivity(new Intent(requireContext(), SettingBackupActivity.class));
                        }
                        break;
                        case 2:{
                            startActivity(new Intent(requireContext(), SettingResetActivity.class));
                        }
                        break;
                        case 3:{
                            startActivity(new Intent(requireContext(), SettingSecurityActivity.class));
                        }
                        break;
                        case 4:{
                            startActivity(new Intent(requireContext(), SettingAddPreVCDataActivity.class));
                        }
                        break;
                        case 5:{
                            startActivity(new Intent(requireContext(), SettingFavoriteActivity.class));
//
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


