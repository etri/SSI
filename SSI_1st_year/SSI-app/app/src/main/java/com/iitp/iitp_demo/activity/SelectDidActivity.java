package com.iitp.iitp_demo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.iitp.iitp_demo.activity.adapter.DidListAdapter;
import com.iitp.iitp_demo.data.IdentityStore;
import com.iitp.iitp_demo.data.RegistryManager;
import com.iitp.iitp_demo.databinding.ActivitySelectDidBinding;
import com.iitp.iitp_demo.model.DidVo;
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
import java.util.ArrayList;
import java.util.List;

public class SelectDidActivity extends BaseActivity{

    private ActivitySelectDidBinding layout;
    private Context ctx;
    private Activity activity;
    private CommonPreference commPref;
    public ObservableField<String> progressBarText = new ObservableField<>();
    private DidVo did = new DidVo();
    private List<DidVo> didListData = new ArrayList<>();
    private DidListAdapter adapter;
    private static String list = null;
    private static Gson gson = new Gson();
    public int index = 0;
    private static boolean first = false;
    public ObservableField<String> didtext = new ObservableField<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_select_did);
        layout.setActivity(this);
        setSupportActionBar(layout.toolbar.appbar);
        commPref = CommonPreference.getInstance(this);
        ctx = this;
        activity = this;
        list = commPref.getStringValue(Constants.ALL_DID_DATA, null);
        index = commPref.getIntValue(Constants.DID_DATA_INDEX,0);
        if(list == null){
            first = true;
        }else{
            first = false;
        }
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            if(list == null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }else{
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                layout.toolbar.appbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            }
            getSupportActionBar().setDisplayShowHomeEnabled(false);

            titleText.setText(R.string.select_did);

        }
//        layout.linearLayout2.setVisibility(View.GONE);
        if(list != null){
            PrintLog.e("get did list = " + list);
//            layout.didTv.setVisibility(View.GONE);
            layout.didList.setVisibility(View.VISIBLE);
            didListData = gson.fromJson(list, new TypeToken<ArrayList<DidVo>>(){
            }.getType());
            didtext.set(didListData.get(index).getIss());
//            layout.didTv.setText(didtext.get());
            layout.didTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
            layout.didTv.setGravity(Gravity.START);
        }else{
            layout.didTv.setVisibility(View.VISIBLE);
            layout.didList.setVisibility(View.GONE);
            didtext.set(getString(R.string.mainmessage1));
//            layout.didTv.setText(didtext.get());
            layout.didTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,32);
            layout.didTv.setGravity(Gravity.CENTER);
        }
        adapter = new DidListAdapter(this, didListData, getLayoutInflater());
        layout.didList.setAdapter(adapter);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item){
//        switch(item.getItemId()){
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    public void click1(){
//
//        Intent i = new Intent(SelectDidActivity.this, RequestPageActivity.class);
//        startActivity(i);
//        finish();
//
//    }
//
//    public void click2(){
//
//        Intent i = new Intent(SelectDidActivity.this, MainActivity.class);
//        startActivity(i);
//        finish();
//
//    }
//
//    public void click3(){
//        Intent i = new Intent(SelectDidActivity.this, SettingActivity.class);
//        startActivity(i);
//        finish();
//    }

    class CreateMetaIdTask extends AsyncTask<String, Integer, AsyncTaskResult<String>>{

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
            keyPair = Bip32ECKeyPair.deriveKeyPair(master, KeyManager.getInstance().getMetaPath());

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
                                //todo
                                PrintLog.e("exception error ");
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
                ((SelectDidActivity) activity).commPref.removeKey(Constants.META_ID);
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

                did = KeyManagerUtil.getDidVo(ctx);
                PrintLog.e("DID = " + did.getIss());

                if(list == null){
                    didListData.add(did);
                }else{
                    didListData = gson.fromJson(list, new TypeToken<ArrayList<DidVo>>(){
                    }.getType());
                    didListData.add(did);
                }
                adapter.updateItemList(didListData);
                list = gson.toJson(didListData);
                PrintLog.e("set did lsit = " + list);
                index= didListData.size()-1;
                commPref.setValue(Constants.ALL_DID_DATA, list);
                commPref.setValue(Constants.DID_DATA_INDEX, index);
                if(first){
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Intent i = new Intent(ctx, MainActivity2.class);
                            ctx.startActivity(i);
                            ((Activity)ctx).finish(); //여기에 딜레이 후 시작할 작업들을 입력
                        }
                    }, 500);// 0.5초 정도 딜레이를 준 후 시작

                }else{
                    didtext.set(didListData.get(index).getIss());
                    layout.progressText.setVisibility(View.INVISIBLE);
//                    layout.didTv.setText(didtext.get());
                    layout.didTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                    layout.didTv.setGravity(Gravity.START);
                    layout.didList.setVisibility(View.VISIBLE);
                    layout.createDid.setEnabled(true);
                }

            }
        }
    }

    public void clickCreateId(){
        KeyManager keyManager = KeyManager.getInstance();
        keyManager.clear(ctx);
        layout.createDid.setEnabled(false);
        new CreateMetaIdTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
//        commPref.setValue(Constants.DID_DATA_INDEX, index);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        PrintLog.e("onDestroy");
        commPref.setValue(Constants.DID_DATA_INDEX, index);
    }
}
