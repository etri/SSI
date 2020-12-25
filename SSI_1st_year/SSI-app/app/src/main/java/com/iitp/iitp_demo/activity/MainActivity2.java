package com.iitp.iitp_demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.iitp.iitp_demo.Constants;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.adapter.TabPagerAdatper;
import com.iitp.iitp_demo.activity.fragment.CredentailaListFragment;
import com.iitp.iitp_demo.activity.fragment.RequestCredentailFragment;
import com.iitp.iitp_demo.activity.fragment.RequestPageFragment;
import com.iitp.iitp_demo.activity.fragment.SettingFragment;
import com.iitp.iitp_demo.databinding.ActivityMain2Binding;
import com.iitp.iitp_demo.util.BackPressCloseHandler;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity{

    private static Context ctx;
    private static Activity activity;

    private static ActivityMain2Binding layout;

    private TextView titleText;
    private int tabIndex = 0;
    private Intent intent = null;

    private BackPressCloseHandler backPressCloseHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        layout = DataBindingUtil.setContentView(this, R.layout.activity_main2);
        layout.setActivity(this);
        ctx = this;
        activity = this;
        backPressCloseHandler = new BackPressCloseHandler(this);
        intent = getIntent();
        tabIndex = intent.getIntExtra(Constants.TAB_INDEX, -1);
        setSupportActionBar(layout.toolbar.appbar);
        if(getSupportActionBar() != null){
            titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            titleText.setText(R.string.tab_title_2);
        }
        initViewPager();

    }

    @Override
    protected void onResume(){

        PrintLog.e("onResume");
        PrintLog.e("tab index data = " + tabIndex);
        if(tabIndex != -1){
            layout.viewpager.setCurrentItem(0);
        }
//        layout.viewpager.setCurrentItem(1);
        super.onResume();
    }

    private List<Fragment> listFragment = new ArrayList<>();

    private void initViewPager(){
        listFragment.add(new RequestPageFragment());
        listFragment.add(new CredentailaListFragment());
        listFragment.add(new SettingFragment());
        layout.tabLayout.addTab(layout.tabLayout.newTab().setText(getString(R.string.tab_title_0)));
        layout.tabLayout.addTab(layout.tabLayout.newTab().setText(getString(R.string.tab_title_2)));
        layout.tabLayout.addTab(layout.tabLayout.newTab().setText(getString(R.string.tab_title_3)));
        TabPagerAdatper fragmentPagerAdatper = new TabPagerAdatper(getSupportFragmentManager(), listFragment);
        layout.viewpager.setAdapter(fragmentPagerAdatper);
        layout.viewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(layout.tabLayout));
        layout.viewpager.setCurrentItem(1);
        layout.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab){
                PrintLog.e("tab indenx" + tab.getPosition());
                layout.viewpager.setCurrentItem(tab.getPosition());
                switch(tab.getPosition()){
                    case 0:
                        titleText.setText(R.string.tab_title_0);
                        break;
                    case 1:
                        titleText.setText(R.string.tab_title_2);
                        break;
                    case 2:
                        titleText.setText(R.string.tab_title_3);
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

    @Override
    public void onBackPressed(){
//        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
}
