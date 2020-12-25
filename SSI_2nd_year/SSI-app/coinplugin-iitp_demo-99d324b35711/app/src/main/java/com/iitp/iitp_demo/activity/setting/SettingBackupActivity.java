package com.iitp.iitp_demo.activity.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.google.gson.reflect.TypeToken;
import com.iitp.core.crypto.KeyManager;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.BaseActivity;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.PincodeActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.CredentialListVo;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.activity.setting.backup.BackupAuthActivity;
import com.iitp.iitp_demo.api.VaultAPI;
import com.iitp.iitp_demo.api.model.AuthBackupDataVo;
import com.iitp.iitp_demo.api.model.AuthBackupVo;
import com.iitp.iitp_demo.api.model.AuthIdVo;
import com.iitp.iitp_demo.api.model.AuthRecoveryDataVo;
import com.iitp.iitp_demo.api.model.AuthResponseVo;
import com.iitp.iitp_demo.api.model.AuthVerifyVo;
import com.iitp.iitp_demo.api.model.RequestVCVo;
import com.iitp.iitp_demo.chain.VCVPCreater;
import com.iitp.iitp_demo.chain.Vault;
import com.iitp.iitp_demo.databinding.ActivitySettingSecureBinding;
import com.iitp.iitp_demo.util.BiometricUtils;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.FingerBioFactory;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.iitp_demo.util.ToastUtils;
import com.iitp.iitp_demo.util.ViewUtils;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nomadconnection.vault.RecoveryKey;
import com.squareup.otto.Subscribe;

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
import static com.iitp.iitp_demo.activity.PincodeActivity.PIN_SETTING_TYPE;
import static com.iitp.iitp_demo.api.VaultAPIInfo.Storage1;
import static com.iitp.iitp_demo.api.VaultAPIInfo.Storage2;

public class SettingBackupActivity extends BaseActivity{


