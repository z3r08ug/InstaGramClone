package com.example.cv0318.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cv0318.instagramclone.Models.Comment;
import com.example.cv0318.instagramclone.Models.Like;
import com.example.cv0318.instagramclone.Models.Photo;
import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.Models.UserSettings;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.cv0318.instagramclone.Utils.FirebaseMethods;
import com.example.cv0318.instagramclone.Utils.GridImageAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment
{
    private static final String TAG = String.format("%s_TAG",
            ProfileFragment.class.getSimpleName());

    public interface OnGridImageSelectedListener
    {
        void onGridImageSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener m_onGridImageSelectedListener;

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLS = 3;

    //widgets
    private TextView mPosts, mFollowing, mFollowers, mDisplayName, mUsername, mWebsite,
            mDescription;
    private ProgressBar m_progressBar;
    private CircleImageView mProfilePhoto;
    private GridView m_gridView;
    private Toolbar m_toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx m_bottomNavigationView;

    //firebase
    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;
    private FirebaseDatabase m_firebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods m_firebaseMethods;

    //vars
    private Context m_context;
    private int followers, posts, following;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mDisplayName = view.findViewById(R.id.tvDisplayName);
        mUsername = view.findViewById(R.id.profileName);
        mWebsite = view.findViewById(R.id.tvWebsite);
        mDescription = view.findViewById(R.id.tvDescription);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowers = view.findViewById(R.id.tvFollowers);
        mFollowing = view.findViewById(R.id.tvFollowing);
        m_progressBar = view.findViewById(R.id.profileProgressBar);
        m_gridView = view.findViewById(R.id.gridView);
        m_toolbar = view.findViewById(R.id.profileToolbar);
        profileMenu = view.findViewById(R.id.profileMenu);
        m_bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        m_context = getActivity();
        m_firebaseMethods = new FirebaseMethods(getActivity());

        Log.d(TAG, "onCreateView: started");

        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();
        setupGridView();

        getFollowersCount();
        getFollowingCount();
        getPostCount();

        TextView editProfile = view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: navigating to " + m_context.getString(R.string.edit_profile_fragment));
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context)
    {
        try
        {
            m_onGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        }
        catch (ClassCastException e)
        {
            Log.e(TAG, String.format("onAttach: ClassCastException: %s", e.getMessage()));
        }
        super.onAttach(context);
    }

    private void setupGridView()
    {
        Log.d(TAG, "setupGridView: setting up image grid");

        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onDataChange: Profile Fragment Datasnapshot: " + dataSnapshot);
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    try
                    {
                        String caption = objectMap.get(getString(R.string.field_caption)).toString();
                        photo.setCaption(caption);
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                    }
                    catch (NullPointerException e)
                    {
                        Log.e(TAG, "onDataChange: "+e.getMessage());
                    }

                    List<Comment> comments = new ArrayList<>();
                    for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_comments)).getChildren())
                    {
                        Comment comment = new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                        comments.add(comment);
                    }

                    photo.setComments(comments);

                    List<Like> likeList = new ArrayList<>();
                    for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_likes)).getChildren())
                    {
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likeList.add(like);
                    }
                    photo.setLikes(likeList);
                    photos.add(photo);
                }
                //setup our image grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / NUM_GRID_COLS;
                m_gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<>();
                for (int i = 0; i < photos.size(); i++)
                {
                    imgUrls.add(photos.get(i).getImage_path());
                }
                GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                        "", imgUrls);
                m_gridView.setAdapter(adapter);

                m_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        m_onGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings)
    {
//        Log.d(TAG, "setProfileWidgets: Setting widgets with data retrieved from firebase" );

        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getUserAccountSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());

        m_progressBar.setVisibility(View.GONE);
    }

    private void setupToolbar()
    {
        ((ProfileActivity) getActivity()).setSupportActionBar(m_toolbar);
        profileMenu.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: navigating to account settings");
                Intent intent = new Intent(m_context, AccountSettingsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     * BottomNavigationView setup
     **/
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "setupBottomNavigationView: Setting up nav.");
        BottomNavigationViewHelper.setupBottomNavigationView(m_bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(m_context, getActivity(), m_bottomNavigationView);

        Menu menu = m_bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private void getFollowersCount()
    {
        followers = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onDataChange: Followers Datasnapshot: " + dataSnapshot);
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found follower: " + singleSnapshot.getValue());
                    followers++;
                }
                String followersText = String.valueOf(followers);
                Log.d(TAG, "onDataChange: Follower data collected: " + followersText);
                mFollowers.setText(followersText);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void getFollowingCount()
    {
        following = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                Log.d(TAG, "onDataChange: Following Datasnapshot: " + dataSnapshot);
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found following user: " + singleSnapshot.getValue());
                    following++;
                }
                String followingText = String.valueOf(following);
                Log.d(TAG, "onDataChange: Collecting following data: " + followingText);
                mFollowing.setText(followingText);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void getPostCount()
    {
        posts = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                Log.d(TAG, "onDataChange: Posts Datasnapshot: " + dataSnapshot);
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found post: " + singleSnapshot.getValue());
                    posts++;
                }
                String postText = String.valueOf(posts);
                Log.d(TAG, "onDataChange: Collecting post data: " + postText);
                mPosts.setText(postText);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
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

        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //retrieve user information from database
                setProfileWidgets(m_firebaseMethods.getUserSettings(dataSnapshot));
                //retrieve images for the user in question
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
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
