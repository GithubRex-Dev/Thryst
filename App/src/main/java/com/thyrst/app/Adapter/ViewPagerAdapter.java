package com.thyrst.app.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rex on 5/24/2017.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private String [] pageTittle = {};
    private final List<Fragment> mFragmentList = new ArrayList<>();

    public void setPageTittle(String [] pageTittle) {
        this.pageTittle = pageTittle;
    }

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTittle[position];
    }
}