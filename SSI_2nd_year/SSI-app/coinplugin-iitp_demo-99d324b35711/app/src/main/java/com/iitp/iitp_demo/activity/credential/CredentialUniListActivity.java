package com.iitp.iitp_demo.activity.credential;

import android.content.Context;
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

import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.model.IndyCredentialVo;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.databinding.ActivityCredentailListBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.iitp.iitp_demo.Constants.DID;
import static com.iitp.iitp_demo.Constants.JWT_DATA;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.CompanyType;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.UniType;
import static com.iitp.iitp_demo.chain.VCVPCreater.OfficeCredential;

public class CredentialUniListActivity extends BaseActivity{

    private ActivityCredentailListBinding layout;

    private String type = null;
    //    private ArrayList<String> jwtData = new ArrayList<>();
    private List<IndyCredentialVo> listData = new ArrayList<>();
    private PreferenceUtil preferenceUtil;
    private CredentialListAdapter adapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        type = intent.getStringExtra(DID);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_credentail_list);
        preferenceUtil = PreferenceUtil.getInstance(this);
        setTitle();
        adapter = new CredentialListAdapter();
        layout.back.setOnClickListener(v -> onBackPressed());

    }

    @Override
    protected void onResume(){
        listData.clear();
        PrintLog.e("type = " + type);
        PrintLog.e("onResume()");

        Indy indy = Indy.getInstance(CredentialUniListActivity.this);
        ArrayList<IndyCredentialVo> indyCredentialVos;
        String indyData;
        try{
            indyData = indy.getCredentialsWorksForEmptyFilter();
            PrintLog.e("indyData = " + indyData);
            indyCredentialVos = IITPApplication.gson.fromJson(indyData, new TypeToken<ArrayList<IndyCredentialVo>>(){
            }.getType());
            int size = indyCredentialVos.size();
            PrintLog.e("size = " + size);
            if(type.equals(UniType)){
                if(size != 0){
                    setUniVCData(indyCredentialVos);
                }else{
                    listData.clear();
                    adapter.notifyDataSetChanged();
                }
            }else if(type.equals(CompanyType)){
                boolean isIndy = false;
                for(IndyCredentialVo temp : indyCredentialVos){
                    if(temp.getSchema_id().contains("company")){
                        isIndy = true;
                    }
                }
                if(isIndy){
                    setUniVCData(indyCredentialVos);
                }else{
                    setJobVCData();
                }
            }

        }catch(Exception e){
          PrintLog.e("onResume errror");
        }
        super.onResume();
    }


    private void setUniVCData(ArrayList<IndyCredentialVo> uniVCData){
//        List<IndyCredentialVo> companyData = new ArrayList<>();
        for(IndyCredentialVo temp : uniVCData){
            if(type.equals(UniType)){
                if(temp.getSchema_id().contains("university")){
                    listData.add(temp);
                }
            }else if(type.equals(CompanyType)){
                if(temp.getSchema_id().contains("company")){
                    listData.add(temp);
                }
            }
        }

        layout.credentialList.setAdapter(adapter);
    }

    private void setJobVCData(){
        String vc = CommonPreference.getInstance(CredentialUniListActivity.this).getSecureSharedPreferences().getString(OfficeCredential, null);
        PrintLog.e("VC = " + vc);
        MetadiumVerifier verifierTemp = new MetadiumVerifier();
        String payload;
        payload = preferenceUtil.getPayload(vc);
        JWTClaimsSet jwtClaimsSet = null;
        try{
            jwtClaimsSet = JWTClaimsSet.parse(payload);
        }catch(ParseException e){
            PrintLog.e("setJobVCData errror");
        }
        VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
        String issuer = credential.getIssuer().toString();
        List<String> listTypes = new ArrayList<>(credential.getTypes());
        PrintLog.e("type = " + listTypes.get(1));
        String credentialType = listTypes.get(1);

        PrintLog.e("issuer = " + issuer);
        Map<String, String> claims = (Map<String, String>) credential.getCredentialSubject();
        IndyCredentialVo temp = new IndyCredentialVo("", issuer, "", "", "", claims);
        listData.add(temp);
        CredentialListAdapter adapter = new CredentialListAdapter();
        layout.credentialList.setAdapter(adapter);
    }

    /**
     * title 설정
     *
     * @param did did
     */
    private void setTitle(){
        String title1 = "대학 컨소시엄";
        String title2 = "ETRI";
        if(type.equals(UniType)){
            layout.appbarTitle.setText(title1);
            layout.icon.setBackgroundResource(R.drawable.uni);
        }else if(type.equals(CompanyType)){
            layout.appbarTitle.setText(title2);
            layout.icon.setBackgroundResource(R.drawable.etri_icon);
        }
    }


    /**
     * list adapter
     */
    class CredentialListAdapter extends BaseAdapter{

        CredentialListAdapter(){

        }

        @Override
        public int getCount(){
            return listData.size();
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
            IndyCredentialVo data = listData.get(i);
            Map<String, String> attr = data.getAttr();
            String desc = "";
            String name = null;
            String birth = null;
            String school = null;
            String number = null;
            String grduation = null;
            String corpid = null;
            String issuancedate = null;
            String groupname = null;
            String title1 = "졸업증명VC";
            String title2 = "사원증VC";
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_credential_2, parent, false);
                holder = new CredentialListAdapter.Holder();
                holder.icon = convertView.findViewById(R.id.icon);
                holder.desc1 = convertView.findViewById(R.id.desc1);
                holder.desc2 = convertView.findViewById(R.id.desc2);
                holder.layout = convertView.findViewById(R.id.layout);
                holder.newMessage = convertView.findViewById(R.id.newMessage);
                convertView.setTag(holder);
            }else{
                holder = (CredentialListAdapter.Holder) convertView.getTag();
            }
            PrintLog.e("data.getDid() = " + data.getSchema_id());
            for(String key : attr.keySet()){
                PrintLog.e(key + " : " + attr.get(key));
                if(!key.equals("id")){
                    if(data.getSchema_id().contains("university")){
                        switch(key){
                            case "name":
                                name = attr.get(key);
                                break;
                            case "number":
                                number = attr.get(key);
                                break;
                            case "collage_name":
                                school = attr.get(key);
                                break;
                            case "birth_date":
                                birth = attr.get(key);
                                break;
                            case "graduation_date":
                                grduation = attr.get(key);
                                break;
                            default:
                                break;
                        }
                    }else if(data.getSchema_id().contains("company")){
                        switch(key){
                            case "name":
                                name = attr.get(key);
                                break;
                            case "corp-id":
                                corpid = attr.get(key);
                                break;
                            case "issuancedate":
                                issuancedate = attr.get(key);
                                break;
                            case "group_name":
                                groupname = attr.get(key);
                                break;
                            default:
                                break;
                        }
                    }else if(data.getSchema_id().contains("meta") || data.getSchema_id().contains("icon")){
                        switch(key){
                            case "name":
                                name = attr.get(key);
                                break;
                            case "corp-id":
                                corpid = attr.get(key);
                                break;
                            case "issuancedate":
                                issuancedate = attr.get(key);
                                break;
                            case "group_name":
                                groupname = attr.get(key);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            if(data.getSchema_id().contains("university")){
                desc = name + ", " + birth + ", " + school + ", " + number + ", " + grduation;
            }else if(data.getSchema_id().contains("company")){
                desc = name + ", " + groupname + ", " + corpid + ", " + issuancedate;
            }else if(data.getSchema_id().contains("meta") || data.getSchema_id().contains("icon")){
                desc = name + ", " + groupname + ", " + corpid + ", " + issuancedate;
            }
            PrintLog.e("desc = " + desc);
            holder.desc2.setText(desc);
            holder.layout.setOnClickListener(v -> {
                Intent intent = new Intent(CredentialUniListActivity.this, CredentialUniListDetailActivity.class);
                String attr1 = IITPApplication.gson.toJson(data);
                intent.putExtra(JWT_DATA, attr1);
                intent.putExtra(DID, type);
                startActivity(intent);
            });
            if(data.getSchema_id().contains("university")){
                holder.icon.setBackgroundResource(R.drawable.uni);
                holder.desc1.setText(title1);
            }else if(data.getSchema_id().contains("company")){
                holder.icon.setBackgroundResource(R.drawable.etri_icon);
                holder.desc1.setText(title2);
            }else if(data.getSchema_id().contains("meta") || data.getSchema_id().contains("icon")){
                holder.icon.setBackgroundResource(R.drawable.etri_icon);
                holder.desc1.setText(title2);
            }
            return convertView;
        }

        /**
         * holder
         */
        class Holder{
            ImageView icon;
            TextView desc1;
            TextView desc2;
            TextView newMessage;
            ConstraintLayout layout;
        }
    }
}

