package com.iitp.iitp_demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.request.VCListActivity;
import com.iitp.iitp_demo.databinding.ActivityPinCodeBinding;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.iitp_demo.util.ViewUtils;

public class PincodeActivity extends BaseActivity{

    private ActivityPinCodeBinding layout;
    private String pincode = null;
    private String newPincode = null;
    public static final String PIN_SETTING_TYPE = "pin_setting_type";
    public static final int ACTIVITY_PIN_SETTING = 0x010;
    private int type = 0;
    private boolean inputPin = true;
    private PreferenceUtil preferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_pin_code);
        setSupportActionBar(layout.toolbar.appbar);
        Intent intent = getIntent();

        preferenceUtil = PreferenceUtil.getInstance(PincodeActivity.this);
        String savePinCode = BiometricUtils.getPincode(PincodeActivity.this);
        type = intent.getIntExtra(PIN_SETTING_TYPE, 0);

        if((savePinCode == null || savePinCode.length() != 6) && (type == 1 || type == 2)){
            Intent setIntent = new Intent(this, PincodeActivity.class);
            setIntent.putExtra(PIN_SETTING_TYPE, 0);
            startActivityForResult(setIntent, ACTIVITY_PIN_SETTING);
        }else if(savePinCode == null && type == 0){
            setActionBarSet(layout.toolbar, getString(R.string.did_pincode_init_title), true);
            layout.toolbar.back.setBackgroundResource(R.drawable.ic_action_close);
            init();
        }else if(savePinCode != null && type == 1){
            setActionBarSet(layout.toolbar, getString(R.string.did_pincode_edit_title), true);
            layout.toolbar.back.setBackgroundResource(R.drawable.ic_action_close);
            init();
        }else if(savePinCode != null && type == 2){
            setActionBarSet(layout.toolbar, getString(R.string.did_pincode_title), true);
            layout.toolbar.back.setBackgroundResource(R.drawable.ic_action_close);
            init();
        }

