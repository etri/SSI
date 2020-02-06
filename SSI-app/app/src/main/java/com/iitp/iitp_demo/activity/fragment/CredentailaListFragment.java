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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.FinishGenerateIdCredentialActivity;
import com.iitp.iitp_demo.activity.FinishGenerateOfficeCredentialActivity;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.databinding.FragmentCredentailListBinding;
import com.iitp.iitp_demo.model.DidVo;
import com.iitp.iitp_demo.util.CommonPreference;

import java.util.ArrayList;
import java.util.List;

import static com.iitp.iitp_demo.Constants.CHECK_CREDENTIAL;


//import com.bumptech.glide.Glide;

/**
 * Identity 의 메인화면
 */

public class CredentailaListFragment extends Fragment{

    FragmentCredentailListBinding binding;
    private CommonPreference commPref = null;
    public String idCredential = null;
    public String officeCredential = null;



    public static CredentailaListFragment newInstance(){
        Bundle args = new Bundle();

        CredentailaListFragment fragment = new CredentailaListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_credentail_list, container, false);
        did = getDidData();
//        initView();

        return binding.getRoot();
    }

    @Override
    public void onResume(){
        did = getDidData();
        initView();
        super.onResume();
    }

    private void initView(){
        if(did != null){
            binding.hadCredential.setVisibility(View.GONE);
            idCredential = did.getIdCredential();
            officeCredential = did.getOfficeCredential();

            if(idCredential == null){
                binding.credential1.setVisibility(View.INVISIBLE);
            }else{
                binding.credential1.setVisibility(View.VISIBLE);
                binding.credential1.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        credential1Click();
                    }
                });
            }

            if(officeCredential == null){
                binding.credential2.setVisibility(View.INVISIBLE);
            }else{
                binding.credential2.setVisibility(View.VISIBLE);
                binding.credential2.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        credential2Click();
                    }
                });
            }
            if(idCredential == null && officeCredential == null){
                binding.hadCredential.setVisibility(View.VISIBLE);
            }else{
                binding.hadCredential.setVisibility(View.INVISIBLE);
            }

        }else{
            binding.hadCredential.setVisibility(View.VISIBLE);
        }
    }

    String didlist = null;
    int didIndex = 0;
    DidVo did = null;
    private Gson gson = new Gson();
    List<DidVo> didVoList = new ArrayList<>();

    private DidVo getDidData(){
        commPref = CommonPreference.getInstance(getActivity());
        DidVo didData = null;
        didlist = commPref.getStringValue(Constants.ALL_DID_DATA, null);
        didIndex = commPref.getIntValue(Constants.DID_DATA_INDEX, 0);
        didVoList = gson.fromJson(didlist, new TypeToken<ArrayList<DidVo>>(){
        }.getType());
        didData = didVoList.get(didIndex);
        return didData;
    }

    public void credential1Click(){
        Intent intent = new Intent(getActivity(), FinishGenerateIdCredentialActivity.class);
        intent.putExtra(CHECK_CREDENTIAL, "check");
        startActivity(intent);
//        getActivity().finish();
    }


    public void credential2Click(){
        Intent intent = new Intent(getActivity(), FinishGenerateOfficeCredentialActivity.class);
        intent.putExtra(CHECK_CREDENTIAL, "check");
        startActivity(intent);
//        getActivity().finish();
    }
}


