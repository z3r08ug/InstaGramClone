package com.example.cv0318.instagramclone.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.cv0318.instagramclone.Home.HomeActivity;
import com.example.cv0318.instagramclone.Likes.LikesActivity;
import com.example.cv0318.instagramclone.Profile.ProfileActivity;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Search.SearchActivity;
import com.example.cv0318.instagramclone.Share.ShareActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNavigationViewHelper
{
    private static final String TAG = String.format("%s_TAG",
        BottomNavigationViewHelper.class.getSimpleName());

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx)
    {
        Log.d(TAG, "setupBottomNavigationView: setting up bottomnavigationview");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx view)
    {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView
            .OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                Intent intent = new Intent();
                switch (item.getItemId())
                {
                    case R.id.ic_house:
                        intent = new Intent(context, HomeActivity.class);
                        break;
                    case R.id.ic_search:
                        intent = new Intent(context, SearchActivity.class);
                        break;
                    case R.id.ic_circle:
                        intent = new Intent(context, ShareActivity.class);
                        break;
                    case R.id.ic_alert:
                        intent = new Intent(context, LikesActivity.class);
                        break;
                    case R.id.ic_android:
                        intent = new Intent(context, ProfileActivity.class);
                        break;
                }
                context.startActivity(intent);
                callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return false;
            }
        });
    }
}
