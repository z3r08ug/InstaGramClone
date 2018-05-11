package com.example.cv0318.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cv0318.instagramclone.Models.Photo;
import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.ViewCommentsFragment;
import com.example.cv0318.instagramclone.Utils.ViewPostFragment;
import com.example.cv0318.instagramclone.Utils.ViewProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity implements ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener,
        ViewProfileFragment.OnGridImageSelectedListener
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

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.calling_activity)))
        {
            Log.d(TAG, "init: searching got user object attached as an intent extra,");
            if (intent.hasExtra(getString(R.string.intent_user)))
            {
                User user = intent.getParcelableExtra(getString(R.string.intent_user));
                if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    Log.d(TAG, "init: Inflating View Profile.");
                    ViewProfileFragment fragment = new ViewProfileFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.intent_user), intent.getParcelableExtra(getString(R.string.intent_user)));
                    fragment.setArguments(args);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.view_profile_fragment));
                    transaction.commit();
                }
                else
                {
                    Log.d(TAG, "init: Inflating profile.");
                    ProfileFragment fragment = new ProfileFragment();
                    FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.profile_fragment));
                    transaction.commit();
                }

            }
            else
            {
                Toast.makeText(m_context, "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Log.d(TAG, "init: Inflating profile.");
            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }
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

    @Override
    public void onCommentThreadSelectedListener(Photo photo)
    {
        Log.d(TAG, "onCommentThreadSelectedListener: Selected a comments thread.");

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }
}
