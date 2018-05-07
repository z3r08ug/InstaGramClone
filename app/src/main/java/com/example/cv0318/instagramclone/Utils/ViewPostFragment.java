package com.example.cv0318.instagramclone.Utils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cv0318.instagramclone.Models.Like;
import com.example.cv0318.instagramclone.Models.Photo;
import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.R;
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
    private TextView m_tvBackLabel, m_tvCaption, m_tvUserName, m_tvTimeStamp, m_likes;
    private ImageView m_backArrow, m_ellipses, m_heartRed, m_heartWhite, m_profileImage;

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
    private GestureDetector m_gestureDetector;
    private Heart m_heart;
    private boolean m_likedByCurrentUser;
    private StringBuilder m_users;
    private String m_likesString = "";

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
        m_heartWhite = view.findViewById(R.id.ivHeartOutline);
        m_heartRed = view.findViewById(R.id.ivRedHeart);
        m_profileImage = view.findViewById(R.id.profile_photo);
        m_likes = view.findViewById(R.id.tvImageLikes);

        m_heart = new Heart(m_heartWhite, m_heartRed);
        m_gestureDetector = new GestureDetector(getActivity(), new GestureListener());
        try
        {
            m_photo = getPhotoFromBundle();
            UniversalImageLoader.setImage(m_photo.getImage_path(), m_postImage, null, "");
            m_activityNum = getActivityNumFromBundle();
            getPhotoDetails();
            getLikesString();
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, String.format("onCreateView: NullPointerException: %s", e.getMessage()));
        }
        setupFirebaseAuth();
        setupBottomNavigationView();

        return view;
    }

    private void getLikesString()
    {
        Log.d(TAG, "getLikesString: getting likes string. photoid: " + m_photo.getPhoto_id());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_photos))
                .orderByChild(getString(R.string.field_photo_id))
                .equalTo(m_photo.getPhoto_id());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "GetLikesString onDataChange: firstSnapshot: " + dataSnapshot);
                m_users = new StringBuilder();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "GetLikesString onDataChange: secondSnapshot: "+singleSnapshot);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Photo.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                            {
                                m_users.append(singleSnapshot.getValue(User.class).getUsername());
                                m_users.append(",");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });

                }



                String[] splitUsers = m_users.toString().split(",");

                if (m_users.toString().contains(m_userAccountSettings.getUsername()))
                {
                    m_likedByCurrentUser = true;
                }
                else
                {
                    m_likedByCurrentUser = false;
                }

                int length = splitUsers.length;
                if (length == 1)
                {
                    m_likesString = String.format("Liked by %s", splitUsers[0]);
                }
                else if (length == 2)
                {
                    m_likesString = String.format("Liked by %s and %s", splitUsers[0], splitUsers[1]);
                }
                else if (length == 3)
                {
                    m_likesString = String.format("Liked by %s, %s, and %s", splitUsers[0], splitUsers[1], splitUsers[2]);
                }
                else if (length == 4)
                {
                    m_likesString = String.format("Liked by %s, %s, %s, and %s", splitUsers[0], splitUsers[1], splitUsers[2], splitUsers[3]);
                }
                else if (length > 4)
                {
                    m_likesString = String.format("Liked by %s, %s, %s, and %s others", splitUsers[0], splitUsers[1], splitUsers[2], (splitUsers.length - 3));
                }
                Log.d(TAG, String.format("onDataChange: likes string:%s", m_likesString));
                setupWidgets();
                if (!dataSnapshot.exists())
                {
                    Log.d(TAG, "onDataChange: snapshot did not exist");
                    m_likesString = "";
                    m_likedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_photos))
                    .child(m_photo.getPhoto_id())
                    .child(getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    Log.d(TAG, "onDataChange: Adding Like snapshot: " + dataSnapshot);
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                    {
                        String keyId = singleSnapshot.getKey();
                        //case 1: the user already liked the photo
                        if (m_likedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        {
                            myRef.child(getString(R.string.dbname_photos))
                                    .child(m_photo.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(m_photo.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            m_heart.toggleLike();
                            getLikesString();
                        }

                        //case 2: the user has not liked the photo
                        else if (!m_likedByCurrentUser)
                        {
                            //add new like
                            addNewLike();
                            break;
                        }
                    }
                    if (!dataSnapshot.exists())
                    {
                        Log.d(TAG, "onDataChange: Snapshot doesn't exist so add the like");
                        //add new like
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

            return true;
        }
    }

    private void addNewLike()
    {
        String newLikeId = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_photos))
                .child(m_photo.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        myRef.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(m_photo.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        m_heart.toggleLike();
        getLikesString();
    }

    private void getPhotoDetails()
    {
        Log.d(TAG, "getPhotoDetails: user id: " + m_photo.getUser_id());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id));
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    if (singleSnapshot.getKey().equals(m_photo.getUser_id()))
                    {
                        m_userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    }

                }
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
        try
        {
            UniversalImageLoader.setImage(m_userAccountSettings.getProfile_photo(), m_profileImage, null, "");
            m_tvUserName.setText(m_userAccountSettings.getUsername());
            m_likes.setText(m_likesString);
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, "setupWidgets: " + e.getMessage());
            Log.e(TAG, "setupWidgets: " + m_userAccountSettings);
        }


        if (m_likedByCurrentUser)
        {
            m_heartWhite.setVisibility(View.GONE);
            m_heartRed.setVisibility(View.VISIBLE);
            m_heartRed.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    Log.d(TAG, "onTouch: red heart touch detected");
                    return m_gestureDetector.onTouchEvent(event);
                }
            });
        }
        else
        {
            m_heartWhite.setVisibility(View.VISIBLE);
            m_heartRed.setVisibility(View.GONE);
            m_heartWhite.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    Log.d(TAG, "onTouch: white heart touch detected");
                    return m_gestureDetector.onTouchEvent(event);
                }
            });
        }


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
        final String photoTimeStamp = m_photo.getDate_created();
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
