package com.example.cv0318.instagramclone.Profile;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.Models.UserSettings;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.FirebaseMethods;
import com.example.cv0318.instagramclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment
{
    private static final String TAG = String.format("%s_TAG",
        EditProfileFragment.class.getSimpleName());

    //EditProfileFragment Widgets
    private CircleImageView mProfilePhoto;
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private String userId;

    //firebase
    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;
    private FirebaseDatabase m_firebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods m_firebaseMethods;


    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mProfilePhoto = view.findViewById(R.id.cvProfilePhoto);
        mDisplayName = view.findViewById(R.id.etDisplayName);
        mUsername = view.findViewById(R.id.etUserName);
        mWebsite = view.findViewById(R.id.etWebsite);
        mDescription = view.findViewById(R.id.etDescription);
        mEmail = view.findViewById(R.id.etEmail);
        mPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        mChangeProfilePhoto = view.findViewById(R.id.tvChangePhoto);

        m_firebaseMethods = new FirebaseMethods(getActivity());

        //setProfileImage();
        setupFirebaseAuth();

        //back arrow for navigating back to "ProfileActivity"
        ImageView backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                getActivity().finish();
            }
        });

        return view;
    }

    /**
     * Retrieves the data contained in the widgets and submits it to the database.
     * Before doing so, it checks to make sure the username is unique.
     */
    private void saveProfileSettings()
    {
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());

        myRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                User user = new User();
                for (DataSnapshot ds: dataSnapshot.child(getString(R.string.dbname_users)).getChildren())
                {
                    if (ds.getKey().equals(userId))
                    {
                        user.setUsername(ds.getValue(User.class).getUsername());
                    }
                }
                Log.d(TAG, "onDataChange: CURRENT USERNAME: "+user.getUsername());

                //case 1 the user did not change their username
                if (user.getUsername().equals(username))
                {

                }
                //case 2 check username for uniqueness
                else
                {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings)
    {
//        Log.d(TAG, "setProfileWidgets: Setting widgets with data retrieved from firebase" );

        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getUserAccountSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(user.getEmail());
        mPhoneNumber.setText(String.valueOf(user.getPhone_number()));
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
        userId = m_auth.getCurrentUser().getUid();

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
        { m_auth.removeAuthStateListener(m_authStateListener); }
    }
}
