package com.example.cv0318.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toolbar;

import com.example.cv0318.instagramclone.Home.HomeActivity;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.cv0318.instagramclone.Utils.GridImageAdapter;
import com.example.cv0318.instagramclone.Utils.UniversalImageLoader;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity
{
    private static final String TAG = String.format("%s_TAG", ProfileActivity.class.getSimpleName());
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLS = 3;
    private Context m_context;
    private ImageView m_profilePhoto;
    private ProgressBar m_progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        m_context = ProfileActivity.this;
        Log.d(TAG, "onCreate: started.");

        init();

//        setupBottomNavigationView();
//        setupToolbar();
//        setupActivityWidgets();
//        setProfileImage();
//        tempGridSetup();
    }
    
    private void init()
    {
        Log.d(TAG, "init: inflating "+getString(R.string.profile_fragment));

        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.profile_fragment));
        transaction.commit();
    }

//    private void tempGridSetup()
//    {
//        ArrayList<String> imgUrls = new ArrayList<>();
//        imgUrls.add("https://vignette.wikia.nocookie.net/deathbattlefanon/images/2/20/C0A9B238-91F6-46AA-ABDC-3FC720228C25.png/revision/latest?cb=20171104014554");
//        imgUrls.add("https://pm1.narvii.com/6613/d28f17111a214ac8097da7123c8054031d594834_hq.jpg");
//        imgUrls.add("https://vignette.wikia.nocookie.net/deathbattlefanon/images/e/ef/Render_de_Piccolo.png/revision/latest?cb=20160309134559");
//        imgUrls.add("https://vignette.wikia.nocookie.net/deathbattlefanon/images/a/a6/Golden_Frieza.png/revision/latest?cb=20150810045513");
//        imgUrls.add("https://vignette.wikia.nocookie.net/villains/images/4/40/Imperfect_Cell.png/revision/latest?cb=20180204211537");
//        imgUrls.add("https://pre00.deviantart.net/5665/th/pre/f/2017/221/9/5/android_17_of_universe_7_by_elrincondeurko-dbjhunx.png");
//        imgUrls.add("http://www.dustloop.com/wiki/images/d/da/DBFZ_Android18_Portrait.png");
//        imgUrls.add("https://vignette.wikia.nocookie.net/vsbattles/images/d/d8/Majin_buu_2.png/revision/latest?cb=20151031012027");
//
//        setupImageGrid(imgUrls);
//    }
//
//    private void setupImageGrid(ArrayList<String> imgUrls)
//    {
//        GridView gridView = findViewById(R.id.gridView);
//
//        int gridWidth = getResources().getDisplayMetrics().widthPixels;
//        int imageWidth = gridWidth / NUM_GRID_COLS;
//        gridView.setColumnWidth(imageWidth);
//
//        GridImageAdapter adapter = new GridImageAdapter(m_context, R.layout.layout_grid_imageview, "", imgUrls);
//        gridView.setAdapter(adapter);
//    }
//
//    private void setProfileImage()
//    {
//        Log.d(TAG, "setProfileImage: setting profile photo");
//        String url = "tr2.cbsistatic.com/hub/i/r/2017/01/31/7e355c52-c68f-4389-825f-392f2dd2ac19/resize/770x/d19d6c021f770122da649e2a77bd1404/androiddatahero.jpg";
//        UniversalImageLoader.setImage(url, m_profilePhoto, m_progressBar, "https://");
//    }
//
//    private void setupActivityWidgets()
//    {
//        m_progressBar = findViewById(R.id.profileProgressBar);
//        m_progressBar.setVisibility(View.GONE);
//        m_profilePhoto = findViewById(R.id.profile_photo);
//    }
//
//    private void setupToolbar()
//    {
//        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.profileToolbar);
//        setSupportActionBar(toolbar);
//
//        ImageView profileMenu = findViewById(R.id.profileMenu);
//        profileMenu.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                Log.d(TAG, "onClick: navigating to account settings");
//                Intent intent = new Intent(m_context, AccountSettingsActivity.class);
//                startActivity(intent);
//            }
//        });
//    }
//
//    /**
//     * BottomNavigationView setup
//     */
//    private void setupBottomNavigationView()
//    {
//        Log.d(TAG, "setupBottomNavigationView: Setting up nav.");
//        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
//        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
//        BottomNavigationViewHelper.enableNavigation(this, bottomNavigationViewEx);
//
//        Menu menu = bottomNavigationViewEx.getMenu();
//        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
//        menuItem.setChecked(true);
//    }
}
