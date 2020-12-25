package com.iitp.iitp_demo.activity.dids;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.databinding.ActivityDidDeatilBinding;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.TimeConvert;

import java.util.ArrayList;

import static com.iitp.iitp_demo.Constants.DID_INDEX;

public class DidDetailActivity extends BaseActivity{

    private ActivityDidDeatilBinding layout;
    private int index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_did_deatil);
        setSupportActionBar(layout.toolbar.appbar);
        setActionBarSet(layout.toolbar,getString(R.string.did_detail_title),true);
        Intent intent = getIntent();
        index = intent.getIntExtra(DID_INDEX, 0);
        init();
    }

    private void init(){
        ArrayList<DidDataVo> didList = MainActivity.getDidList(DidDetailActivity.this);
        DidDataVo data = didList.get(index);
        layout.nickname.setText(data.getNickName());
        layout.publicKey.setText(data.getPublicKey());
        layout.did.setText(data.getDid());
        layout.createTimeTv.setText(TimeConvert.convertDate(data.getCreate_at()));
        BlockChainType type = data.getBlackChain();
        final float scale = getResources().getDisplayMetrics().density;
        int dpHeightInPx = (int) (28 * scale);
        int dpWidthInPx = 0;
        int resource = -1;
        if(type.equals(BlockChainType.METADIUM)){
            dpWidthInPx = (int) (122 * scale);
            resource = R.drawable.img_metadium;
        }else if(type.equals(BlockChainType.ICON)){
            dpWidthInPx = (int) (122 * scale);
            resource = R.drawable.img_icon;
        }else if(type.equals(BlockChainType.INDY)){
            dpWidthInPx = (int) (122 * scale);
            resource = R.drawable.img_indy;
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpWidthInPx, dpHeightInPx);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        layout.chainImage.setLayoutParams(layoutParams);
        layout.chainImage.setBackgroundResource(resource);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        PrintLog.e("onDestroy");
    }


}

