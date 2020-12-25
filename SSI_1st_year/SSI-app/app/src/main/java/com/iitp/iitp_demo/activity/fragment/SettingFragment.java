package com.iitp.iitp_demo.activity.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.SelectChainActivity;
import com.iitp.iitp_demo.activity.SelectDidActivity;
import com.iitp.iitp_demo.activity.UserListActivity;
import com.iitp.iitp_demo.databinding.FragmentSettingBinding;


//import com.bumptech.glide.Glide;

/**
 * Identity 의 메인화면
 */

public class SettingFragment extends Fragment{

    FragmentSettingBinding binding;

    public static SettingFragment newInstance(){
        Bundle args = new Bundle();

        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false);

        binding.UserListBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                clickUserList();
            }
        });

        binding.selectBlockchainBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                clickSelectChain();
            }
        });
        binding.generateDidBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                clickCreateId();
            }
        });
        return binding.getRoot();
    }

    public void clickUserList(){
        Intent intent = new Intent(getActivity(), UserListActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    public void clickSelectChain(){
        Intent intent = new Intent(getActivity(), SelectChainActivity.class);
        startActivity(intent);
    }

    public void clickCreateId(){
        Intent intent = new Intent(getActivity(), SelectDidActivity.class);
        startActivity(intent);
    }
}