    private ActivitySettingSecureBinding layout;
    private RequestVCVo requestVCVo;
    private String json;
    private ArrayList<CredentialListVo> listItem = new ArrayList<CredentialListVo>();


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
    private int index = 0;
    private PreferenceUtil preferenceUtil;
    private RelativeLayout pass2Layout;
    private boolean secondClick = false;
    ImageView authCode2Image;
    ImageView authCode1Image;
    EditText authCode1Edit;
    EditText authCode2Edit;
    EditText authCode3Edit;
    private TextView textViewSecond;
    private static final int REQUEST_CODE_VERIFY_PIN_APP_LOCK = 0x0010;
    private boolean checkBio;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_setting_secure);
        setActionBarSet(layout.toolbar, getString(R.string.setting_backup), true);
        preferenceUtil = PreferenceUtil.getInstance(this);
        initView();
    }

    @Override
    protected void onResume(){
        super.onResume();
        checkBio = false;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    /**
     * init view
     */
    private void initView(){
        addListItem();
        SettingMenuListAdapter adapter = new SettingMenuListAdapter();
        layout.menuList.setAdapter(adapter);
    }

    private void resetData(){
        masterVerifyToken = null;
        storage1VerifyToken = null;
        storage2VerifyToken = null;
        masterAuthToken = null;
        storage1AuthToken = null;
        storage2AuthToken = null;
        recoveryMasterKey = null;
        recoveryStorage1 = null;
        dataClueStorage1 = null;
        recoveryStorage2 = null;
        dataClueStorage2 = null;
        index = 0;
        secondClick = false;
    }

    /**
     * add list item
     */
    private void addListItem(){
        listItem.add(new CredentialListVo(R.drawable.ic_settings_backup, getString(R.string.setting_backup), getString(R.string.setting_backup_desc), null, 1));
        listItem.add(new CredentialListVo(R.drawable.ic_settings_backup, getString(R.string.setting_restore), getString(R.string.setting_restore_desc), null, 2));
    }

    /**
     * list adapter
     */
    class SettingMenuListAdapter extends BaseAdapter{

        SettingMenuListAdapter(){

        }

        @Override
        public int getCount(){
            return listItem.size();
        }

        @Override
        public Object getItem(int i){
            return null;
        }

        @Override
        public long getItemId(int i){
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent){
            SettingMenuListAdapter.Holder holder;
            CredentialListVo data = listItem.get(i);
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.list_item_credential, parent, false);
                holder = new SettingMenuListAdapter.Holder();
                holder.icon = convertView.findViewById(R.id.icon);
                holder.desc1 = convertView.findViewById(R.id.desc1);
                holder.desc2 = convertView.findViewById(R.id.desc2);
                holder.layout = convertView.findViewById(R.id.layout);
                convertView.setTag(holder);
            }else{
                holder = (SettingMenuListAdapter.Holder) convertView.getTag();
            }


            holder.icon.setBackgroundResource(data.getImageIcon());
            holder.desc1.setText(data.getDesc1());
            holder.desc2.setText(data.getDesc2());
            holder.layout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(i == 0){
                        String json = CommonPreference.getInstance(SettingBackupActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
                        if(json != null){
                            pinCode(i);
                        }else{
                            ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "백업할 데이터가 없습니다.", Toast.LENGTH_SHORT)).show();
                        }
//                        sendPush();
                    }else{
                        pinCode(i);
                    }
                    PrintLog.e("click : " + i);
                }
            });
            return convertView;
        }

        /**
         * holder
         */
        class Holder{
            ImageView icon;
            TextView desc1;
            TextView desc2;
            ConstraintLayout layout;
        }


    }


    /**
     * finish dialog
     *
     * @param text
     */
    private void showDialogFinish(String text){
        LayoutInflater inflater = (LayoutInflater) SettingBackupActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        AlertDialog customDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        float width = ViewUtils.dp2px(this, 320);
        float height = ViewUtils.dp2px(this, 252);
        customDialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        TextView textview = dialogView.findViewById(R.id.messageTextView);
        Button btPositive = dialogView.findViewById(R.id.cancel1);
        LinearLayout twoBtn = dialogView.findViewById(R.id.twoBtn);
        LinearLayout oneBtn = dialogView.findViewById(R.id.oneBtn);
        title.setText(text);
        twoBtn.setVisibility(View.GONE);
        oneBtn.setVisibility(View.VISIBLE);
        btPositive.setText(R.string.ok);
        textview.setText(getString(R.string.setting_prevc_dialog));
        btPositive.setOnClickListener(v -> {
            customDialog.dismiss();
        });

        customDialog.show();
    }

    private AlertDialog password1Dialog;
    private AlertDialog password2Dialog;
    private AlertDialog password3Dialog;
    private AlertDialog FinishDialog;

    /**
     * finish dialog
     *
     * @param text
     */

    private void showDialogFirstPassword(){
        LayoutInflater inflater = (LayoutInflater) SettingBackupActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_password, null);
        password1Dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener(){
                    @Override
                    public void onCancel(DialogInterface dialog){
                        resetData();
                    }
                })
                .show();
        float width = ViewUtils.dp2px(this, 320);
        float height = ViewUtils.dp2px(this, 300);
        password1Dialog.getWindow().setLayout((int) width, (int) height);

        TextView title = dialogView.findViewById(R.id.titleTextView);
        Button btPositive = dialogView.findViewById(R.id.next);

        EditText emailEditText = dialogView.findViewById(R.id.emailEdit);
        EditText telEditText = dialogView.findViewById(R.id.telEdit);
        emailEditText.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback(){
            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu){
                return false;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu){
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item){
                return false;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode){

            }
        });

        telEditText.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback(){
            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu){
                return false;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu){
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item){
                return false;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode){

            }
        });


        title.setText(R.string.backupRequestAuth);
        btPositive.setText(R.string.backupSendNum);
        btPositive.setOnClickListener(v -> {
            if(isEmail(emailEditText.getText().toString()) && isPhoneNumber(telEditText.getText().toString())){
                requestMasterInit(emailEditText.getText().toString());
                requestStorage1Init(emailEditText.getText().toString());
                requestStorage2Init(telEditText.getText().toString());
            }else{
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "이메일이나 전화번호가 형식에 맞지 않습니다.", Toast.LENGTH_SHORT)).show();
            }
        });
        password1Dialog.show();
    }


    /**
     * finish dialog
     *
     * @param text
     */

    private void showDialogSecondPassword(){
        LayoutInflater inflater = (LayoutInflater) SettingBackupActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_password2, null);
        password2Dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener(){
                    @Override
                    public void onCancel(DialogInterface dialog){
                        resetData();
                    }
                })
                .show();
        float width = ViewUtils.dp2px(this, 320);
        float height = ViewUtils.dp2px(this, 300);
        password2Dialog.getWindow().setLayout((int) width, (int) height);
        TextView title = dialogView.findViewById(R.id.titleTextView);
        Button btPositive = dialogView.findViewById(R.id.next);

        authCode1Edit = dialogView.findViewById(R.id.authCode1Edit);
        authCode1Edit.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback(){
            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu){
                return false;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu){
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item){
                return false;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode){

            }
        });
        pass2Layout = dialogView.findViewById(R.id.secondLayout);
        authCode2Edit = dialogView.findViewById(R.id.authCode2Edit);
        authCode2Edit.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback(){
            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu){
                return false;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu){
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item){
                return false;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode){

            }
        });
        textViewSecond = dialogView.findViewById(R.id.text2);
        authCode1Image = dialogView.findViewById(R.id.authCode1Img);
        authCode2Image = dialogView.findViewById(R.id.authCode2Img);
        title.setText("이메일 인증");
        btPositive.setText(R.string.backupNumVerify);
        btPositive.setOnClickListener(v -> {
            if(authCode1Edit.getText().toString().length() != 0){
                if(!secondClick){
                    requestMasterVerify(authCode1Edit.getText().toString().trim());
                }else{
                    requestStorage1Verify(authCode2Edit.getText().toString().trim());
                }
            }
        });
        password2Dialog.show();
    }

    /**
     * finish dialog
     *
     * @param text
     */

    private void showDialogThirdPassword(){
        LayoutInflater inflater = (LayoutInflater) SettingBackupActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_password1, null);
        password3Dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener(){
                    @Override
                    public void onCancel(DialogInterface dialog){
                        resetData();
                    }
                })
                .show();
        float width = ViewUtils.dp2px(this, 300);
        float height = ViewUtils.dp2px(this, 252);
        password3Dialog.getWindow().setLayout((int) width, (int) height);

        TextView title = dialogView.findViewById(R.id.titleTextView);
        Button btPositive = dialogView.findViewById(R.id.cancel1);
        authCode3Edit = dialogView.findViewById(R.id.authCode1Edit);
        authCode3Edit.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback(){
            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu){
                return false;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu){
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item){
                return false;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode){

            }
        });
        LinearLayout layout1 = dialogView.findViewById(R.id.layout1);
        LinearLayout layout2 = dialogView.findViewById(R.id.layout2);
        layout1.setVisibility(View.VISIBLE);
        layout2.setVisibility(View.GONE);
        title.setText("SMS 인증");
        btPositive.setText(R.string.backupNumVerify);
        btPositive.setOnClickListener(v -> {
            requestStorage2Verify(authCode3Edit.getText().toString());
        });
        password3Dialog.show();
    }

    private void showBackupRecoverDialogFinish(String titleText, String messageText){
        LayoutInflater inflater = (LayoutInflater) SettingBackupActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            finish();
        });
        FinishDialog.show();
    }

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
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " MasterInit", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

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
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " Storage1Init", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

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
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " Storage2Init", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

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
                            pass2Layout.setVisibility(View.VISIBLE);
                            authCode2Edit.requestFocus();
                            authCode1Image.setVisibility(View.VISIBLE);
                            textViewSecond.setVisibility(View.VISIBLE);
                            secondClick = true;
                        }else{
                            authCode1Edit.setText("");
                            authCode1Edit.requestFocus();
                            ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "인증코드가 틀렸습니다.다시 시도해주세요", Toast.LENGTH_SHORT)).show();
                        }
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " MasterVerify", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

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
                            authCode2Image.setVisibility(View.VISIBLE);
                            PrintLog.e("storage1 AuthToken = " + storage1AuthToken);
                            password2Dialog.dismiss();
                            password2Dialog = null;
                            secondClick = false;
                            pass2Layout.setVisibility(View.INVISIBLE);
                            textViewSecond.setVisibility(View.INVISIBLE);
                            authCode2Image.setVisibility(View.INVISIBLE);
                            authCode1Image.setVisibility(View.INVISIBLE);
                            showDialogThirdPassword();
                        }else{
                            authCode2Edit.setText("");
                            authCode2Edit.requestFocus();
                            ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "인증코드가 틀렸습니다.다시 시도해주세요", Toast.LENGTH_SHORT)).show();
                        }
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " Storage1Verify", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

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
                            password3Dialog.dismiss();
                            password3Dialog = null;
                            layout.progresslayout.setVisibility(View.VISIBLE);
                            if(index == 0){
                                vaultBackup();
                            }else if(index == 1){
                                vaultRecover();
                            }
                        }else{
                            authCode3Edit.setText("");
                            authCode3Edit.requestFocus();
                            ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "인증코드가 틀렸습니다.다시 시도해주세요", Toast.LENGTH_SHORT)).show();
                        }
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, response.code() + " Server error", Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " Storage2Verify", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

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
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " BackupMaster", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

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
                        //todo 완료 팝업
                    }
                }else{
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " BackupStorage1", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

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
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " BackupStorage2", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }


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
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " RecoverMaster", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

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
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " RecoverStorage1", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

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
                    ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, "Server error " + response.code(), Toast.LENGTH_SHORT)).show();
                    layout.progresslayout.setVisibility(View.GONE);
                    PrintLog.e("response = " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<AuthResponseVo> call, Throwable t){
                PrintLog.e("fail = " + t.getMessage());
                ToastUtils.custom(Toast.makeText(SettingBackupActivity.this, t.getMessage() + " RecoverStorage2", Toast.LENGTH_SHORT)).show();
                layout.progresslayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * pincode 입력
     */
    private void pinCode(int i){
        index = i;
        BiometricUtils.hasBiometricEnrolled(this);
        if((BiometricUtils.getPincode(this) == null && !(BiometricUtils.isFingerPrint(this)))){
            if(BiometricUtils.checkFingerprint(this)){
                fingerBiofactory(this);
            }else{
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startPincodeActivity();
                    }
                }, 1000);
            }
        }else{
            if(BiometricUtils.isFingerPrint(this)){
                fingerBiofactory(this);
            }else{
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startPincodeActivity();
                    }
                }, 1500);
            }
        }
    }

    private void startPincodeActivity(){
        Intent intent;
        intent = new Intent(this, PincodeActivity.class);
        intent.putExtra(PIN_SETTING_TYPE, 2);
        startActivityForResult(intent, REQUEST_CODE_VERIFY_PIN_APP_LOCK);
        //finish();
    }

    private void checkVerifyToken(){
        PrintLog.e("checkAuthToken");
        PrintLog.e("masterVerifyToken = " + masterVerifyToken);
        PrintLog.e("storage1VerifyToken = " + storage1VerifyToken);
        PrintLog.e("storage2VerifyToken = " + storage2VerifyToken);
        if(masterVerifyToken != null && storage1VerifyToken != null && storage2VerifyToken != null){
            password1Dialog.dismiss();
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    showDialogSecondPassword();
                }
            }, 1000);

        }
    }

    private void checkRecoverData(){
        PrintLog.e("checkRecoverData");
        PrintLog.e("revocerKey = " + recoveryMasterKey);
        if(recoveryMasterKey != null && recoveryStorage1 != null && recoveryStorage2 != null && dataClueStorage1 != null && dataClueStorage2 != null){
            vaultRecoverData();
        }
    }

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

    private void vaultRecover(){
        layout.progresslayout.setVisibility(View.VISIBLE);
        requestRecoverMaster();


    }

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
//        PrintLog.e("data = " + data);
        parseRecoverData(data);
    }

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

    private void saveDidList(String didList){
        List<DidDataVo> didDataList = IITPApplication.gson.fromJson(didList, new TypeToken<ArrayList<DidDataVo>>(){
        }.getType());
        for(DidDataVo temp : didDataList){
            if(temp.getBlackChain().equals(BlockChainType.METADIUM)){
                KeyManager keyManager = new KeyManager(temp.getDid());
                keyManager.setIdentity(SettingBackupActivity.this, temp.getDid(), temp.getMnemonic());
            }else if(temp.getBlackChain().equals(BlockChainType.ICON)){

            }else if(temp.getBlackChain().equals(BlockChainType.INDY)){

            }
        }
        String didJson = IITPApplication.gson.toJson(didDataList);
        PrintLog.e("Did json = " + didJson);
        CommonPreference.getInstance(SettingBackupActivity.this).getSecureSharedPreferences().edit().putString(DID_LIST, didJson).apply();
    }

    private void saveVCData(String key, String vc){
        PrintLog.e("key = " + key);
        PrintLog.e("VC = " + vc);
        CommonPreference.getInstance(SettingBackupActivity.this).getSecureSharedPreferences().edit().putString(key, vc).apply();
    }

    private String getBackupData(){
        Map<String, String> backupData = new HashMap<>();
        VCVPCreater vcvpCreater = VCVPCreater.getInstance();
        String didList = CommonPreference.getInstance(SettingBackupActivity.this).getSecureSharedPreferences().getString(DID_LIST, null);
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
//        return "testDATA~~~~~~~~~~~~~~~`";
    }

    /**
     * check email type
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email){
        if(email == null){
            return false;
        }
        return Pattern.matches(
                "[\\w\\~\\-\\.]+@[\\w\\~\\-]+(\\.[\\w\\~\\-]+)+",
                email);
    }

    public static boolean isPhoneNumber(String phoneNumber){
        if(phoneNumber == null) return false;
        String regex = "^01(?:0|1|[6-9])(\\d{4}|\\d{3})(\\d{4})$";
        boolean rtn = Pattern.matches(regex, phoneNumber);
        PrintLog.e("phoneNumber : " + phoneNumber);
        return rtn;
    }


    private void fingerBiofactory(Context ctx){
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
                    if(!checkBio){
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
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startBackupAuthActivity();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CODE_VERIFY_PIN_APP_LOCK){
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startBackupAuthActivity();
                    }
                }, 200);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startBackupAuthActivity(){
        Intent intent;
        intent = new Intent(this, BackupAuthActivity.class);
        intent.putExtra("TYPE", index);
        startActivity(intent);

    }

    @Subscribe
    public void finishActivity(String message){
        PrintLog.e("message = " + message);
        if(message.equals("SettingBackupActivityFinish")){
         finish();
        }
    }
}
