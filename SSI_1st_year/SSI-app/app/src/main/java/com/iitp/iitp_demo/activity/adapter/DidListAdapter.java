package com.iitp.iitp_demo.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.EditUserActivity;
import com.iitp.iitp_demo.activity.SelectDidActivity;
import com.iitp.iitp_demo.activity.UserListActivity;
import com.iitp.iitp_demo.databinding.DidListItemBinding;
import com.iitp.iitp_demo.model.DidVo;
import com.iitp.iitp_demo.databinding.ListItemBinding;
import com.iitp.iitp_demo.model.UserDataVo;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.ArrayList;
import java.util.List;

public class DidListAdapter extends BaseAdapter{
    public List<DidVo> didDataVos;
    private LayoutInflater mLayoutInflater;
    private Activity activity;
    private Context context;

    public DidListAdapter(Activity activity, List<DidVo> didData, LayoutInflater inflater){
        this.activity = activity;
        context = activity;
        didDataVos = didData;
        if(didDataVos == null){
            didDataVos = new ArrayList<>();
        }
        mLayoutInflater = inflater;
    }

    @Override
    public int getCount(){
        return didDataVos.size();
    }

    @Override
    public Object getItem(int position){
        return didDataVos.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        DidListItemBinding binding;
        if (convertView == null) {
            if (mLayoutInflater == null) {
                mLayoutInflater = (LayoutInflater) parent.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            binding = DataBindingUtil.inflate(
                    mLayoutInflater, R.layout.did_list_item,parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);

        }
        else {
            binding = (DidListItemBinding) convertView.getTag();
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                v.setSelected(true);
                ((SelectDidActivity)activity).didtext.set(didDataVos.get(position).getIss());
                ((SelectDidActivity)activity).index = position;
                PrintLog.e("did click : "+position);

            }
        });

//        convertView.setOnLongClickListener(new View.OnLongClickListener(){
//            @Override
//            public boolean onLongClick(View v){
//                PrintLog.e("long click : "+position);
//                DidVo data = didDataVos.get(position);
//                Intent intent = new Intent(context, EditUserActivity.class);
//
//
//                context.startActivity(intent);
//                return true;
//            }
//        });
        binding.setDidData(didDataVos.get(position));
        binding.executePendingBindings();
        return convertView;
    }

    public void updateItemList(List<DidVo> diddata){
        didDataVos = diddata;
        notifyDataSetChanged();
    }
}
