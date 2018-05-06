package com.example.cv0318.instagramclone;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cv0318.instagramclone.Models.Photo;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.cv0318.instagramclone.Utils.FirebaseMethods;
import com.example.cv0318.instagramclone.Utils.GridImageAdapter;
import com.example.cv0318.instagramclone.Utils.SquareImageView;
import com.example.cv0318.instagramclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment
{
    private static final String TAG = String.format("%s_TAG", ViewPostFragment.class.getSimpleName());
    
    //widgets
    private SquareImageView m_postImage;
    private BottomNavigationViewEx m_bottomNavigationView;
    private TextView m_tvBackLabel, m_tvCaption, m_tvUserName, m_tvTimeStamp;
    private ImageView m_backArrow, m_ellipses, m_heartRed, m_heartOutline, m_profileImage;
    
    //firebase
    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;
    private FirebaseDatabase m_firebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods m_firebaseMethods;
    
    //vars
    private Photo m_photo;
    private int m_activityNum = 0;
    private String m_photoUsername = "";
    private String m_photoUrl = "";
    private UserAccountSettings m_userAccountSettings;
    
    
    public ViewPostFragment()
    {
        setArguments(new Bundle());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        m_postImage = view.findViewById(R.id.postImage);
        m_bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        m_backArrow = view.findViewById(R.id.postBackArrow);
        m_tvBackLabel = view.findViewById(R.id.tvPostBackLabel);
        m_tvCaption = view.findViewById(R.id.tvImageCaption);
        m_tvUserName = view.findViewById(R.id.postUserName);
        m_tvTimeStamp = view.findViewById(R.id.tvImageTimePosted);
        m_ellipses = view.findViewById(R.id.ivPostEllipses);
        m_heartOutline = view.findViewById(R.id.ivHeartOutline);
        m_heartRed = view.findViewById(R.id.ivRedHeart);
        m_profileImage = view.findViewById(R.id.profile_photo);
        
        try
        {
            m_photo = getPhotoFromBundle();
            UniversalImageLoader.setImage(m_photo.getImagePath(), m_postImage, null, "");
            m_activityNum = getActivityNumFromBundle();
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, String.format("onCreateView: NullPointerException: %s", e.getMessage()));
        }
        setupFirebaseAuth();
        setupBottomNavigationView();
        getPhotoDetails();
        
        return view;
    }
    
    private void getPhotoDetails()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(m_photo.getUserId());
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    m_userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                }
                setupWidgets();
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }
    
    private void setupWidgets()
    {
        String timestampDifference = getTimestampDifference();
        if (!timestampDifference.equals("0"))
        {
            m_tvTimeStamp.setText(String.format("%sDAYS AGO", timestampDifference));
        }
        else
        {
            m_tvTimeStamp.setText("TODAY");
        }
        UniversalImageLoader.setImage(m_userAccountSettings.getProfile_photo(), m_profileImage, null, "");
        m_tvUserName.setText(m_userAccountSettings.getUsername());
    }
    
    /**
     * Returns a string representing the number of days ago the post was made.
     *
     * @return
     */
    private String getTimestampDifference()
    {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference");
        
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
        Date today = c.getTime();
        sdf.format(today);
        Date timeStamp;
        final String photoTimeStamp = m_photo.getDateCreated();
        try
        {
            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round(((today.getTime() - timeStamp.getTime()) / 1000 / 60 / 60 / 24)));
        }
        catch (ParseException e)
        {
            Log.e(TAG, String.format("getTimestampDifference: %s", e.getMessage()));
            difference = "0";
        }
        return difference;
    }
    
    /**
     * Retrieve the photo from the incoming bundle from Profile Activity interface
     *
     * @return
     */
    private Photo getPhotoFromBundle()
    {
        Log.d(TAG, String.format("getPhotoFromBundle: arguments: %s", getArguments()));
        
        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            return bundle.getParcelable(getString(R.string.photo));
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Retrieve the activity number from the incoming bundle from Profile Activity interface
     *
     * @return
     */
    private int getActivityNumFromBundle()
    {
        Log.d(TAG, String.format("getActivityNumFromBundle: num: %s", getArguments()));
        
        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            return bundle.getInt(getString(R.string.activity_number));
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * BottomNavigationView setup
     **/
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "setupBottomNavigationView: Setting up nav.");
        BottomNavigationViewHelper.setupBottomNavigationView(m_bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(), m_bottomNavigationView);
        
        Menu menu = m_bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(m_activityNum);
        menuItem.setChecked(true);
    }
    
    /**
     * ---------------------------------------Firebase--------------------------------------
     */
    
    /**
     * Setup Firebase Auth Object.
     */
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "setupFirebaseAuth: Setting up Firebase Auth.");
        m_auth = FirebaseAuth.getInstance();
        m_firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = m_firebaseDatabase.getReference();
        
        m_authStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                
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
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
        if (m_authStateListener != null)
        {
            m_auth.removeAuthStateListener(m_authStateListener);
        }
    }
}
