package com.example.cv0318.instagramclone.Share;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.cv0318.instagramclone.Utils.Permissions;
import com.example.cv0318.instagramclone.Utils.SectionsPagerAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity
{
    private static final String TAG = String.format("%s_TAG", ShareActivity.class.getSimpleName());
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager m_viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: started.");

        if (checkPermissionsArray(Permissions.PERMISSIONS))
        {
            setupViewPager();
        }
        else
        {
            verifyPermissions(Permissions.PERMISSIONS);
        }
    }

    /**
     * return the current tab index
     * 0 = Gallery Fragment
     * 1 = Photo Fragment
     * @return
     */
    public int getCurrentTabNumber()
    {
        return m_viewPager.getCurrentItem();
    }

    /**
     * setup viewpager for managing the tabs
     */
    private void setupViewPager()
    {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());
        m_viewPager = findViewById(R.id.container);
        m_viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(m_viewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));
    }

    /**
     * verify all the permissions passed to the array
     * @param permissions
     */
    public void verifyPermissions(String[] permissions)
    {
        Log.d(TAG, "verifyPermissions: verifying permissions");

        ActivityCompat.requestPermissions(
            ShareActivity.this,
            permissions,
            VERIFY_PERMISSIONS_REQUEST
        );
    }

    /**
     * check an array of permissions
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions)
    {
        for (int i = 0; i < permissions.length; i++)
        {
            String check = permissions[i];
            if (!checkPermissions(check))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * check single permission for granted access
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission)
    {
        Log.d(TAG, String.format("checkPermissions: checking permission: %s", permission));

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG,
                String.format("checkPermissions: Permission was not granted for: %s", permission));
            return false;
        }
        else
        {
            Log.d(TAG,
                String.format("checkPermissions: Permission was granted for: %s", permission));
            return true;
        }
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
