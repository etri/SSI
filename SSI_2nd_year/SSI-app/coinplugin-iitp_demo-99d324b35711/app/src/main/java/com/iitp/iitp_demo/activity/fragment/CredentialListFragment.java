package com.iitp.iitp_demo.activity.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.credential.CredentialListActivity;
import com.iitp.iitp_demo.activity.credential.CredentialUniListActivity;
import com.iitp.iitp_demo.activity.model.CredentialListVo;
import com.iitp.iitp_demo.api.model.pushResponseVo;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.databinding.FragmentCredentailListBinding;
import com.iitp.iitp_demo.util.BusProvider;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.iitp.iitp_demo.Constants.DELEGATOR_TOKEN_SET;
import static com.iitp.iitp_demo.Constants.DID;
import static com.iitp.iitp_demo.chain.VCVPCreater.DELEGATOR_VC;


public class CredentialListFragment extends Fragment{

    public static final String GoverType = "GoverType";
    public static final String CardTokenType = "CardTokenType";
    public static final String MobileType = "MobileType";
    public static final String PostType = "PostType";
    public static final String StockType = "StockType";
    public static final String UniType = "UniType";
    public static final String ProductType = "ProductType";
    public static final String CompanyType = "CompanyType";

    private FragmentCredentailListBinding binding;
    private ArrayList<CredentialListVo> listItem = new ArrayList<>();
    //    private MutableLiveData<ArrayList<CredentialListVo>> listItem1 = new MutableLiveData<>();
    private CredentialListAdapter adapter;
    //    private Bus bus;
    private PreferenceUtil preferenceUtil;

    public CredentialListFragment(){
        // Required empty public constructor
    }


    public static CredentialListFragment newInstance(){
        Bundle args = new Bundle();
        CredentialListFragment fragment = new CredentialListFragment();
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
        BusProvider.getInstance().register(this);
        preferenceUtil = PreferenceUtil.getInstance(getContext());
        Indy indy = Indy.getInstance(getContext());
        new Thread(indy::createWallet).start();
        initView();
        return binding.getRoot();
    }

    @Override
    public void onResume(){
        PrintLog.e("onResume");
        initView();
        super.onResume();
    }

