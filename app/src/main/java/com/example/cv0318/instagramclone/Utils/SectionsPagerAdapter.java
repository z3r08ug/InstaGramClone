package com.example.cv0318.instagramclone.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that stores fragments
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter
{
    private static final String TAG = String.format("%s_TAG",
        SectionsPagerAdapter.class.getSimpleName());

    private final List<Fragment> m_fragmentList = new ArrayList<>();

    public SectionsPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        return m_fragmentList.get(position);
    }

    @Override
    public int getCount()
    {
        return m_fragmentList.size();
    }

    public void addFragment(Fragment fragment)
    {
        m_fragmentList.add(fragment);
    }
}
