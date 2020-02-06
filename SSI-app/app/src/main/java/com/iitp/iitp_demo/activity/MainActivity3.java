package com.iitp.iitp_demo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

import com.iitp.core.contract.IdentityRegistry;
import com.iitp.core.crypto.KeyManager;
import com.iitp.core.identity.Identity;
import com.iitp.core.protocol.MetaProxy;
import com.iitp.core.protocol.Web3jBuilder;
import com.iitp.core.protocol.data.RegistryAddress;
import com.iitp.core.util.Web3jUtils;
import com.iitp.core.wapper.NotSignTransactionManager;
import com.iitp.core.wapper.ZeroContractGasProvider;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.data.IdentityStore;
import com.iitp.iitp_demo.data.RegistryManager;
import com.iitp.iitp_demo.databinding.ActivityMain3Binding;
import com.iitp.iitp_demo.databinding.ActivityMainBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.KeyManagerUtil;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.util.AsyncTaskResult;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

public class MainActivity3 extends AppCompatActivity{

    private static Context ctx;
    private static Activity activity;
    public String btnString = "";

    private static ActivityMain3Binding layout;
    private CommonPreference commPref;
    public static final ObservableField<String> metaId = new ObservableField<>();

    public static final ObservableField<String> progressBarText = new ObservableField<>();
    public String idCredential = null;
    public String officeCredential = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_main);
        layout.setActivity(this);
        ctx = this;
        activity = this;
        setSupportActionBar(layout.toolbar.appbar);
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            titleText.setText(R.string.title);
//            layout.toolbar.appbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        }

        commPref = CommonPreference.getInstance(ctx);
