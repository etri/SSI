package com.iitp.iitp_demo.activity.dids;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.FinishListener;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.setting.SettingResetActivity;
import com.iitp.iitp_demo.chain.Icon;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.chain.Metadium;
import com.iitp.iitp_demo.databinding.ActivityDidAddBinding;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ViewUtils;

public class DidAddActivity extends BaseActivity{

    private ActivityDidAddBinding layout;
    public int index = 0;
    private BlockChainType type;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_did_add);
        setSupportActionBar(layout.toolbar.appbar);
        setActionBarSet(layout.toolbar, getString(R.string.add_did_new_did), true);
        layout.toolbar.back.setBackgroundResource(R.drawable.ic_action_close);
        init();
    }

    private void init(){
        layout.nickEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                if(layout.nickEditText.length() == 0){
                    layout.nickEditText.setText("");
                }
            }
        });
        layout.metadiumLayout.setOnClickListener(v -> layout.rgBtn1.setChecked(true));

        layout.indyLayout.setOnClickListener(v -> layout.rgBtn2.setChecked(true));
        layout.iconLayout.setOnClickListener(v -> layout.rgBtn3.setChecked(true));

        layout.rgBtn1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                type = BlockChainType.METADIUM;
                layout.rgBtn2.setChecked(false);
                layout.rgBtn3.setChecked(false);
            }
        });

        layout.rgBtn2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                type = BlockChainType.INDY;
                layout.rgBtn1.setChecked(false);
                layout.rgBtn3.setChecked(false);
            }
        });

        layout.rgBtn3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                type = BlockChainType.ICON;
                layout.rgBtn2.setChecked(false);
                layout.rgBtn1.setChecked(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.create_did, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if(item.getItemId() == R.id.create){
            View focusView = getCurrentFocus();
            if(focusView != null){
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            }
            if(type == null){
                showDialog(getString(R.string.add_did_select_blockChain));
                return true;
            }
            if(type.equals(BlockChainType.METADIUM)){
                if(layout.nickEditText.getText().toString().length() == 0){
                    showDialog(getString(R.string.add_did_write_nick));
                }else{
                    layout.progresslayout.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(this::createMetadiumDid, 500);
                }

            }else if(type.equals(BlockChainType.INDY)){
                if(layout.nickEditText.getText().toString().length() == 0){
                    showDialog(getString(R.string.add_did_write_nick));
                }else{
                    layout.progresslayout.setVisibility(View.VISIBLE);
                    createIndyDid();
                }
            }else if(type.equals(BlockChainType.ICON)){
                if(layout.nickEditText.getText().toString().length() == 0){
                    showDialog(getString(R.string.add_did_write_nick));
                }else{
                    layout.progresslayout.setVisibility(View.VISIBLE);
                    createIconDid();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(layout.progresslayout.getVisibility() != View.VISIBLE){
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        PrintLog.e("onDestroy");
    }

    private void createMetadiumDid(){
        Metadium metadium = Metadium.getInstance();
        FinishListener finish = new FinishListener(){
            @Override
            public void finishOK(String did){
                PrintLog.e("finish create");
                metadium.saveDid(DidAddActivity.this, layout.nickEditText.getText().toString().trim(), did, BlockChainType.METADIUM, false);
                layout.progresslayout.setVisibility(View.GONE);
                showFinishCreateDIDDialog();
//                activityFinish();
            }

            @Override
            public void finishError(String error){
                PrintLog.e("error");
            }
        };

        metadium.createMetaDID(DidAddActivity.this, finish);

    }

    private void createIconDid(){
        if(layout.nickEditText.getText().toString().length() == 0){
            showDialog(getString(R.string.add_did_write_nick));
        }else{
            Icon icon = Icon.getInstance();
            FinishListener finish = new FinishListener(){
                @Override
                public void finishOK(String did){
                    PrintLog.e("finish icon did create");
                    icon.saveDid(DidAddActivity.this, layout.nickEditText.getText().toString().trim(), BlockChainType.ICON);
                    runOnUiThread(() -> {
                        layout.progresslayout.setVisibility(View.GONE);
                        showFinishCreateDIDDialog();
                    });
//                    showFinishCreateDIDDialog();
//                    activityFinish();
                }

                @Override
                public void finishError(String error){

                }
            };
            new Thread(() -> {
                try{
                    icon.creatIconDid(finish);
                }catch(Exception e){
                    // error
                    e.printStackTrace();
                }
            }).start();

        }
    }

    private void createIndyDid(){
        if(layout.nickEditText.getText().toString().length() == 0){
            showDialog(getString(R.string.add_did_write_nick));
        }else{
            Indy indy = Indy.getInstance(DidAddActivity.this);
            FinishListener indyFinish = new FinishListener(){
                @Override
                public void finishOK(String did){
                    PrintLog.e("finish indy did create");
                    indy.saveDid(DidAddActivity.this, layout.nickEditText.getText().toString().trim(), BlockChainType.INDY);
                    runOnUiThread(() -> {
                        layout.progresslayout.setVisibility(View.GONE);
                        showFinishCreateDIDDialog();
                    });
//                    activityFinish();
                }

                @Override
                public void finishError(String error){

                }
            };
            new Thread(() -> {
                try{
//                    indy.createPreVCIDCard(indyFinish);
                    indy.createIndyDid(indyFinish);
                }catch(Exception e){
                    // error
                    e.printStackTrace();
                }
            }).start();

        }
    }

    private void activityFinish(){
        finish();
    }


    private void showDialog(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(DidAddActivity.this, R.style.AppCompatAlertDialogStyle);
//        AlertDialog.Builder builder = new AlertDialog.Builder(DidAddActivity.this);
        builder.setMessage(text);
        builder.setPositiveButton(R.string.ok,
                (dialog, which) -> {
                    // MetaID 생성 재시도
                    dialog.dismiss();
                });

        builder.setCancelable(false);
        builder.show();
    }

//    private void showFinishCreateDIDDialog(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(DidAddActivity.this, R.style.AppCompatAlertDialogStyle);
////        AlertDialog.Builder builder = new AlertDialog.Builder(DidAddActivity.this);
//        builder.setMessage("DID 생성 완료");
//        builder.setPositiveButton(R.string.ok,
//                (dialog, which) -> {
//                    // MetaID 생성 재시도
//                    dialog.dismiss();
//                    activityFinish();
//                });
//
//        builder.setCancelable(false);
//        builder.show();
//    }

    private void showFinishCreateDIDDialog(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog customDialog = new AlertDialog.Builder(DidAddActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(DidAddActivity.this, 300);
        float height = ViewUtils.dp2px(DidAddActivity.this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btPositive = dialogView.findViewById(R.id.cancel1);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        oneBtn.setVisibility(View.VISIBLE);
        twoBtn.setVisibility(View.INVISIBLE);
        title.setText("DID 생성");
        btPositive.setText(R.string.ok);

        textview.setText(R.string.did_create_complate);


        btPositive.setOnClickListener(v -> {
            customDialog.dismiss();
            activityFinish();
        });
        customDialog.show();
    }


}

