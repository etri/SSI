package com.iitp.iitp_demo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
import com.iitp.iitp_demo.databinding.ActivitySettingBinding;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.util.AsyncTaskResult;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

public class SettingActivity extends BaseActivity{

    private static ActivitySettingBinding layout;
    //    public static final ObservableField<String> progressBarText = new ObservableField<>();
    private static Context ctx;
    private static Activity activity;
    private CommonPreference commPref;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        layout.setActivity(this);
        setSupportActionBar(layout.toolbar.appbar);
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            titleText.setText(R.string.tab_title_3);
//            layout.toolbar.appbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        }
        ctx = this;
        activity = this;
        commPref = CommonPreference.getInstance(ctx);
        layout.selectBlockchainBtn.setOnClickListener(view -> {
            PrintLog.e("select btn");
        });

        layout.generateDidBtn.setOnClickListener(view -> {
            PrintLog.e("generate btn");
        });

//        layout.progressBar.setIndeterminate(true);
//        layout.progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.etri1), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void clickOldMain(){
        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void clickUserList(){
        Intent intent = new Intent(SettingActivity.this, UserListActivity.class);
        startActivity(intent);
        finish();
    }

    public void clickSelectChain(){
        Intent intent = new Intent(SettingActivity.this, SelectChainActivity.class);
        startActivity(intent);
        finish();
    }

    public void clickCreateId(){
        Intent intent = new Intent(SettingActivity.this, SelectDidActivity.class);
        startActivity(intent);
//        KeyManager keyManager = KeyManager.getInstance();
//        keyManager.clear(ctx);
//        new CreateMetaIdTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
    }

    public void click1(){
        Intent i = new Intent(SettingActivity.this, RequestPageActivity.class);
        startActivity(i);
        finish();
    }

    public void click2(){
        Intent i = new Intent(SettingActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void click3(){
        Intent i = new Intent(SettingActivity.this, SettingActivity.class);
        startActivity(i);
        finish();
    }


  /*  static class CreateMetaIdTask extends AsyncTask<String, Integer, AsyncTaskResult<String>>{

        @Override
        protected void onPreExecute(){
            layout.progressBar.setVisibility(View.VISIBLE);
            layout.progressText.setVisibility(View.VISIBLE);
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

            byte[] initialEntropy = new byte[16];
            new SecureRandom().nextBytes(initialEntropy);

            mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
            PrintLog.e("mnemonic = " + mnemonic);
            byte[] entropy = MnemonicUtils.generateEntropy(mnemonic);
            String normalizedMnemonic = MnemonicUtils.generateMnemonic(entropy);
            byte[] seed = MnemonicUtils.generateSeed(normalizedMnemonic, null);
            Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
            keyPair = Bip32ECKeyPair.deriveKeyPair(master, KeyManager.BIP44_META_PATH);

            managementKey = Numeric.prependHexPrefix(Keys.getAddress(keyPair.getPublicKey()));

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
                                        KeyManager.getInstance().addPrivateKey(ctx, mnemonic, KeyManager.BIP44_META_PATH, null);
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
                                }catch(Exception e){
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
                                    KeyManager.getInstance().addPrivateKey(ctx, mnemonic, KeyManager.BIP44_META_PATH, null);
                                    try{
                                        String result = new MetaProxy(Constants.PROXY_URL).addPublicKeyDelegated(
                                                ctx,
                                                web3j,
                                                keyPair,
                                                registryAddress,
                                                managementKey

                                        );
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }

                            }catch(Exception e){
                                //todo
                            }
                        }
                        onProgressUpdate(100);
                        progressBarText.set(ctx.getString(R.string.complete));
                        PrintLog.e("meta id = " + metaId);
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
                ((SettingActivity) activity).commPref.removeKey(Constants.META_ID);
                AlertDialog.Builder dialog = new AlertDialog.Builder(ctx, android.R.style.Theme_DeviceDefault_Light_Dialog);
                // 생성 실패 에러
                dialog.setMessage(ctx.getString(R.string.error_creation_meta_id));
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
//                        activity.finish();
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
                ((SettingActivity) activity).commPref.setValue(Constants.META_ID, result.getResult());
//                ((SettingActivity) activity).sendActivity();
            }
        }
    }*/

    private void sendActivity(){
        finish();
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }


}
