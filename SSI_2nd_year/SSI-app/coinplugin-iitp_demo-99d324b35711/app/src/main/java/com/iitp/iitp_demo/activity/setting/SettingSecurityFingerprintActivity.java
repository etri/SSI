package com.iitp.iitp_demo.activity.setting;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.SplashActivity;
import com.iitp.iitp_demo.databinding.ActivitySettingSecureSetFingerprintBinding;
import com.iitp.iitp_demo.util.BiometricDialog;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ViewUtils;

public class SettingSecurityFingerprintActivity extends BaseActivity{


    private ActivitySettingSecureSetFingerprintBinding layout;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_setting_secure_set_fingerprint);
        setActionBarSet(layout.toolbar, getString(R.string.did_setting_security_edit_bio), true);
        initView();
    }

    @Override
    protected void onResume(){

        super.onResume();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    private void initView(){
        layout.onLayout.setOnClickListener(v -> setBio(true));
        layout.offLayout.setOnClickListener(v -> setBio(false));
    }

    private void showDialogSetFingerPrint(int type){
        LayoutInflater inflater = (LayoutInflater) SettingSecurityFingerprintActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog customDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(SettingSecurityFingerprintActivity.this, 300);
        float height = ViewUtils.dp2px(SettingSecurityFingerprintActivity.this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btPositive = dialogView.findViewById(R.id.cancel1);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        title.setText(R.string.did_setting_security_edit_bio);
        twoBtn.setVisibility(View.GONE);
        oneBtn.setVisibility(View.VISIBLE);
        btPositive.setText(R.string.ok);
        if(type == 0){
            textview.setText(getString(R.string.did_fingerprint_bio_set_dialog));
        }else{
            textview.setText(getString(R.string.did_fingerprint_bio_set_dialog));
        }
        btPositive.setOnClickListener(v -> {
            customDialog.dismiss();
            finish();
        });
        customDialog.show();
    }


    private void setBio(boolean on){
        if(BiometricUtils.checkFingerprint(this)){
            fingerBioFactory(this, on);
//            fingerPrintFactory(on);
        }else{
            showDialogSetFingerPrint(1);
        }
    }

    private void fingerBioFactory(Context ctx, boolean on){
        FingerBioFactory fingerBioFactory = new FingerBioFactory(this, new BiometricPrompt.AuthenticationCallback(){
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString){
                PrintLog.e("onAuthenticationError");
                PrintLog.e("Error = " + errorCode);
                if(errorCode == BiometricConstants.ERROR_HW_UNAVAILABLE){
                    Toast.makeText(ctx, ctx.getString(R.string.biometric_no_hw), Toast.LENGTH_SHORT).show();
                }else if(errorCode == BiometricConstants.ERROR_NO_BIOMETRICS){
                    Toast.makeText(ctx, ctx.getString(R.string.biometric_no_bio), Toast.LENGTH_SHORT).show();
                }
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result){
                PrintLog.e("onAuthenticationSucceeded");
                super.onAuthenticationSucceeded(result);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        if(on){
                            BiometricUtils.enableFingerPrint(SettingSecurityFingerprintActivity.this);
                        }else{
                            BiometricUtils.disableFingerPrint(SettingSecurityFingerprintActivity.this);
                        }
                        showDialogSetFingerPrint(0);
                    }
                }, 500);
            }


            @Override
            public void onAuthenticationFailed(){
                PrintLog.e("onAuthenticationFailed");
                super.onAuthenticationFailed();
            }
        });
        fingerBioFactory.setting(getString(R.string.did_fingerprint_title), getString(R.string.did_fingerprint_desc), getString(R.string.cancel));
        fingerBioFactory.authenticate();
    }

    private void fingerPrintFactory(boolean on){
        BiometricDialog biometricDialog = new BiometricDialog(SettingSecurityFingerprintActivity.this,
                getString(R.string.did_fingerprint_title),
                getString(R.string.did_fingerprint_desc),
                new BiometricDialog.OnAuthenticationListener(){
                    @Override
                    public void onSuccess(FingerprintManager.AuthenticationResult result){
                        if(on){
                            BiometricUtils.enableFingerPrint(SettingSecurityFingerprintActivity.this);
                        }else{
                            BiometricUtils.disableFingerPrint(SettingSecurityFingerprintActivity.this);
                        }

                        showDialogSetFingerPrint(0);
                    }

                    @Override
                    public void onError(int errorCode, CharSequence errString, AlertDialog errorDialog){
                        PrintLog.e("error " + errString.toString());
                        if(errorDialog != null){
                            errorDialog.setOnDismissListener(d -> {
                            });
                        }
                    }

                    @Override
                    public void onCancel(){
                        PrintLog.e("cancel");
                    }
                }, 1
        );
        biometricDialog.setCancelable(false);
        biometricDialog.show();
    }
}