//        metaId.set(commPref.getStringValue(Constants.META_ID, null));
//        if(metaId.get() == null){
//            btnString = getString(R.string.gendid);
//            layout.noMeataIdTv.setVisibility(View.VISIBLE);
//            layout.hasDidLayout.setVisibility(View.INVISIBLE);
//        }else{
//            btnString = getString(R.string.finish);
//            layout.noMeataIdTv.setVisibility(View.INVISIBLE);
//            layout.hasDidLayout.setVisibility(View.VISIBLE);
//
//        }

        layout.progressBar.setIndeterminate(true);
        layout.progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.etri1), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    protected void onResume(){
        super.onResume();
        setMetaId();
    }

    private void setMetaId(){
        String did = KeyManagerUtil.getDid(this);
        metaId.set(did);
//        metaId.set(commPref.getStringValue(Constants.META_ID, null));
        if(metaId.get() == null){
            btnString = getString(R.string.gendid);
            layout.noMeataIdTv.setVisibility(View.VISIBLE);
            layout.hasDidLayout.setVisibility(View.INVISIBLE);
        }else{
            btnString = getString(R.string.ok);
            layout.noMeataIdTv.setVisibility(View.INVISIBLE);
            layout.hasDidLayout.setVisibility(View.VISIBLE);

        }
        SharedPreferences secure = commPref.getSecureSharedPreferences();
        idCredential = secure.getString(Constants.ID_CREDENTIAL, null);
        officeCredential = secure.getString(Constants.OFFICE_CREDENTIAL, null);
        if(idCredential == null){
            layout.credential1.setVisibility(View.INVISIBLE);
        }else{
            layout.credential1.setVisibility(View.VISIBLE);
        }

        if(officeCredential == null){
            layout.credential2.setVisibility(View.INVISIBLE);
        }else{
            layout.credential2.setVisibility(View.VISIBLE);
        }
        if(idCredential == null && officeCredential == null){
            layout.hasCredentialLayout.setVisibility(View.INVISIBLE);
        }else{
            layout.hasCredentialLayout.setVisibility(View.VISIBLE);
        }

    }

    public void btnClick(){
        if(metaId.get() == null){
            new CreateMetaIdTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
        }else{
            finish();
        }
    }

    public void btnTitleClick(){
        Intent intent = new Intent(MainActivity3.this, SettingActivity.class);
        startActivity(intent);
    }

    private void sendActivity(){
        Intent intent = new Intent(MainActivity3.this, FinishGenerateDidActivity.class);
        startActivity(intent);
        finish();
    }

    public void credential1Click(){
        Intent intent = new Intent(MainActivity3.this, FinishGenerateIdCredentialActivity.class);
        startActivity(intent);
        finish();
    }


    public void credential2Click(){
        Intent intent = new Intent(MainActivity3.this, FinishGenerateOfficeCredentialActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * MetaID 생성 처리<br/>
     * {@link CreateMetaIdTask#execute(Object[])} 의 첫번째 parameter 는 MetaID 에 생성에 사용될 key 의 address<br/>
     * null 이며 내부적으로 key 를 생성하고 {@link KeyManager} 에 key 를 추가한다.
     */
    static class CreateMetaIdTask extends AsyncTask<String, Integer, AsyncTaskResult<String>>{

        @Override
        protected void onPreExecute(){
            layout.progressBar.setVisibility(View.VISIBLE);
            layout.progressText.setVisibility(View.VISIBLE);
            layout.noMeataIdTv.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values){
//            super.onProgressUpdate(values);
            layout.progressBar.setProgress(values[0].intValue());
        }

        @SuppressLint("WrongThread")
        @Override
        protected AsyncTaskResult<String> doInBackground(String... strings){


            onProgressUpdate(0);
            progressBarText.set(ctx.getString(R.string.genkey));

            String managementKey = strings[0];
            String mnemonic;
            ECKeyPair keyPair = null;

            // key 가 존재하지 않으면 key 를 생성한다.
            if(managementKey == null){
                byte[] initialEntropy = new byte[16];
                new SecureRandom().nextBytes(initialEntropy);

                mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
                PrintLog.e("mnemonic = " + mnemonic);
                byte[] entropy = MnemonicUtils.generateEntropy(mnemonic);
                String normalizedMnemonic = MnemonicUtils.generateMnemonic(entropy);
                byte[] seed = MnemonicUtils.generateSeed(normalizedMnemonic, null);
                Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
                keyPair = Bip32ECKeyPair.deriveKeyPair(master, KeyManager.getInstance().getMetaPath());

                managementKey = Numeric.prependHexPrefix(Keys.getAddress(keyPair.getPublicKey()));
            }else{
                KeyManager keyManager = KeyManager.getInstance();
                mnemonic = keyManager.getMnemonic(ctx, managementKey);
                byte[] entropy = MnemonicUtils.generateEntropy(mnemonic);
                String normalizedMnemonic = MnemonicUtils.generateMnemonic(entropy);
                byte[] seed = MnemonicUtils.generateSeed(normalizedMnemonic, null);
                Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
                keyPair = Bip32ECKeyPair.deriveKeyPair(master, KeyManager.getInstance().getMetaPath());
            }
            onProgressUpdate(20);
            progressBarText.set(ctx.getString(R.string.regaddress));
            RegistryAddress registryAddress = RegistryManager.getRegistryAddress(ctx);
            if(registryAddress == null){
                return new AsyncTaskResult<>(new Exception("RegistryAddress is null"));
            }

            Web3j web3j = Web3jBuilder.build(Constants.META_API_NODE_URL);

            Boolean hasId = false;
            onProgressUpdate(40);
            progressBarText.set(ctx.getString(R.string.checkid));
            IdentityRegistry identityRegistry = IdentityRegistry.load(registryAddress.identityRegistry, web3j, new NotSignTransactionManager(web3j), new ZeroContractGasProvider());
            try{
                // ID가 등록되어 있는지 확인한다.
                hasId = identityRegistry.hasIdentity(managementKey).send();
            }catch(Exception e){
                //todo
                return new AsyncTaskResult<>(e);
            }

            TransactionReceipt transactionReceipt;
            String transactionHash;
            try{
                // MetaID 생성을 Proxy 에 요청한다.
                transactionHash = new MetaProxy(Constants.PROXY_URL).createIdentityDelegated(
                        ctx,
                        web3j,
                        keyPair,
                        registryAddress,
                        managementKey,
                        managementKey
                );
            }catch(Exception e){
                return new AsyncTaskResult<>(e);
            }
            progressBarText.set(ctx.getString(R.string.regchain));

            onProgressUpdate(50);
            try{
                if(!hasId){
                    // Get transaction receipt
                    transactionReceipt = Web3jUtils.ethGetTransactionReceipt(web3j, transactionHash);
                    onProgressUpdate(60);
                    progressBarText.set(ctx.getString(R.string.genmetaid));

                    if(transactionReceipt.getStatus().equals("0x1")){
                        // receipt 의 로그에서 MetaID 생성 event 를 찾어 MetaID 로 사용될 ein 값을 조회한다.
                        List<IdentityRegistry.IdentityCreatedEventResponse> responses = identityRegistry.getIdentityCreatedEvents(transactionReceipt);
                        if(responses.size() > 0){
                            String metaId = Numeric.toHexStringWithPrefixZeroPadded(responses.get(0).ein, 32);

                            onProgressUpdate(90);
                            progressBarText.set(ctx.getString(R.string.savekey));
                            // Identity 및 key 저장
                            if(IdentityStore.saveIdentity(ctx, new Identity(metaId, managementKey))){
                                if(KeyManager.getInstance().getPrivateKey(ctx, managementKey) == null){
                                    if(mnemonic != null){
                                        KeyManager.getInstance().addPrivateKey(ctx, mnemonic, KeyManager.getInstance().getMetaPath(), null);
                                    }
                                }

                                try{
                                    String result = new MetaProxy(Constants.PROXY_URL).addPublicKeyDelegated(
                                            ctx,
                                            web3j,
                                            keyPair,
                                            registryAddress,
                                            managementKey

                                    );
                                    TransactionReceipt addPublicReceipt = Web3jUtils.ethGetTransactionReceipt(web3j, result);
                                    if(!transactionReceipt.getStatus().equals("0x1")){
                                        return new AsyncTaskResult<>(new Exception("Add Publickey TransactionReceipt.status is " + addPublicReceipt.getStatus()));
                                    }
                                }catch(IOException e){
                                    return new AsyncTaskResult<>(e);
                                }
                                onProgressUpdate(100);
                                progressBarText.set(ctx.getString(R.string.complete));
                                return new AsyncTaskResult<>(metaId);
                            }else{
                                return new AsyncTaskResult<>(new Exception("Error save Meta ID"));
                            }
                        }else{
                            return new AsyncTaskResult<>(new Exception("TransactionReceipt.logs is empty"));
                        }
                    }else{
                        return new AsyncTaskResult<>(new Exception("TransactionReceipt.status is " + transactionReceipt.getStatus()));
                    }
                }else{
                    BigInteger ein = BigInteger.ZERO;
                    ein = identityRegistry.getEIN(managementKey).send();
                    String metaId = Numeric.toHexStringWithPrefixZeroPadded(ein, 64);
                    onProgressUpdate(90);
                    progressBarText.set(ctx.getString(R.string.savekey));
                    // Identity 및 key 저장
                    if(IdentityStore.saveIdentity(ctx, new Identity(metaId, managementKey))){
                        if(mnemonic != null){
                            try{
                                BigInteger pk = KeyManager.getInstance().getPrivateKey(ctx, managementKey);
                                if(pk == null){
                                    KeyManager.getInstance().addPrivateKey(ctx, mnemonic, KeyManager.getInstance().getMetaPath(), null);
                                    try{
                                        String result = new MetaProxy(Constants.PROXY_URL).addPublicKeyDelegated(
                                                ctx,
                                                web3j,
                                                keyPair,
                                                registryAddress,
                                                managementKey

                                        );
                                    }catch(IOException e){
                                        e.printStackTrace();
                                    }
                                }

                            }catch(Exception e){
                                PrintLog.e("exception error ");
                            }
                        }
                        onProgressUpdate(100);
                        progressBarText.set(ctx.getString(R.string.complete));
                        return new AsyncTaskResult<>(metaId);
                    }else{
                        return new AsyncTaskResult<>(new Exception("Error save Meta ID"));
                    }
                }
            }catch(Exception e){
                return new AsyncTaskResult<>(e);
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<String> result){
            layout.progressBar.setVisibility(View.INVISIBLE);
            if(result.getError() != null){
                ((MainActivity3) activity).commPref.removeKey(Constants.META_ID);
                AlertDialog.Builder dialog = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Light_Dialog);
                // 생성 실패 에러
                dialog.setMessage(ctx.getString(R.string.error_creation_meta_id));
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
                        activity.finish();
                    }
                });
                dialog.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        // MetaID 생성 재시도
                        new CreateMetaIdTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }else{
                // go main
                ((MainActivity3) activity).commPref.setValue(Constants.META_ID, result.getResult());
                ((MainActivity3) activity).sendActivity();
            }
        }
    }

    public void testClick(){
        sendTestActivity();
        finish();
    }

    private void sendTestActivity() {
        Intent intent = new Intent(MainActivity3.this, TestPageActivity.class);
        startActivity(intent);
    }
}
