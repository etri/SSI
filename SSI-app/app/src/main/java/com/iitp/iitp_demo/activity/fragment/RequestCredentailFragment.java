package com.iitp.iitp_demo.activity.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.databinding.ActivityCheckoutCredantialBinding;
import com.iitp.iitp_demo.databinding.FragmentCheckoutCredantialBinding;


//import com.bumptech.glide.Glide;

/**
 * Identity 의 메인화면
 */

public class RequestCredentailFragment extends Fragment{

    FragmentCheckoutCredantialBinding binding;

    public static RequestCredentailFragment newInstance(){
        Bundle args = new Bundle();

        RequestCredentailFragment fragment = new RequestCredentailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_checkout_credantial, container, false);
        return binding.getRoot();
    }
}


