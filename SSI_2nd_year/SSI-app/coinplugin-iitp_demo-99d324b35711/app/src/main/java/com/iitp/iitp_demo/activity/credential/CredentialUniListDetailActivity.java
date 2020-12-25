package com.iitp.iitp_demo.activity.credential;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.model.IndyCredentialVo;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.databinding.ActivityCredentailListDetailBinding;
import com.iitp.iitp_demo.util.BusProvider;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ViewUtils;

import java.util.Map;

import static com.iitp.iitp_demo.Constants.DID;
import static com.iitp.iitp_demo.Constants.JWT_DATA;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.CompanyType;
import static com.iitp.iitp_demo.activity.fragment.CredentialListFragment.UniType;
import static com.iitp.iitp_demo.chain.VCVPCreater.OfficeCredential;

public class CredentialUniListDetailActivity extends BaseActivity{

    private ActivityCredentailListDetailBinding layout;
    private String jwt;
    private IndyCredentialVo attr;
    private String type;
    private boolean indyVC = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        layout = DataBindingUtil.setContentView(this, R.layout.activity_credentail_list_detail);
        layout.back.setOnClickListener(v -> onBackPressed());
        jwt = intent.getStringExtra(JWT_DATA);
        type = intent.getStringExtra(DID);
        attr = IITPApplication.gson.fromJson(jwt, IndyCredentialVo.class);
        if(attr.getSchema_id().contains("meta") || attr.getSchema_id().contains("icon")){
            indyVC = false;
        }else{
            indyVC = true;
        }
        setTitle();
        layout.deligaterBtn.setVisibility(View.GONE);

        layout.detailBtn.setOnClickListener(v -> {
            Intent intent1;
            intent1 = new Intent(CredentialUniListDetailActivity.this, CredentialDetailUniActivity.class);
            intent1.putExtra(JWT_DATA, jwt);
            intent1.putExtra(DID, type);
            intent1.putExtra("indyVc", indyVC);
            startActivity(intent1);
        });

        layout.deleteBtn.setOnClickListener(v -> showDialogReset());
    }

    @Override
    protected void onDestroy(){
        BusProvider.getInstance().post("VCManageActivity");
        super.onDestroy();
    }

    private void deleteVC(){
//        PrintLog.e("jsonData : " + jwt);

        if(type.equals(CompanyType)){
            if(indyVC){
                new Thread(() -> {
                    Indy indy;
                    indy = Indy.getInstance(CredentialUniListDetailActivity.this);
                    indy.deleteCredential(attr.getReferent());
                    runOnUiThread(this::showDialogFinish);
                }).start();
            }else{
                CommonPreference.getInstance(CredentialUniListDetailActivity.this).getSecureSharedPreferences().edit().remove(OfficeCredential).apply();
                showDialogFinish();
            }
        }else{
            new Thread(() -> {
                Indy indy;
                indy = Indy.getInstance(CredentialUniListDetailActivity.this);
                indy.deleteCredential(attr.getReferent());
                runOnUiThread(this::showDialogFinish);
            }).start();
        }
    }

    /**
     * title 설정
     */

    private void setTitle(){
        String title1 = "졸업증명VC";
        String title2 = "사원증VC";
        String title3 = "ETRI";
        if(type.equals(UniType)){
            Map<String, String> attrData = attr.getAttr();
            layout.vcName.setText(title1);
            layout.appbarTitle.setText(attrData.get("collage_name"));
            layout.icon.setBackgroundResource(R.drawable.uni);
        }else if(type.equals(CompanyType)){
            layout.vcName.setText(title2);
            layout.appbarTitle.setText(title3);
            layout.icon.setBackgroundResource(R.drawable.etri_icon);
        }

    }

    private void showDialogReset(){
        String title1 = "VC 삭제";
        LayoutInflater inflater = (LayoutInflater) CredentialUniListDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom2, null);
        AlertDialog customDialog = new AlertDialog.Builder(CredentialUniListDetailActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(CredentialUniListDetailActivity.this, 300);
        float height = ViewUtils.dp2px(CredentialUniListDetailActivity.this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btPositive = dialogView.findViewById(R.id.positive);
        Button btNegative = dialogView.findViewById(R.id.negative);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        twoBtn.setVisibility(View.VISIBLE);
        oneBtn.setVisibility(View.GONE);
        title.setText(title1);
        btPositive.setText(R.string.ok);
        btNegative.setText(R.string.cancel);
        textview.setText(getString(R.string.setting_vc_delete_reset));
        btPositive.setOnClickListener(v -> {
            deleteVC();
            customDialog.dismiss();

        });
        btNegative.setOnClickListener(v -> customDialog.dismiss());
        customDialog.show();
    }

    private void showDialogFinish(){
        String title1 = "VC 삭제";
        LayoutInflater inflater = (LayoutInflater) CredentialUniListDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog customDialog = new AlertDialog.Builder(CredentialUniListDetailActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(CredentialUniListDetailActivity.this, 300);
        float height = ViewUtils.dp2px(CredentialUniListDetailActivity.this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btNegative = dialogView.findViewById(R.id.cancel1);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        twoBtn.setVisibility(View.GONE);
        oneBtn.setVisibility(View.VISIBLE);
        title.setText(title1);
        btNegative.setText(R.string.ok);
        textview.setText("삭제가 완료되었습니다.");
        btNegative.setOnClickListener(v -> {
            customDialog.dismiss();
            finish();
        });
        customDialog.show();
    }
}

