package com.iitp.iitp_demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.api.AgentAPI;
import com.iitp.iitp_demo.api.model.pushResponseVo;
import com.iitp.iitp_demo.util.BiometricDialog;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;

import java.util.ArrayList;

import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;


public class SplashActivity extends AppCompatActivity{

    private CommonPreference commPref;
    String metaId = null;

    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;

    private boolean checkBio;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        PrintLog.e("splsh");
        setContentView(R.layout.activity_splash);
        commPref = CommonPreference.getInstance(this);
        checkFCMToken();
        String savePinCode =  BiometricUtils.getPincode(this);
        if(savePinCode == null ){
            checkBio = false;
            BiometricUtils.hasBiometricEnrolled(this);
            if((BiometricUtils.getPincode(SplashActivity.this) == null && !(BiometricUtils.isFingerPrint(SplashActivity.this)))){
                if(BiometricUtils.checkFingerprint(this)){
                    fingerBioFactory(this);
                }else{
                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            startPincodeActivity();
                        }
                    }, 1000);
                }
            }else{
                if(BiometricUtils.isFingerPrint(SplashActivity.this)){
                    fingerBioFactory(this);
                }else{
                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            startPincodeActivity();
                        }
                    }, 1500);
                }
            }
        } else {
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    startMainActivity();
                }
            }, 1500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBio = false;
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        PrintLog.e("splash onNewIntent");
    }

    private void startMainActivity(){
        Intent intent;
        intent = new Intent(this, MainActivity.class);
//        intent = new Intent(this, SampleTestActivity.class);

        startActivity(intent);
        finish();
    }

    private void startPincodeActivity(){
        Intent intent;
        intent = new Intent(this, PincodeActivity.class);
        intent.putExtra(PIN_SETTING_TYPE, 2);
        startActivityForResult(intent, REQUEST_CODE_VERIFY_PIN_APP_LOCK);
        //finish();
    }

    /**
     * fcm token 체크
     */
    private void checkFCMToken(){
        PrintLog.e("push is set");
        ArrayList<DidDataVo> didList;
        String didListJson = CommonPreference.getInstance(SplashActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
        if(didListJson != null){
            didList = IITPApplication.gson.fromJson(didListJson, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
            String did = null;
            for(DidDataVo temp : didList){
                if(temp.getFavorite()){
                    did = temp.getDid();
                }
            }
            MutableLiveData<pushResponseVo> response = new MutableLiveData<>();
            response.observe(this, pushAPIResponseVo -> {
                if(pushAPIResponseVo != null){
                    PrintLog.e("status = " + pushAPIResponseVo.getStatus());
                }else{
//                        networkErrorDialog(SplashActivity.this);
                }
            });
            if(did != null){
                AgentAPI.getInstance().registerToken(response, did);
            }
        }
    }

    private void fingerBioFactory(Context ctx){
        FingerBioFactory fingerBioFactory = new FingerBioFactory(this, new BiometricPrompt.AuthenticationCallback(){
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString){
                PrintLog.e("onAuthenticationError");
                PrintLog.e("Error = " + errorCode);
                if(errorCode == BiometricConstants.ERROR_HW_UNAVAILABLE || errorCode == BiometricConstants.ERROR_NO_BIOMETRICS
                        || errorCode == 13 || errorCode == 10 || errorCode == 7){
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            // 사용하고자 하는 코드
                            ToastUtils.custom(Toast.makeText(ctx, errString, Toast.LENGTH_SHORT)).show();
                        }
                    }, 0);
                    if(!checkBio) {
                        startPincodeActivity();
                        checkBio = true;
                    }
                }
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result){
                PrintLog.e("onAuthenticationSucceeded");
                super.onAuthenticationSucceeded(result);
                BiometricUtils.enableFingerPrint(SplashActivity.this);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startMainActivity();
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

    private void fingerPrintFactory(){
        BiometricDialog biometricDialog = new BiometricDialog(SplashActivity.this,
                getString(R.string.did_fingerprint_title),
                getString(R.string.did_fingerprint_desc),
                new BiometricDialog.OnAuthenticationListener(){
                    @Override
                    public void onSuccess(FingerprintManager.AuthenticationResult result){
                        BiometricUtils.enableFingerPrint(SplashActivity.this);
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                startMainActivity();
                            }
                        }, 500);
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
                }
                , 0);

        biometricDialog.setCancelable(false);
        biometricDialog.setPositiveButton(new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                PrintLog.e("positive");
                dialog.dismiss();
                finish();
            }
        });
        biometricDialog.setNegativeButton(new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                PrintLog.e("neagative");
                dialog.dismiss();
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startPincodeActivity();
                    }
                }, 1000);
            }
        });
        biometricDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK) {
            PrintLog.e("requestCode = " + requestCode);
            if (resultCode == RESULT_OK) {
                startMainActivity();
                finish();
            } else {
                finish();
                //startMainActivity();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