    @Override
    public void onDestroy(){
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    /**
     * inti view
     */
    private void initView(){
        addListItem();
        adapter = new CredentialListAdapter();
        binding.credentialList.setAdapter(adapter);
    }

    /**
     * list 생
     */
    private void addListItem(){
        List<String> list = preferenceUtil.getAllVCData();
        listItem.clear();
        if(list.size() != 0){
            for(String jwt : list){
                String payload;
                if(jwt.contains("{")){
                    pushResponseVo data = IITPApplication.gson.fromJson(jwt, pushResponseVo.class);
                    String temp = data.getPoaVc();
                    payload = preferenceUtil.getPayload(temp);
                }else{
                    payload = preferenceUtil.getPayload(jwt);
                }
                JWTClaimsSet jwtClaimsSet = null;
                try{
                    jwtClaimsSet = JWTClaimsSet.parse(payload);
                }catch(ParseException e){
                    PrintLog.e("addListItem error");
                }
                MetadiumVerifier verifierTemp = new MetadiumVerifier();
                VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
                List<String> typeList = new ArrayList<>(credential.getTypes());
                String type = typeList.get(1);
                PrintLog.e("type = " + type);
                switch(type){
                    case "IdentificationCredential":
                        listItem.add(new CredentialListVo(R.drawable.ic_list_item_govern, getString(R.string.credential_tab_listItem1_desc1), getString(R.string.credential_tab_listItem1_desc2), GoverType, 1));
                        break;
                    case "CardTokenCredential":
                        boolean add = true;
                        for(CredentialListVo temp : listItem){
                            if(temp.getDid().equals(CardTokenType)){
                                add = false;
                            }
                        }
                        if(add){
                            listItem.add(new CredentialListVo(R.drawable.ic_list_item_card, getString(R.string.credential_tab_listItem2_desc1), getString(R.string.credential_tab_listItem2_desc3), CardTokenType, 2));
                        }
                        break;
                    case "AddressCredential":
                        listItem.add(new CredentialListVo(R.drawable.ic_list_item_post, getString(R.string.credential_tab_listItem3_desc1), getString(R.string.credential_tab_listItem3_desc2), PostType, 3));
                        break;
                    case "LoginCredential":
                        listItem.add(new CredentialListVo(R.drawable.market, getString(R.string.credential_tab_listItem6_desc1), getString(R.string.credential_tab_listItem6_desc2), ProductType, 6));
                        break;
                    case "StockServiceCredential":
                        listItem.add(new CredentialListVo(R.drawable.stock, getString(R.string.credential_tab_listItem7_desc1), getString(R.string.credential_tab_listItem7_desc2), StockType, 7));
                        break;
                    case "PhoneCredential":
                        listItem.add(new CredentialListVo(R.drawable.phone, getString(R.string.credential_tab_listItem8_desc1), getString(R.string.credential_tab_listItem8_desc2), MobileType, 8));
                        break;
                    case "EmployeeCredential":
                        listItem.add(new CredentialListVo(R.drawable.etri_icon, getString(R.string.credential_tab_listItem9_desc1), getString(R.string.credential_tab_listItem9_desc2), CompanyType, 5));
                        break;
                    default:
                        break;
                }


            }
            String indyDID = preferenceUtil.getIndyDID();
            if(indyDID != null){
                Indy indy = Indy.getInstance(getContext());
                String uniData = null;
                try{
                    uniData = indy.getCredentialsWorksForEmptyFilter();
//                    PrintLog.e("uniData = " + uniData);
                }catch(Exception e){
                    PrintLog.e("addListItem error");
                }
                if(uniData.contains("university")){
                    listItem.add(new CredentialListVo(R.drawable.uni, getString(R.string.credential_tab_listItem4_desc1), getString(R.string.credential_tab_listItem4_desc2), UniType, 4));
                }
                if(uniData.contains("company")){
                    boolean addlist = true;
                    for(CredentialListVo temp : listItem){
                        if(temp.getIndex() == 5){
                            addlist = false;
                        }
                    }
                    if(addlist){
                        listItem.add(new CredentialListVo(R.drawable.etri_icon, getString(R.string.credential_tab_listItem9_desc1), getString(R.string.credential_tab_listItem9_desc2), CompanyType, 5));
                    }
                }
            }
            Collections.sort(listItem, sortByIndex);
        }
    }

    private final static Comparator<CredentialListVo> sortByIndex = (o1, o2) -> Long.compare(o1.getIndex(), o2.getIndex());

    class CredentialListAdapter extends BaseAdapter{

        CredentialListAdapter(){

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
                holder.newMessage = convertView.findViewById(R.id.newMessage);
                convertView.setTag(holder);
            }else{
                holder = (CredentialListAdapter.Holder) convertView.getTag();
            }

            if(data.getDid().equals(GoverType)){
                boolean receive = CommonPreference.getInstance(getContext()).getSecureSharedPreferences().getBoolean(DELEGATOR_TOKEN_SET, false);
                if(receive){
                    holder.newMessage.setVisibility(View.VISIBLE);
                }else{
                    holder.newMessage.setVisibility(View.GONE);
                }
            }else{
                holder.newMessage.setVisibility(View.GONE);
            }
            holder.icon.setBackgroundResource(data.getImageIcon());
            holder.desc1.setText(data.getDesc1());
            holder.desc2.setText(data.getDesc2());
            holder.layout.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), CredentialListActivity.class);
                String type = GoverType;
                PrintLog.e("data.getDid() = " + data.getDid());
                switch(data.getDid()){
                    case GoverType:
                        type = GoverType;
                        break;
                    case CardTokenType:
                        type = CardTokenType;
                        break;
                    case PostType:
                        type = PostType;
                        break;
                    case UniType:
                        intent = new Intent(requireContext(), CredentialUniListActivity.class);
                        type = UniType;
                        break;
                    case CompanyType:
                        intent = new Intent(requireContext(), CredentialUniListActivity.class);
                        type = CompanyType;
                        break;
                    case DELEGATOR_VC:
                        type = DELEGATOR_VC;
                        holder.newMessage.setVisibility(View.GONE);
//                        CommonPreference.getInstance(getContext()).getSecureSharedPreferences().edit().putBoolean(DELEGATOR_TOKEN_SET, false).apply();
                        break;
                    case ProductType:
                        type = ProductType;
                        break;
                    case StockType:
                        type = StockType;
                        break;
                    case MobileType:
                        type = MobileType;
                        break;
                }
                intent.putExtra(DID, type);
                startActivity(intent);
                PrintLog.e("click : " + i);
            });
            return convertView;
        }

        /**
         * holder
         */
        class Holder{
            ImageView icon;
            TextView newMessage;
            TextView desc1;
            TextView desc2;
            ConstraintLayout layout;
        }


    }

    /**
     * event bus
     *
     * @param message "CredentialListActivity"
     */
    @Subscribe
    public void FinishLoad(String message){
        PrintLog.e("message = " + message);
        if(message.equals("CredentialListActivity") || message.equals("VCManageActivity")){
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }
    }

}


