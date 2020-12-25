package com.iitp.iitp_demo.util;



import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter of ViewPager
 */
public class BaseFragmentPageAdapter extends FragmentPagerAdapter{
    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> pageTitleList = new ArrayList<>();

    public BaseFragmentPageAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void addFragment(Fragment fragment) {
        addFragment(fragment, "");
    }

    public void addFragment(Fragment fragment, String pageTitle) {
        fragmentList.add(fragment);
        pageTitleList.add(pageTitle);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitleList.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
