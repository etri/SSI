package com.iitp.iitp_demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.adapter.TabPagerAdatper;
import com.iitp.iitp_demo.activity.fragment.CredentialListFragment;
import com.iitp.iitp_demo.activity.fragment.DidsFragment;
import com.iitp.iitp_demo.activity.fragment.QRCodeScanFragment;
import com.iitp.iitp_demo.activity.fragment.SettingFragment;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.api.model.MessageVo;
import com.iitp.iitp_demo.chain.indy.Indy;
import com.iitp.iitp_demo.databinding.ActivityMainBinding;
import com.iitp.iitp_demo.util.BackPressCloseHandler;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.ArrayList;
import java.util.List;

import jnr.ffi.annotations.In;

import static com.iitp.iitp_demo.Constants.DID_LIST;

public class MainActivity extends AppCompatActivity{

    private static Context ctx;
    private static Activity activity;

    private static ActivityMainBinding layout;

    private TextView titleText;
    private int tabIndex = 0;
    private Intent intent = null;
    private BackPressCloseHandler backPressCloseHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_main);
        layout.setActivity(this);
        ctx = this;
        activity = this;
        backPressCloseHandler = new BackPressCloseHandler(this);
        intent = getIntent();
        String messageStock = intent.getStringExtra("stockMessage");
        if(messageStock != null){
            PrintLog.e("Stock message = "+messageStock);
            sendStockApp(messageStock);
        }
        tabIndex = intent.getIntExtra(Constants.TAB_INDEX, -1);
        setSupportActionBar(layout.toolbar.appbar);
        if(getSupportActionBar() != null){
            titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            titleText.setText(R.string.tab_credential);
        }
        initViewPager();
