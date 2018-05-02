package com.example.cv0318.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.cv0318.instagramclone.Utils.FirebaseMethods;
import com.example.cv0318.instagramclone.Utils.SectionsStatePagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class AccountSettingsActivity extends AppCompatActivity
{
    private static final String TAG = String.format("%s_TAG",
        AccountSettingsActivity.class.getSimpleName());
    private static final int ACTIVITY_NUM = 4;

    private Context m_context;
    private SectionsStatePagerAdapter m_pagerAdapter;
    private ViewPager m_viewPager;
    private RelativeLayout m_relativeLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        m_context = AccountSettingsActivity.this;
        Log.d(TAG, "onCreate: started");
        m_viewPager = findViewById(R.id.container);
        m_relativeLayout = findViewById(R.id.relLayout1);

        setupSettingsList();
        setupBottomNavigationView();
        setupFragments();
        getIncomingIntent();

        //setup backarrow
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Navigating back to profile activity");
                finish();
            }
        });
    }

    private void getIncomingIntent()
    {
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.calling_activity)))
        {
            Log.d(TAG, "getIncomingIntent: Received incoming intent from "+getString(R.string.profile_activity));
            setViewPager(m_pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }
    }

    private void setupFragments()
    {
        m_pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        m_pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile_fragment));
        m_pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out_fragment));
    }
    
    private void setViewPager(int fragmentNumber)
    {
        m_relativeLayout.setVisibility(View.GONE);
        Log.d(TAG,
            String.format("setViewPager: navigating to fragment number: %d", fragmentNumber));
        m_viewPager.setAdapter(m_pagerAdapter);
        m_viewPager.setCurrentItem(fragmentNumber);
    }

    private void setupSettingsList()
    {
        Log.d(TAG, "setupSettingsList: Initializing account settings list");
        ListView listView = findViewById(R.id.lvAccountSettings);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.sign_out_fragment));

        ArrayAdapter arrayAdapter = new ArrayAdapter(m_context, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(arrayAdapter);
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, String.format("onItemClick: navigating to fragment #: %d", position));
                setViewPager(position);
            }
        });
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "setupBottomNavigationView: Setting up nav.");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
