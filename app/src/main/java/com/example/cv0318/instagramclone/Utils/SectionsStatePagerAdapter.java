package com.example.cv0318.instagramclone.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter
{
    private final List<Fragment> m_fragmentList = new ArrayList<>();
    private final HashMap<Fragment, Integer> m_fragments = new HashMap<>();
    private final HashMap<String, Integer> m_fragmentNums = new HashMap<>();
    private final HashMap<Integer, String> m_fragmentNames = new HashMap<>();

    public SectionsStatePagerAdapter(FragmentManager fm)
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

    public void addFragment(Fragment fragment, String fragmentName)
    {
        m_fragmentList.add(fragment);
        m_fragments.put(fragment, m_fragmentList.size()-1);
        m_fragmentNums.put(fragmentName, m_fragmentList.size()-1);
        m_fragmentNames.put(m_fragmentList.size()-1, fragmentName);
    }

    /**
     * Returns the fragment with the name @param
     * @param fragmentName
     * @return
     */
    public Integer getFragmentNumber(String fragmentName)
    {
        if (m_fragmentNums.containsKey(fragmentName))
        {
            return m_fragmentNums.get(fragmentName);
        }
        return null;
    }

    /**
     * Returns the fragment with the name @param
     * @param fragment
     * @return
     */
    public Integer getFragmentNumber(Fragment fragment)
    {
        if (m_fragmentNums.containsKey(fragment))
        {
            return m_fragmentNums.get(fragment);
        }
        return null;
    }

    /**
     * Returns the fragment with the number @param
     * @param fragment
     * @return
     */
    public String getFragmentName(Integer fragment)
    {
        if (m_fragmentNames.containsKey(fragment))
        {
            return m_fragmentNames.get(fragment);
        }
        return null;
    }
}