//        checkNoneZKPVC();//todo vc가 없는 경우 vc 생성

    }

    @Override
    protected void onNewIntent(Intent intent){
        PrintLog.e("onNewIntent");
        super.onNewIntent(intent);
        String messageStock = intent.getStringExtra("stockMessage");
        if(messageStock != null){
            PrintLog.e("Stock message = "+messageStock);
            sendStockApp(messageStock);
        }
    }

    @Override
    protected void onResume(){

        PrintLog.e("onResume");
        PrintLog.e("tab index data = " + tabIndex);
        if(tabIndex != -1){
            layout.viewpager.setCurrentItem(0);
        }
        super.onResume();
    }

    private List<Fragment> listFragment = new ArrayList<>();

    private void initViewPager(){
        listFragment.add(CredentialListFragment.newInstance());
        listFragment.add(DidsFragment.newInstance());
        listFragment.add(QRCodeScanFragment.newInstance());
        listFragment.add(SettingFragment.newInstance());
        layout.tabLayout.addTab(layout.tabLayout.newTab().setText(getString(R.string.tab_credential)));
        layout.tabLayout.addTab(layout.tabLayout.newTab().setText(getString(R.string.tab_dids)));
        layout.tabLayout.addTab(layout.tabLayout.newTab().setText(getString(R.string.tab_scan)));
        layout.tabLayout.addTab(layout.tabLayout.newTab().setText(getString(R.string.tab_setting)));
        layout.tabLayout.getTabAt(0).setCustomView(setupTabIcons(R.drawable.selector_tab_credential, R.string.tab_credential));
        layout.tabLayout.getTabAt(1).setCustomView(setupTabIcons(R.drawable.selector_tab_dids, R.string.tab_dids));
        layout.tabLayout.getTabAt(2).setCustomView(setupTabIcons(R.drawable.selector_tab_scan, R.string.tab_scan));
        layout.tabLayout.getTabAt(3).setCustomView(setupTabIcons(R.drawable.selector_tab_setting, R.string.tab_setting));
        TabPagerAdatper fragmentPagerAdatper = new TabPagerAdatper(getSupportFragmentManager(), listFragment);
        layout.viewpager.setAdapter(fragmentPagerAdatper);
        layout.viewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(layout.tabLayout));
        layout.viewpager.setCurrentItem(0);
        layout.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab){
                PrintLog.e("tab indenx" + tab.getPosition());
                layout.viewpager.setCurrentItem(tab.getPosition());
                switch(tab.getPosition()){
                    case 0:
                        titleText.setText(R.string.tab_credential);
                        QRCodeScanFragment.newInstance().onPause();
                        break;
                    case 1:
                        titleText.setText(R.string.tab_dids);
                        QRCodeScanFragment.newInstance().onPause();
                        break;
                    case 2:
                        titleText.setText(R.string.tab_scan);
                        QRCodeScanFragment.newInstance().onResume();
                        break;
                    case 3:
                        titleText.setText(R.string.tab_setting);
                        QRCodeScanFragment.newInstance().onPause();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab){
                PrintLog.e("tab indenx" + tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab){
                PrintLog.e("tab indenx" + tab.getPosition());
            }
        });


    }

    private View setupTabIcons(int img, int text){
        View viewFirst = getLayoutInflater().inflate(R.layout.custom_tab, null);
        ImageView imgFirst = viewFirst.findViewById(R.id.image);
        TextView txtFirst = viewFirst.findViewById(R.id.title);
        imgFirst.setImageResource(img);
        txtFirst.setText(text);
        return viewFirst;
    }


    @Override
    public void onBackPressed(){
//        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }

    static public ArrayList<DidDataVo> serchDidList(Context context, String serchNickName){
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(context).getSecureSharedPreferences().getString(DID_LIST, null);
        if(json != null){
            didList = IITPApplication.gson.fromJson(json, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
        }else{
            didList = new ArrayList<>();
        }

        ArrayList<DidDataVo> serchDitList = new ArrayList<>();
        for( DidDataVo vo : didList) {
            if (vo.getNickName().contains(serchNickName)) {
                serchDitList.add(vo);
            }
        }
        return serchDitList;
    }

    static public ArrayList<DidDataVo> getDidList(Context context){
        ArrayList<DidDataVo> didList = new ArrayList<>();
        String json = CommonPreference.getInstance(context).getSecureSharedPreferences().getString(DID_LIST, null);
        if(json != null){
            didList = IITPApplication.gson.fromJson(json, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
        }else{
            didList = new ArrayList<>();
        }
        PrintLog.e("didlist size =" + didList.size());
        return didList;
    }

    static public void setDidList(ArrayList<DidDataVo> didList, Context context){
        String json = IITPApplication.gson.toJson(didList);
        PrintLog.e("save key = " + json);
        CommonPreference.getInstance(context).getSecureSharedPreferences().edit().putString(DID_LIST, json).apply();
    }

    /**
     * DIDList 닉네임 변경
     * @param context context
     * @param nickName Nickname to be changed
     * @param index Did_List Index
     */
    static public void changeNickname(Context context, String nickName, int index) {
        ArrayList<DidDataVo> didList;
        String didListJson = CommonPreference.getInstance(context).getSecureSharedPreferences().getString(DID_LIST, null);
        if(didListJson != null){
            didList = IITPApplication.gson.fromJson(didListJson, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
        }else{
            didList = new ArrayList<>();
        }
        DidDataVo originalDidList = didList.get(index);
        originalDidList.setNickName(nickName);
        didList.set(index, originalDidList);
        setDidList(didList, context);
    }





    /**
     * 생성 위임 VC 저장
     *
     * @param key key
     * @param jwt vc
     */
    private void savePreVCList(String key, String jwt){
        PrintLog.e("VC = " + jwt);
        PrintLog.e("save Key = " + key);
        CommonPreference.getInstance(MainActivity.this).getSecureSharedPreferences().edit().putString(key, jwt).apply();
    }

    private void sendStockApp(String message){
        MessageVo messageVo = IITPApplication.gson.fromJson(message, MessageVo.class);
        String sign = messageVo.getSign();
        String url = messageVo.getUrl();
        String issuer = messageVo.getIssuer();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        String scheme ="zento://stock?sign="+sign+"&url="+url+"&issuer= "+issuer;
        PrintLog.e("scheme= "+scheme);
        intent.setData(Uri.parse(scheme));
        startActivity(intent);
    }


}
