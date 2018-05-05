package com.example.cv0318.instagramclone.Profile;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cv0318.instagramclone.Dialogs.ConfirmPasswordDialog;
import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.Models.UserSettings;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.FirebaseMethods;
import com.example.cv0318.instagramclone.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog
    .OnConfirmPasswordListener
{
    private static final String TAG = String.format("%s_TAG",
        EditProfileFragment.class.getSimpleName());

    //EditProfileFragment Widgets
    private CircleImageView mProfilePhoto;
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;

    //firebase
    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;
    private FirebaseDatabase m_firebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods m_firebaseMethods;

    //variables
    private String userId;
    private UserSettings mUserSettings;

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

        ImageView checkmark = view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: attempting to save changes.");
                saveProfileSettings();
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

        //case 1 if the user made a change to username
        if (!mUserSettings.getUser().getUsername().equals(username))
        {
            checkIfUsernameExists(username);
        }
        //case 2 if the user made a change to email
        if (!mUserSettings.getUser().getEmail().equals(email))
        {
            //step1 Re-authenticate
            //          -Confirm the password and email
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this, 1);

            //step2 check is the email is already registered
            //          -'fetchProvidersForEmail(String email)'
            //step3 change the email
            //          -submit the new email to the database and authentication
        }

        //change settings that don't require uniqueness
        if (!mUserSettings.getUserAccountSettings().getDisplay_name().equals(displayName))
        {
            //update display name
            m_firebaseMethods.updateUserAccountSettings(displayName, null, null, 0);
        }
        if (!mUserSettings.getUserAccountSettings().getWebsite().equals(website))
        {
            //update website
            m_firebaseMethods.updateUserAccountSettings(null, website, null, 0);
        }
        if (!mUserSettings.getUserAccountSettings().getDescription().equals(description))
        {
            //update description
            m_firebaseMethods.updateUserAccountSettings(null, null, description, 0);
        }
        if (!(mUserSettings.getUser().getPhone_number() == phoneNumber))
        {
            //phone number
            m_firebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber);
        }
    }

    /**
     * Check if @param username already exists in the database
     *
     * @param username
     */
    private void checkIfUsernameExists(final String username)
    {
        Log.d(TAG, String.format("checkIfUsernameExists: Checking if %s already exists", username));

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
            .child(getString(R.string.dbname_users))
            .orderByChild(getString(R.string.field_username))
            .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.exists())
                {
                    //add the username
                    Toast.makeText(getActivity(), "Saved username.", Toast.LENGTH_SHORT).show();
                    m_firebaseMethods.updateUsername(username);
                }
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    if (singleSnapshot.exists())
                    {
                        Log.d(TAG,
                            "onDataChange: FOUND A MATCH: " + singleSnapshot.getValue(User.class)
                                .getUsername());
                        Toast.makeText(getActivity(),
                            "That username already exists.",
                            Toast.LENGTH_SHORT).show();
                    }
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

        mUserSettings = userSettings;

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

    @Override
    public void onConfirmPassword(String password)
    {
        Log.d(TAG, String.format("onConfirmPassword: got the password: %s", password));

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        /* Get auth credentials from the user for re-authentication.  The example
            below shows email and password credentials but there are multiple possible providers,
            such as GoogleAuthProvider or FBAuthProvider.
         */
        AuthCredential credential = EmailAuthProvider
            .getCredential(m_auth.getCurrentUser().getEmail(), password);

        //prompt the user to reprovide their sign in credentials
        user.reauthenticate(credential)
            .addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        Log.d(TAG, "onComplete: User Re-authenticated.");

                        //Check to see if the email is not already present in the database
                        m_auth.fetchProvidersForEmail(mEmail.getText().toString())
                            .addOnCompleteListener(
                                new OnCompleteListener<ProviderQueryResult>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<ProviderQueryResult> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            try
                                            {
                                                if (task.getResult().getProviders().size() == 1)
                                                {
                                                    Log.d(TAG,
                                                        "onComplete: that email is already in use" +
                                                            ".");
                                                    Toast.makeText(getActivity(),
                                                        "That email is already in use.",
                                                        Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    Log.d(TAG,
                                                        "onComplete: That email is available.");

                                                    m_auth.getCurrentUser()
                                                        .updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(
                                                                @NonNull
                                                                    Task<Void>
                                                                    task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Log.d(TAG,
                                                                        "onComplete: User email " +
                                                                            "address has been " +
                                                                            "updated");
                                                                    Toast.makeText(getActivity(),
                                                                        "Email updated.",
                                                                        Toast.LENGTH_SHORT).show();
                                                                    m_firebaseMethods.updateEmail(
                                                                        mEmail.getText()
                                                                            .toString());
                                                                }
                                                            }
                                                        });
                                                }
                                            }
                                            catch (NullPointerException e)
                                            {
                                                Log.e(TAG,
                                                    "onComplete: NullPointterException: ",
                                                    e);
                                            }
                                        }
                                    }
                                });
                    }
                    else
                    {
                        Log.d(TAG, "onComplete: Re-authentication failed.");
                    }
                }
            });

    }
}
