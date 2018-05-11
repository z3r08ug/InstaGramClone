package com.example.cv0318.instagramclone.Home;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.cv0318.instagramclone.Login.LoginActivity;
import com.example.cv0318.instagramclone.Models.Photo;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.cv0318.instagramclone.Utils.MainfeedListAdapter;
import com.example.cv0318.instagramclone.Utils.SectionsPagerAdapter;
import com.example.cv0318.instagramclone.Utils.UniversalImageLoader;
import com.example.cv0318.instagramclone.Utils.ViewCommentsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

public class HomeActivity extends AppCompatActivity implements MainfeedListAdapter.OnLoadMoreItemsListener
{
    private static final String TAG = String.format("%s_TAG", HomeActivity.class.getSimpleName());
    private static final int ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;

    //firebase
    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;

    //widgets
    private ViewPager m_viewPager;
    private FrameLayout m_frameLayout;
    private RelativeLayout m_relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting.");
        m_viewPager = findViewById(R.id.viewpager_container);
        m_frameLayout = findViewById(R.id.container);
        m_relativeLayout = findViewById(R.id.relLayoutParent);

        setupFirebaseAuth();

        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();
    }

    public void onCommentThreadSelected(Photo photo, String callingActivity)
    {
        Log.d(TAG, "onCommentThreadSelected: Selected a comment thread.");

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    public void hideLayout()
    {
        Log.d(TAG, "hideLayout: hiding layout.");

        m_relativeLayout.setVisibility(View.GONE);
        m_frameLayout.setVisibility(View.VISIBLE);
    }

    public void showLayout()
    {
        Log.d(TAG, "hideLayout: showing layout.");

        m_relativeLayout.setVisibility(View.VISIBLE);
        m_frameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        if (m_frameLayout.getVisibility() == View.VISIBLE)
        {
            showLayout();
        }
    }

    private void initImageLoader()
    {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    /**
     * Responsible for adding the 3 tabs: camera, home and messages
     */
    private void setupViewPager()
    {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CameraFragment());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new MessagesFragment());
        m_viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(m_viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_instagram);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_send);
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "setupBottomNavigationView: Setting up nav.");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(this, this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    /**
     * ---------------------------------------Firebase--------------------------------------
     */

    /**
     * Checks to see if the @param 'user' is logged in.
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user)
    {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if (user == null)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
    
    
    
    /**
     * Setup Firebase Auth Object.
     */
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "setupFirebaseAuth: Setting up Firebase Auth.");
        m_auth = FirebaseAuth.getInstance();
        m_authStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check is user is logged in.
                checkCurrentUser(user);

                if (user != null)
                {
                    //user is signed in
                    Log.d(TAG, String.format("onAuthStateChanged: signed in: %s", user.getUid()));
                }
                else
                {
                    //user is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out.");
                }
            }
        };
    }

    @Override
    public void onStart()
    {
        super.onStart();
        m_auth.addAuthStateListener(m_authStateListener);
        m_viewPager.setCurrentItem(HOME_FRAGMENT);
        checkCurrentUser(m_auth.getCurrentUser());
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (m_authStateListener != null)
        { m_auth.removeAuthStateListener(m_authStateListener); }
    }

    @Override
    public void onLoadMoreItems()
    {
        Log.d(TAG, "onLoadMoreItems: displaying more photos.");

        HomeFragment fragment = (HomeFragment) getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + m_viewPager.getCurrentItem());
        if (fragment != null)
        {
            fragment.displayMorePhotos();
        }
    }
}
