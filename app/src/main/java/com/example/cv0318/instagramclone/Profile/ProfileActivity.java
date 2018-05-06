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
import com.example.cv0318.instagramclone.Models.Photo;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.cv0318.instagramclone.Utils.GridImageAdapter;
import com.example.cv0318.instagramclone.Utils.UniversalImageLoader;
import com.example.cv0318.instagramclone.ViewPostFragment;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements ProfileFragment.OnGridImageSelectedListener
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
    
    @Override
    public void onGridImageSelected(Photo photo, int activityNumber)
    {
        Log.d(TAG, String.format("onGridImageSelected: selected an image from gridview: %s", photo.toString()));
    
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);
        fragment.setArguments(args);
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }
}
