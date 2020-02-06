package com.iitp.iitp_demo.activity.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.databinding.FragmentCheckoutCredantialBinding;
import com.iitp.iitp_demo.databinding.FragmentRequestPageBinding;


//import com.bumptech.glide.Glide;

/**
 * Identity 의 메인화면
 */

public class RequestPageFragment extends Fragment{

    FragmentRequestPageBinding binding;

    public static RequestPageFragment newInstance(){
        Bundle args = new Bundle();

        RequestPageFragment fragment = new RequestPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_request_page, container, false);
        binding.setFragment(this);
        binding.link1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                requestIdBtn();
            }
        });

        binding.link2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                requestOfficeBtn();
            }
        });
        return binding.getRoot();
    }


    public void requestIdBtn(){
        String url = "http://129.254.194.112/mreq/index.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }

    public void requestOfficeBtn(){
        String url = "http://129.254.194.112/ereq/index.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}