//        switch(type) {
//            case 0:
//                setActionBarSet(layout.toolbar, getString(R.string.did_pincode_init_title), true);
//                layout.toolbar.back.setBackgroundResource(R.drawable.ic_action_close);
//                init();
//                break;
//        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(resultCode == RESULT_OK){
            if(requestCode == ACTIVITY_PIN_SETTING){
                switch(type){
                    case 0:
                        setActionBarSet(layout.toolbar, getString(R.string.did_pincode_init_title), true);
                        break;
                    case 1:
                        setActionBarSet(layout.toolbar, getString(R.string.did_pincode_edit_title), true);
                        break;
                    case 2:
                        setActionBarSet(layout.toolbar, getString(R.string.did_pincode_title), true);
                        break;
                    default:
                        break;
                }
                layout.toolbar.back.setBackgroundResource(R.drawable.ic_action_close);
                init();
            }
        }else{
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init(){
        if(type == 0 || type == 2){
            layout.pinText.setText(R.string.did_pincode_desc1);
            layout.checkLayout.setVisibility(View.INVISIBLE);
        }else if(type == 1){
            layout.pinText.setText(R.string.did_pincode_desc5);
            layout.checkLayout.setVisibility(View.VISIBLE);
        }
        layout.inputLayout.setOnClickListener(v -> {
            inputPin = true;
            layout.inputLayout.requestFocus();
        });
        layout.checkLayout.setOnClickListener(v -> {
            inputPin = false;
            layout.checkLayout.requestFocus();
        });
        initKeypad();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem registrar = menu.findItem(R.id.save);
        if(type == 0 || type == 2){
            registrar.setVisible(false);
        }else if(type == 1){
            registrar.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.pin_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.save){
            String orgPin = pincode;
            String newPin = newPincode;
            PrintLog.e("org pin = " + orgPin);
            PrintLog.e("newPin = " + newPin);
            if(orgPin.equals(newPin)){
                showDialogSetPincode();
            }else{
                ToastUtils.custom(Toast.makeText(PincodeActivity.this, "비밀번호가 일치 하지 않습니다.", Toast.LENGTH_SHORT)).show();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initKeypad(){
        layout.key1.setOnClickListener(this::addChar);
        layout.key2.setOnClickListener(this::addChar);
        layout.key3.setOnClickListener(this::addChar);
        layout.key4.setOnClickListener(this::addChar);
        layout.key5.setOnClickListener(this::addChar);
        layout.key6.setOnClickListener(this::addChar);
        layout.key7.setOnClickListener(this::addChar);
        layout.key8.setOnClickListener(this::addChar);
        layout.key9.setOnClickListener(this::addChar);
        layout.key11.setOnClickListener(this::addChar);
        layout.key12.setOnClickListener(v -> removeLastChar());
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

    public void addChar(View v){
        String text = ((TextView) v).getText().toString();
        PrintLog.e("string = " + text);
        if(inputPin){
            if(pincode != null){
                if(pincode.length() == 0){
                    clearPinCodeImg();
                }
                if(pincode.length() < 6){
                    pincode = pincode + text;
                }else{
                    clearPinCodeImg();
                }
            }else{
                pincode = text;
            }
            PrintLog.e("Pincode = " + pincode);
            changePinImage(pincode);

            if(pincode.length() == 6){
                if(type == 0){
                    setKeypadEnable(false);
                    new Handler().postDelayed(() -> {
                        BiometricUtils.setPincode(PincodeActivity.this, pincode);
                        PrintLog.e("Pincode = " + pincode);
                        //startMainActivity();
                        setResult(RESULT_OK);
                        finish();
                    }, 500);
                }else if(type == 2){
                    String savePinCode = BiometricUtils.getPincode(PincodeActivity.this);
                    PrintLog.e("Pincode = " + pincode);
                    PrintLog.e("savePinCode = " + savePinCode);
                    if(pincode.equals(savePinCode)){
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        pincode = null;
                        clearPinCodeImg();
                        Toast.makeText(getApplicationContext(), R.string.did_pincode_no_matching, Toast.LENGTH_SHORT).show();
                    }


                }else if(type == 1){
                    inputPin = false;
                }
            }
        }else{
            if(newPincode != null){
                if(newPincode.length() == 0){
                    clearPinCodeImg();
                }
                if(newPincode.length() < 6){
                    newPincode = newPincode + text;
                }else{
                    clearPinCodeImg();
                }
            }else{
                newPincode = text;
            }
            changePinImage(newPincode);
        }
    }

    private void startMainActivity(){
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //finish();
    }

    void clearPinCodeImg(){
        changePinImage("");

    }

    private void changePinImage(String pincode){
        int size = pincode.length();
        ImageView[] pins = new ImageView[]{layout.img1, layout.img2, layout.img3, layout.img4, layout.img5, layout.img6};
        ImageView[] pins_1 = new ImageView[]{layout.img11, layout.img21, layout.img31, layout.img41, layout.img51, layout.img61};
        for(int i = 0; i < pins.length; i++){
            if(inputPin){
                PrintLog.e("layout.inputLayout.isFocused()");
                pins[i].setBackgroundResource(i < size ? R.drawable.ic_password_round_active : R.drawable.ic_password_round_inactive);
            }else{
                PrintLog.e("layout.checkLayout.isFocused()");
                pins_1[i].setBackgroundResource(i < size ? R.drawable.ic_password_round_active : R.drawable.ic_password_round_inactive);
            }


        }
    }

    void setKeypadEnable(boolean enable){
        if(layout == null){
            return;
        }
        layout.key1.setClickable(enable);
        layout.key2.setClickable(enable);
        layout.key3.setClickable(enable);
        layout.key4.setClickable(enable);
        layout.key5.setClickable(enable);
        layout.key6.setClickable(enable);
        layout.key7.setClickable(enable);
        layout.key8.setClickable(enable);
        layout.key9.setClickable(enable);
        layout.key11.setClickable(enable);
        layout.key12.setClickable(enable);
    }

    public void removeLastChar(){
        if(inputPin){
            if(pincode != null && pincode.length() > 0){
                pincode = pincode.substring(0, pincode.length() - 1);
                changePinImage(pincode);
            }
        }else{
            if(newPincode != null && newPincode.length() > 0){
                newPincode = newPincode.substring(0, newPincode.length() - 1);
                changePinImage(newPincode);
            }
        }

    }

    private void showDialogSetPincode(){
        LayoutInflater inflater = (LayoutInflater) PincodeActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog customDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(PincodeActivity.this, 300);
        float height = ViewUtils.dp2px(PincodeActivity.this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btPositive = dialogView.findViewById(R.id.cancel1);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        title.setText(R.string.did_fingerprint_pin);
        twoBtn.setVisibility(View.GONE);
        oneBtn.setVisibility(View.VISIBLE);
        btPositive.setText(R.string.ok);
        textview.setText(getString(R.string.did_pincode_dialog_text));
        btPositive.setOnClickListener(v -> {
            BiometricUtils.setPincode(PincodeActivity.this, newPincode);
            customDialog.dismiss();
            preferenceUtil.setPincode(newPincode);
            finish();
        });
        customDialog.show();
    }


}

