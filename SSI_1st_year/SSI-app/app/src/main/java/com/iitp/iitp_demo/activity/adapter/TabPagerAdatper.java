package com.iitp.iitp_demo.activity.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by bslee on 2018-07-03.
 */

public class TabPagerAdatper extends FragmentPagerAdapter{
    List<Fragment> listfragment;

    public TabPagerAdatper(FragmentManager fm, List<Fragment> listfragment) {
        super(fm);
        this.listfragment = listfragment;
    }

    @Override
    public int getCount() {
        return listfragment.size();
    }

    @Override
    public Fragment getItem(int i) {
        return listfragment.get(i);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Tab " + String.valueOf(position);
    }
}
