package com.iitp.iitp_demo.activity.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.dids.DidAddActivity;
import com.iitp.iitp_demo.activity.dids.DidManageActivity;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.databinding.FragmentDidsBinding;
import com.iitp.iitp_demo.util.BusProvider;
import com.iitp.iitp_demo.util.PrintLog;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import static com.iitp.iitp_demo.Constants.DID_INDEX;
import static com.iitp.iitp_demo.Constants.DID_NAME;


//import com.bumptech.glide.Glide;

/**
 * Identity 의 메인화면
 */

public class DidsFragment extends Fragment{

    private FragmentDidsBinding binding;
    private ArrayList<DidDataVo> didList = new ArrayList<>();
    private DidListAdapter adapter;
    private int defaultIndex = 0;
    private Bus bus;

    public DidsFragment(){
        // Required empty public constructor
    }

    public static DidsFragment newInstance(){
        Bundle args = new Bundle();
        DidsFragment fragment = new DidsFragment();
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dids, container, false);
        setHasOptionsMenu(true);

        didList = MainActivity.getDidList(requireContext());
        BusProvider.getInstance().register(this);
        initView();
        binding.searchView.setIconifiedByDefault(false);
        //여기서 검색하기
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query){
                didList = MainActivity.serchDidList(requireContext(), query);
                adapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText){
                if(newText.isEmpty() || newText == ""){
                    didList = MainActivity.getDidList(requireContext());
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragmet_did, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.did_add:
                startActivity(new Intent(requireContext(), DidAddActivity.class));
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onResume(){
        PrintLog.e("onResume");
        if(adapter != null){
            didList = MainActivity.getDidList(requireContext());
            adapter.notifyDataSetChanged();
            setDefault();
        }
        super.onResume();
    }

    private void initView(){
        adapter = new DidListAdapter();
        binding.didList.setAdapter(adapter);
        setDefault();

        binding.useBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(requireContext(), DidManageActivity.class);
                intent.putExtra(DID_INDEX, defaultIndex);
                String didname = null;
                for(DidDataVo did : didList){
                    if(did.getFavorite()){
                        didname = did.getDid();
                        break;
                    }
                }
                intent.putExtra(DID_NAME, didname);
                startActivity(intent);
            }
        });
    }

    private void setDefault(){

        int i = 0;
        for(DidDataVo temp : didList){
            PrintLog.e("getFavorite = " + temp.getFavorite());
            if(temp.getFavorite()){
                binding.favoriteItemLayout.setVisibility(View.VISIBLE);
                binding.nickname.setText(temp.getNickName());
                binding.blockChain.setText(temp.getBlackChain().toString());
                defaultIndex = i;
                break;
            }
            i++;
            binding.favoriteItemLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe
    public void FinishLoad(String message){
        PrintLog.e("message = " + message);
        if(message.equals("DidManageActivity")){
            if(adapter != null){
                didList = MainActivity.getDidList(requireContext());
                adapter.notifyDataSetChanged();
                setDefault();
            }
        }
    }


    class DidListAdapter extends BaseAdapter{

        public DidListAdapter(){

        }

        @Override
        public int getCount(){
            return didList.size();
        }

        @Override
        public DidDataVo getItem(int i){
            return null;
        }

        @Override
        public long getItemId(int i){
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent){
            DidListAdapter.Holder holder;
            DidDataVo data = didList.get(i);
//            PrintLog.e("index = " + i);
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_did, parent, false);
                holder = new DidListAdapter.Holder();
                holder.nickName = convertView.findViewById(R.id.nickname);
                holder.chainName = convertView.findViewById(R.id.blockChain);
                holder.button = convertView.findViewById(R.id.useBtn);
                holder.did = data.getDid();
                convertView.setTag(holder);
            }else{
                holder = (DidListAdapter.Holder) convertView.getTag();
            }
            holder.nickName.setText(data.getNickName());
            holder.chainName.setText(data.getBlackChain().toString());
            holder.button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent intent = new Intent(requireContext(), DidManageActivity.class);
                    intent.putExtra(DID_NAME, data.getDid());
                    startActivity(intent);
                }
            });
            return convertView;
        }

        /**
         * holder
         */
        public class Holder{
            TextView nickName;
            TextView chainName;
            Button button;
            String did;
        }
    }


}


