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

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.FriendsListActivity;
import com.iitp.iitp_demo.activity.model.ProductVC;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.databinding.ActivityCredentailListDetailBinding;
import com.iitp.iitp_demo.util.BusProvider;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.iitp.iitp_demo.Constants.DID_DATA;
import static com.iitp.iitp_demo.Constants.JWT_DATA;
import static com.iitp.iitp_demo.chain.VCVPCreater.DELEGATOR_VC;

public class CredentialListDetailActivity extends BaseActivity{

    private ActivityCredentailListDetailBinding layout;
    private HashMap<String, Object> jsonData;
    private String type;
    private String jwt;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        layout = DataBindingUtil.setContentView(this, R.layout.activity_credentail_list_detail);
        layout.back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onBackPressed();
            }
        });
        jsonData = (HashMap<String, Object>) intent.getSerializableExtra(DID_DATA);
        jwt = intent.getStringExtra(JWT_DATA);
        index = intent.getIntExtra("index", 0);
        for(String key : jsonData.keySet()){
            String data = jsonData.get(key).toString();
            PrintLog.e(key + " : " + data);
            if(key.equals("type")){
                ArrayList<String> typeList = (ArrayList<String>) jsonData.get(key);
                type = typeList.get(1);
            }
        }
        setTitle(type);
        layout.deligaterBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(CredentialListDetailActivity.this, FriendsListActivity.class);
                startActivity(intent);
            }
        });

        layout.detailBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                PrintLog.e("type= "+type);
                Intent intent;
                if(type.equals("DelegatedVC")){
                    intent = new Intent(CredentialListDetailActivity.this, CredentialDetailDeleigatorActivity.class);
                }else{
                    intent = new Intent(CredentialListDetailActivity.this, CredentialDetailActivity.class);
                }
                intent.putExtra(DID_DATA, jsonData);
                intent.putExtra(JWT_DATA, jwt);
                intent.putExtra("index", index);
                startActivity(intent);
            }
        });

        layout.deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                showDialogReset();

            }
        });


    }

    @Override
    protected void onDestroy(){
        BusProvider.getInstance().post("VCManageActivity");
        super.onDestroy();
    }

    private void deleteVC(){
        PrintLog.e("type : " + type);
        PrintLog.e("jsonData : " + jsonData);
        PrintLog.e("jsonData : " + jwt);
        VCVPCreater creater = VCVPCreater.getInstance();
        String preKey = creater.getPreferenceKey(type);
        if(type.equals("DelegatedVC")){
            preKey = DELEGATOR_VC;
            CommonPreference.getInstance(CredentialListDetailActivity.this).getSecureSharedPreferences().edit().remove(preKey).apply();
        }else if(type.equals("ProductCredential")||type.equals("ProductProofCredential")){
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(CredentialListDetailActivity.this);
            ArrayList<ProductVC> vcList = preferenceUtil.getProductVC();
            for(ProductVC temp : vcList){
                if(temp.getProductProofVC().equals(jwt) || temp.getProductVC().equals(jwt)){
                    PrintLog.e(temp.getProductProofVC());
                    PrintLog.e(temp.getProductVC());
                    preferenceUtil.removeProductVC(temp);
                }
            }
        }
//        finish();
    }

    /**
     * title 설정
     *
     * @param did
     */
    private void setTitle(String type){
        PrintLog.e("Type = " + type);
        switch(type){
            case "IdentificationCredential":
                layout.vcName.setText(R.string.idcredentialVC);
                layout.appbarTitle.setText("행정안전부");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_govern);
                break;
            case "CardTokenCredential":
                if(index == 0){
                    layout.vcName.setText(R.string.cardVC1);
                }else{
                    layout.vcName.setText(R.string.cardVC2);
                }
                layout.appbarTitle.setText("카드사");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_card);
                break;
            case "AddressCredential":
                layout.vcName.setText(R.string.addressVC);
                layout.appbarTitle.setText("우체국");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_post);
                break;
            case "GraduationCredential":
                layout.appbarTitle.setText("대학 컨소시엄");
                layout.icon.setBackgroundResource(R.drawable.uni);
                break;
            case "delegated_id_VC":
            case "DelegatedVC":
                layout.appbarTitle.setText("행정안정부");
                layout.vcName.setText("위임신분증VC");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_govern);
                layout.deligaterBtn.setVisibility(View.GONE);
                break;
            case "ProductCredential":
                layout.vcName.setText("물품정보VC");
                layout.appbarTitle.setText("고가품마켓");
                layout.icon.setBackgroundResource(R.drawable.market);
                break;
            case "ProductProofCredential":
                layout.vcName.setText("거래증명VC");
                layout.appbarTitle.setText("고가품마켓");
                layout.icon.setBackgroundResource(R.drawable.market);
                break;
            case "LoginCredential":
                layout.vcName.setText("Men's Watch VC");
                layout.appbarTitle.setText("고가품마켓");
                layout.icon.setBackgroundResource(R.drawable.market);
                break;
            case "StockServiceCredential":
                layout.vcName.setText("한국증권VC");
                layout.appbarTitle.setText("증권사");
                layout.icon.setBackgroundResource(R.drawable.stock);
                break;
            case "PhoneCredential":
                layout.vcName.setText("한국통신VC");
                layout.appbarTitle.setText("통신사");
                layout.icon.setBackgroundResource(R.drawable.phone);
                break;
            default:
                layout.vcName.setText(R.string.idcredentialVC);
                layout.appbarTitle.setText("행정안전부");
                layout.icon.setBackgroundResource(R.drawable.ic_list_item_govern);
                break;

        }
    }


    private void showDialogReset(){
        LayoutInflater inflater = (LayoutInflater) CredentialListDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom2, null);
        AlertDialog customDialog = new AlertDialog.Builder(CredentialListDetailActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(CredentialListDetailActivity.this, 300);
        float height = ViewUtils.dp2px(CredentialListDetailActivity.this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btPositive = dialogView.findViewById(R.id.positive);
        Button btNegative = dialogView.findViewById(R.id.negative);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        twoBtn.setVisibility(View.VISIBLE);
        oneBtn.setVisibility(View.GONE);
        title.setText("VC 삭제");
        btPositive.setText(R.string.ok);
        btNegative.setText(R.string.cancel);
        textview.setText(getString(R.string.setting_vc_delete_reset));
        btPositive.setOnClickListener(v -> {
            deleteVC();
            customDialog.dismiss();
            showDialogFinish();
        });
        btNegative.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                customDialog.dismiss();
            }
        });
        customDialog.show();
    }

    private void showDialogFinish(){
        LayoutInflater inflater = (LayoutInflater) CredentialListDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog customDialog = new AlertDialog.Builder(CredentialListDetailActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(CredentialListDetailActivity.this, 300);
        float height = ViewUtils.dp2px(CredentialListDetailActivity.this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btNegative = dialogView.findViewById(R.id.cancel1);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        twoBtn.setVisibility(View.GONE);
        oneBtn.setVisibility(View.VISIBLE);
        title.setText("VC 삭제");
        btNegative.setText(R.string.ok);
        textview.setText("삭제가 완료되었습니다.");
        btNegative.setOnClickListener(v -> {
            customDialog.dismiss();
            finish();
        });
        customDialog.show();
    }
}

