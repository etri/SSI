package com.iitp.iitp_demo.activity.dids;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.databinding.ActivityDidChangeNameBinding;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.ArrayList;

import static com.iitp.iitp_demo.Constants.DID_INDEX;

public class DidChangeNameActivity extends BaseActivity{

    private ActivityDidChangeNameBinding layout;
    private int index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_did_change_name);
        setActionBarSet(layout.toolbar, getString(R.string.did_change_title), true);
        Intent intent = getIntent();
        index = intent.getIntExtra(DID_INDEX, 0);


        init();

        layout.changeConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(layout.nicknameEt.getWindowToken(), 0);
                Toast.makeText(getApplicationContext(), R.string.change_did_nickname_confirm, Toast.LENGTH_SHORT).show();
                MainActivity.changeNickname(getApplicationContext(), layout.nicknameEt.getText().toString(), index);
                finish();
            }
        });
    }

    private void init(){
        ArrayList<DidDataVo> didList = MainActivity.getDidList(DidChangeNameActivity.this);
        DidDataVo data = didList.get(index);
        layout.nicknameEt.setText(data.getNickName());
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
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dpWidthInPx, dpHeightInPx);
        layout.blockChainImage.setLayoutParams(layoutParams);
        layout.blockChainImage.setBackgroundResource(resource);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu){
//        getMenuInflater().inflate(R.menu.change_did, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item){
//
//        switch(item.getItemId()){
//            case R.id.change:
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        PrintLog.e("onDestroy");
    }



//    private void createMetadiumDid(){
//        if(layout.nickEditText.getText().toString().length() == 0){
//            showDialog(getString(R.string.add_did_write_nick));
//        }else{
//            Metadium metadium = Metadium.getInstance();
//            FinishListener finish = new FinishListener(){
//                @Override
//                public void finishOK(String did){
//                    PrintLog.e("did = "+did);
//                    metadium.saveDid(DidChangeNameActivity.this, layout.nickEditText.getText().toString().trim(),did, BlockChainType.METADIUM, false);
//                }
//
//                @Override
//                public void finishError(String error){
//
//                }
//            };
//
//            metadium.createMetaDID(DidChangeNameActivity.this, finish);
//        }
//    }
//
//    private void createIconDid(){
//        if(layout.nickEditText.getText().toString().length() == 0){
//            showDialog(getString(R.string.add_did_write_nick));
//        }else{
//            Icon icon = Icon.getInstance();
//            FinishListener finish = new FinishListener(){
//                @Override
//                public void finishOK(String did){
//                    PrintLog.e("finish icon did create");
////                    icon.saveDid(AddDidActivity.this, layout.nickEditText.getText().toString().trim(), BlockChainType.METADIUM);
//                }
//
//                @Override
//                public void finishError(String error){
//
//                }
//            };
//            new Thread(() -> {
//                try{
//                    icon.creatIconDid(finish);
//                }catch(Exception e){
//                    // error
//                    e.printStackTrace();
//                }
//            }).start();
//
//        }
//    }

}

