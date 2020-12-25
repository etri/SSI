package com.iitp.iitp_demo.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.EditUserActivity;
import com.iitp.iitp_demo.activity.UserListActivity;
import com.iitp.iitp_demo.databinding.ListItemBinding;
import com.iitp.iitp_demo.model.UserDataVo;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends BaseAdapter{
    public List<UserDataVo> userDataVos;
    private LayoutInflater mLayoutInflater;
    private Activity activity;
    private Context context;
    public UserListAdapter(Activity activity, List<UserDataVo> userdata, LayoutInflater inflater){
        this.activity = activity;
        context = activity;
        userDataVos = userdata;
        if(userdata == null){
            userDataVos = new ArrayList<>();
        }
        mLayoutInflater = inflater;
    }

    @Override
    public int getCount(){
        return userDataVos.size();
    }

    @Override
    public Object getItem(int position){
        return userDataVos.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ListItemBinding binding;
        if (convertView == null) {
            if (mLayoutInflater == null) {
                mLayoutInflater = (LayoutInflater) parent.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            binding = DataBindingUtil.inflate(
                    mLayoutInflater, R.layout.list_item,parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        }
        else {
            binding = (ListItemBinding) convertView.getTag();
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((UserListActivity)activity).name.set(userDataVos.get(position).getName());
                ((UserListActivity)activity).index = position;
                PrintLog.e("click : "+position);
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                PrintLog.e("long click : "+position);
                UserDataVo data = userDataVos.get(position);
                Intent intent = new Intent(context, EditUserActivity.class);
                intent.putExtra(Constants.USER_DATA_DETAIL, data);
                intent.putExtra(Constants.USER_TYPE, EditUserActivity.USER_EDIT);
                intent.putExtra("index",position);
                ((UserListActivity)activity).index = position;
                context.startActivity(intent);
                return true;
            }
        });
        binding.setUserdata(userDataVos.get(position));
        binding.executePendingBindings();
        return convertView;
    }

    public void updateItemList(List<UserDataVo> userdata){
        userDataVos = userdata;
        notifyDataSetChanged();
    }
}
