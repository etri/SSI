package com.iitp.iitp_demo.activity.setting.backup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.KeyManager;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.activity.setting.SettingBackupActivity;
import com.iitp.iitp_demo.api.VaultAPI;
import com.iitp.iitp_demo.api.model.AuthBackupDataVo;
import com.iitp.iitp_demo.api.model.AuthBackupVo;
import com.iitp.iitp_demo.api.model.AuthIdVo;
import com.iitp.iitp_demo.api.model.AuthRecoveryDataVo;
import com.iitp.iitp_demo.api.model.AuthResponseVo;
import com.iitp.iitp_demo.api.model.AuthVerifyVo;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.chain.Vault;
import com.iitp.iitp_demo.databinding.ActivitySettingBackupAuth1Binding;
import com.iitp.iitp_demo.util.BusProvider;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.iitp_demo.util.ViewUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nomadconnection.vault.RecoveryKey;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.api.VaultAPIInfo.Storage1;
import static com.iitp.iitp_demo.api.VaultAPIInfo.Storage2;

public class BackupAuthActivity extends BaseActivity{


    private ActivitySettingBackupAuth1Binding layout;


    private VaultAPI vaultAPI = VaultAPI.getInstance();

    private String masterVerifyToken = null;
    private String storage1VerifyToken = null;
    private String storage2VerifyToken = null;
    private String masterAuthToken = null;
    private String storage1AuthToken = null;
    private String storage2AuthToken = null;
    private String recoveryMasterKey = null;
    private String recoveryStorage1 = null;
    private String dataClueStorage1 = null;
    private String recoveryStorage2 = null;
    private String dataClueStorage2 = null;
    private PreferenceUtil preferenceUtil;
    private boolean secondClick = false;
    private int type = 0;
    private AlertDialog FinishDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_setting_backup_auth1);
        Intent intent = getIntent();
        type = intent.getIntExtra("TYPE", 0);
        PrintLog.e("type = " + type);
        setActionBarSet(layout.toolbar, getString(R.string.backupRequestAuth), true);
        preferenceUtil = PreferenceUtil.getInstance(this);
        layout.layoutAuth1.setVisibility(View.VISIBLE);
        layout.requestAuth.setOnClickListener(v -> {
            if(isEmail(layout.emailEdit.getText().toString()) && isPhoneNumber(layout.phoneEdit.getText().toString())){
                layout.progresslayout.setVisibility(View.VISIBLE);
                requestMasterInit(layout.emailEdit.getText().toString());
                requestStorage1Init(layout.emailEdit.getText().toString());
                requestStorage2Init(layout.phoneEdit.getText().toString());
            }else{
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "이메일이나 전화번호가 형식에 맞지 않습니다.", Toast.LENGTH_SHORT)).show();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    /**
     * backup/resotre finish dailog
     * @param titleText title
     * @param messageText message
     */
    private void showBackupRecoverDialogFinish(String titleText, String messageText){
        LayoutInflater inflater = (LayoutInflater) BackupAuthActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_password1, null);
        FinishDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(this, 320);
        float height = ViewUtils.dp2px(this, 252);
        FinishDialog.getWindow().setLayout((int) width, (int) height);

        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView message = dialogView.findViewById(R.id.messageText);
        Button btPositive = dialogView.findViewById(R.id.cancel1);
        LinearLayout layout1 = dialogView.findViewById(R.id.layout1);
        LinearLayout layout2 = dialogView.findViewById(R.id.layout2);
        layout1.setVisibility(View.GONE);
        layout2.setVisibility(View.VISIBLE);
        title.setText(titleText);
        message.setText(messageText);
        btPositive.setText("확인");
        btPositive.setOnClickListener(v -> {
            FinishDialog.dismiss();
            BusProvider.getInstance().post("SettingBackupActivityFinish");
            finish();
        });
        FinishDialog.show();
    }

    /**
     * master init
     * @param email email
     */
    private void requestMasterInit(String email){
        PrintLog.e("email = " + email);
        AuthIdVo authIdVo = new AuthIdVo(email);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestMasterAuth(authIdVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        if(result){
                            masterVerifyToken = authResponseVo.getResult().getVerifyToken();
                            checkVerifyToken();
                        }
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " MasterInit", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * storage1 init
     * @param email email
     */
    private void requestStorage1Init(String email){
        AuthIdVo authIdVo = new AuthIdVo(email);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestStorage1Auth(Storage1 + "v1/auth/initiate", authIdVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        if(result){
                            storage1VerifyToken = authResponseVo.getResult().getVerifyToken();
                            checkVerifyToken();
                        }
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " Storage1Init", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * storage2 init
     * @param mobile mobile
     */
    private void requestStorage2Init(String mobile){
        AuthIdVo authIdVo = new AuthIdVo(mobile);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestStorage2Auth(Storage2 + "v1/auth/initiate", authIdVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        if(result){
                            storage2VerifyToken = authResponseVo.getResult().getVerifyToken();
                            checkVerifyToken();
                        }
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " Storage2Init", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * master verify
     * @param authCode authCode
     */
    private void requestMasterVerify(String authCode){

        AuthVerifyVo authVerifyVo = new AuthVerifyVo(masterVerifyToken, authCode);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestMasterVerify(authVerifyVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        if(result){
                            masterAuthToken = authResponseVo.getResult().getAuthToken();
                            PrintLog.e("master AuthToken = " + masterAuthToken);
                            layout.auth2Text.setVisibility(View.VISIBLE);
                            layout.auth2Edit.setVisibility(View.VISIBLE);
                            layout.auth2Edit.requestFocus();
                            layout.authCode1Img.setVisibility(View.VISIBLE);
                            layout.authInput2.setVisibility(View.VISIBLE);
                            secondClick = true;
                        }else{
                            layout.auth1Edit.setText("");
                            layout.auth1Edit.requestFocus();
                            ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "인증코드가 틀렸습니다.다시 시도해주세요", Toast.LENGTH_SHORT)).show();
                        }
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " MasterVerify", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * storage1 verify
     * @param authcode authcode
     */
    private void requestStorage1Verify(String authcode){
        AuthVerifyVo authVerifyVo = new AuthVerifyVo(storage1VerifyToken, authcode);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestStorage1Verify(Storage1 + "v1/auth/verify", authVerifyVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        if(result){
                            storage1AuthToken = authResponseVo.getResult().getAuthToken();
                            layout.authCode2Img.setVisibility(View.VISIBLE);
                            PrintLog.e("storage1 AuthToken = " + storage1AuthToken);
                            secondClick = false;
                            new Handler(Looper.getMainLooper()).postDelayed(() -> runOnUiThread(BackupAuthActivity.this::initLayout3), 500);

                        }else{
                            layout.auth2Edit.setText("");
                            layout.auth2Edit.requestFocus();
                            ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "인증코드가 틀렸습니다.다시 시도해주세요", Toast.LENGTH_SHORT)).show();
                        }
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " Storage1Verify", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * storage2 verify
     * @param authcode authCode
     */
    private void requestStorage2Verify(String authcode){
        AuthVerifyVo authVerifyVo = new AuthVerifyVo(storage2VerifyToken, authcode);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestStorage2Verify(Storage2 + "v1/auth/verify", authVerifyVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        if(result){
                            storage2AuthToken = authResponseVo.getResult().getAuthToken();
                            PrintLog.e("storage2 AuthToken = " + storage2AuthToken);
                            layout.progresslayout.setVisibility(View.VISIBLE);
                            PrintLog.e("type = "+type);
                            if(type == 0){
                                vaultBackup();
                            }else if(type == 1){
                                vaultRecover();
                            }
                        }else{
                            layout.authSMSEdit.setText("");
                            layout.authSMSEdit.requestFocus();
                            ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "인증코드가 틀렸습니다.다시 시도해주세요", Toast.LENGTH_SHORT)).show();
                        }
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, response.code() + " Server error", Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " Storage2Verify", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * requestBackupMaster
     * @param recoveryKey recovery key
     */
    private void requestBackupMaster(String recoveryKey){
        AuthBackupVo authBackupVo = new AuthBackupVo(masterAuthToken, recoveryKey);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestBackupMaster(authBackupVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        PrintLog.e("result = " + result);
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " BackupMaster", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * requestBackupStorage1
     * @param recoveryKey recoveryKey
     * @param data data
     */
    private void requestBackupStorage1(String recoveryKey, String data){
        AuthBackupDataVo authBackupDataVo = new AuthBackupDataVo(storage1AuthToken, recoveryKey, data);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestBackupStorage(Storage1 + "v1/vault/clue", authBackupDataVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        PrintLog.e("result = " + result);
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " BackupStorage1", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * requestBackupStorage2
     * @param recoveryKey recoveryKey
     * @param data data
     */
    private void requestBackupStorage2(String recoveryKey, String data){
        AuthBackupDataVo authBackupDataVo = new AuthBackupDataVo(storage2AuthToken, recoveryKey, data);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestBackupStorage(Storage2 + "v1/vault/clue", authBackupDataVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        PrintLog.e("result = " + result);
                        showBackupRecoverDialogFinish("백업", "키/VC 백업이\n완료되었습니다.");
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " BackupStorage2", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * requestRecoverMaster
     */
    private void requestRecoverMaster(){
        AuthRecoveryDataVo authRecoveryDataVo = new AuthRecoveryDataVo(masterAuthToken);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestRecoveryMaster(authRecoveryDataVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        PrintLog.e("result = " + result);
                        if(result){
                            recoveryMasterKey = authResponseVo.getResult().getRecoveryKey();
                            checkRecoverData();
                            requestRecoverStorage1();
                            requestRecoverStorage2();
                        }else{
                            showBackupRecoverDialogFinish("복구", "백업데이터가 없습니다.");
                        }
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " RecoverMaster", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * requestRecoverStorage1
     */
    private void requestRecoverStorage1(){
        AuthRecoveryDataVo authRecoveryDataVo = new AuthRecoveryDataVo(storage1AuthToken);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestRecoveryStorage(Storage1 + "v1/vault/restore", authRecoveryDataVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){
                layout.progresslayout.setVisibility(View.GONE);
                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        recoveryStorage1 = authResponseVo.getResult().getRecoveryClue();
                        dataClueStorage1 = authResponseVo.getResult().getDataClue();
                        PrintLog.e("result = " + result);
                        checkRecoverData();
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " RecoverStorage1", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * requestRecoverStorage2
     */
    private void requestRecoverStorage2(){
        AuthRecoveryDataVo authRecoveryDataVo = new AuthRecoveryDataVo(storage2AuthToken);
        Call<AuthResponseVo> rtn = vaultAPI.vaultAPIInfo.requestRecoveryStorage(Storage2 + "v1/vault/restore", authRecoveryDataVo);
        rtn.enqueue(new Callback<AuthResponseVo>(){
            @Override
            public void onResponse(Call<AuthResponseVo> call, Response<AuthResponseVo> response){

                if(response.code() == 200){
                    if(response.body() != null){
                        AuthResponseVo authResponseVo = response.body();
                        boolean result = authResponseVo.getSuccess();
                        recoveryStorage2 = authResponseVo.getResult().getRecoveryClue();
                        dataClueStorage2 = authResponseVo.getResult().getDataClue();
                        PrintLog.e("result = " + result);
                        checkRecoverData();
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(BackupAuthActivity.this, t.getMessage() + " RecoverStorage2", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * checkVerifyToken
     */
    private void checkVerifyToken(){
        PrintLog.e("checkAuthToken");
        PrintLog.e("masterVerifyToken = " + masterVerifyToken);
        PrintLog.e("storage1VerifyToken = " + storage1VerifyToken);
        PrintLog.e("storage2VerifyToken = " + storage2VerifyToken);
        if(masterVerifyToken != null && storage1VerifyToken != null && storage2VerifyToken != null){
            new Handler().postDelayed(this::initLayout2, 1000);
        }
    }

    /**
     * initLayout2
     */
    private void initLayout2(){
        layout.layoutAuth1.setVisibility(View.GONE);
        layout.layoutAuth2.setVisibility(View.VISIBLE);
        layout.toolbar.appbarTitle.setText("이메일 인증");
        layout.requestAuthNumber.setOnClickListener(v -> {
            if(layout.auth1Edit.getText().toString().length() != 0){
                if(!secondClick){
                    requestMasterVerify(layout.auth1Edit.getText().toString().trim());
                }else{
                    requestStorage1Verify(layout.auth2Edit.getText().toString().trim());
                }
            }
        });
    }

    /**
     * initLayout3
     */
    private void initLayout3(){
        layout.layoutAuth2.setVisibility(View.GONE);
        layout.layoutAuth3.setVisibility(View.VISIBLE);
        layout.toolbar.appbarTitle.setText("SMS 인증");
        layout.requestAuthNumber2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                requestStorage2Verify(layout.authSMSEdit.getText().toString());
            }
        });
    }

    /**
     * checkRecoverData
     */
    private void checkRecoverData(){
        PrintLog.e("checkRecoverData");
        PrintLog.e("revocerKey = " + recoveryMasterKey);
        if(recoveryMasterKey != null && recoveryStorage1 != null && recoveryStorage2 != null && dataClueStorage1 != null && dataClueStorage2 != null){
            vaultRecoverData();
        }
    }

    /**
     * vaultBackup
     */
    private void vaultBackup(){
        Vault vault = Vault.getInstance();
        String recoveryKeyHex = vault.createRecoveryKey(); //hex
        RecoveryKey recoveryKey = vault.getRecoveryKey(recoveryKeyHex);
        List<String> spiltRecoveryKey = vault.spiltRecoveryKey(recoveryKey, 2);
        String sharingData = getBackupData();
        List<String> encrypteData = vault.encryptData(recoveryKey, 2, 2, sharingData);
        for(String temp : encrypteData){
            PrintLog.e("data length = " + temp.length());
        }
        requestBackupMaster(recoveryKeyHex);
        requestBackupStorage1(spiltRecoveryKey.get(0), encrypteData.get(0));
        requestBackupStorage2(spiltRecoveryKey.get(1), encrypteData.get(1));

    }

    /**
     * vaultRecover
     */
    private void vaultRecover(){
        layout.progresslayout.setVisibility(View.VISIBLE);
        requestRecoverMaster();
    }

    /**
     * vaultRecoverData
     */
    private void vaultRecoverData(){
        Vault vault = Vault.getInstance();
        List<String> vi = new ArrayList<>();
        List<String> sharedCluesFromServer = new ArrayList<>();
        vi.add(recoveryStorage1);
        vi.add(recoveryStorage2);
        sharedCluesFromServer.add(dataClueStorage1);
        sharedCluesFromServer.add(dataClueStorage2);
        RecoveryKey reconstructKey = vault.mergeRecoveryKey(vi);
        String hexv = vault.getRecoveryKey(reconstructKey);
        String data = vault.decryptData(hexv, sharedCluesFromServer, 2, 2);
        parseRecoverData(data);
    }

    /**
     * parseRecoverData
     * @param data data
     */
    private void parseRecoverData(String data){
        Map<String, String> backupData = IITPApplication.gson.fromJson(data, new TypeToken<Map<String, String>>(){
        }.getType());
        for(String key : backupData.keySet()){
            PrintLog.e("key = " + key);
            if(key.equals(DID_LIST)){
                saveDidList(backupData.get(key));
            }else{
                saveVCData(key, backupData.get(key));
            }
        }
        layout.progresslayout.setVisibility(View.GONE);
        showBackupRecoverDialogFinish("복구", "키/VC 복구가 \n완료되었습니다.");
    }

    /**
     * saveDidList
     * @param didList didList
     */
    private void saveDidList(String didList){
        List<DidDataVo> didDataList = IITPApplication.gson.fromJson(didList, new TypeToken<ArrayList<DidDataVo>>(){
        }.getType());
        for(DidDataVo temp : didDataList){
            if(temp.getBlackChain().equals(BlockChainType.METADIUM)){
                KeyManager keyManager = new KeyManager(temp.getDid());
                keyManager.setIdentity(BackupAuthActivity.this, temp.getDid(), temp.getMnemonic());
            }
//            else if(temp.getBlackChain().equals(BlockChainType.ICON)){
//
//            }else if(temp.getBlackChain().equals(BlockChainType.INDY)){
//
//            }
        }
        String didJson = IITPApplication.gson.toJson(didDataList);
        PrintLog.e("Did json = " + didJson);
        CommonPreference.getInstance(BackupAuthActivity.this).getSecureSharedPreferences().edit().putString(DID_LIST, didJson).apply();
    }

    /**
     * saveVCData
     * @param key key
     * @param vc vc
     */
    private void saveVCData(String key, String vc){
        PrintLog.e("key = " + key);
        PrintLog.e("VC = " + vc);
        CommonPreference.getInstance(BackupAuthActivity.this).getSecureSharedPreferences().edit().putString(key, vc).apply();
    }

    /**
     * getBackupData
     * @return backupDataJson
     */
    private String getBackupData(){
        Map<String, String> backupData = new HashMap<>();
        VCVPCreater vcvpCreater = VCVPCreater.getInstance();
        String didList = CommonPreference.getInstance(BackupAuthActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
        List<String> allVC = preferenceUtil.getAllVCData();
        backupData.put(DID_LIST, didList);
        for(String vc : allVC){
            String payload = preferenceUtil.getPayload(vc);
            JWTClaimsSet jwtClaimsSet = null;
            try{
                jwtClaimsSet = JWTClaimsSet.parse(payload);
            }catch(ParseException e){
               PrintLog.e("getBackupData error");
            }
            MetadiumVerifier verifierTemp = new MetadiumVerifier();
            VerifiableCredential credential = verifierTemp.toCredential(jwtClaimsSet);
            List<String> list = new ArrayList<>(credential.getTypes());
            PrintLog.e("type = " + list.get(1));
            String type = vcvpCreater.getPreferenceKey(list.get(1));
            PrintLog.e("type = " + type);
            backupData.put(type, vc);
        }
        for(String key : backupData.keySet()){
            PrintLog.e("key = " + key);
        }

        String backupDataJson = IITPApplication.gson.toJson(backupData);
        PrintLog.e("backupDataJson = " + backupDataJson);
        return backupDataJson;
    }

    /**
     * email pattern check
     * @param email email
     * @return true/false
     */
    public static boolean isEmail(String email){
        if(email == null){
            return false;
        }
        return Pattern.matches(
                "[\\w\\~\\-\\.]+@[\\w\\~\\-]+(\\.[\\w\\~\\-]+)+",
                email);
    }

    /**
     * mobile pattern check
     * @param phoneNumber mobile num
     * @return true/false
     */
    public static boolean isPhoneNumber(String phoneNumber){
        if(phoneNumber == null) return false;
        String regex = "^01(?:0|1|[6-9])(\\d{4}|\\d{3})(\\d{4})$";
        boolean rtn = Pattern.matches(regex, phoneNumber);
        PrintLog.e("phoneNumber : " + phoneNumber);
        return rtn;
    }
}
