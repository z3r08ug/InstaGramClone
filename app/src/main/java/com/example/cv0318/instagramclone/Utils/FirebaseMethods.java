package com.example.cv0318.instagramclone.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.Models.UserSettings;
import com.example.cv0318.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseMethods
{
    private static final String TAG = String.format("%s_TAG",
        FirebaseMethods.class.getSimpleName());

    //firebase
    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;
    private FirebaseDatabase m_firebaseDatabase;
    private DatabaseReference myRef;

    private String userId;

    private Context m_context;

    public FirebaseMethods(Context context)
    {
        m_auth = FirebaseAuth.getInstance();
        m_firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = m_firebaseDatabase.getReference();
        m_context = context;

        if (m_auth.getCurrentUser() != null)
        {
            userId = m_auth.getCurrentUser().getUid();
        }
    }

    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot)
    {
        Log.d(TAG, "checkIfUsernameExists: Checking if username already exists.");

        User user = new User();

        for (DataSnapshot ds : dataSnapshot.child(userId).getChildren())
        {
            Log.d(TAG, String.format("checkIfUsernameExists: datasnapshot: %s", ds));

            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "checkIfUsernameExists: username: "+user.getUsername());

            if (StringManipulation.expandUsername(user.getUsername()).equals(username))
            {
                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: "+user.getUsername());
                return true;
            }
        }
        return false;
    }

    /**
     * Register a new email and password to Firebase Authentication.
     *
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, final String username)
    {
        m_auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        // Sign in success, update UI with the signed-in user's information
                        sendVerificationEmail();

                        FirebaseUser user = m_auth.getCurrentUser();
                        userId = user.getUid();
                        Log.d(TAG, String.format("createUserWithEmail:success : %s", userId));
                    }
                    else
                    {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(m_context, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    public void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (!task.isSuccessful())
                        {
                            Toast.makeText(m_context, "Couldn't send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    /**
     * Add information to the users node
     * Add information to the user_account_settings node
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public void addNewUser(String email, String username, String description, String website, String profile_photo)
    {
        User user = new User(userId, email, 1, StringManipulation.condenseUsername(username));

        myRef.child(m_context.getString(R.string.dbname_users))
            .child(userId)
            .setValue(user);
        UserAccountSettings settings = new UserAccountSettings(
            description,
            username,
            0,
            0,
            0,
            profile_photo,
            StringManipulation.condenseUsername(username),
            website
        );
        myRef.child(m_context.getString(R.string.dbname_user_account_settings))
            .child(userId)
            .setValue(settings);
    }

    /**
     * Retrieves the account settings for the user currently logged in.
     * Databse: user_account_settings node
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot)
    {
        Log.d(TAG, "getUserAccountSettings: Retrieving user account settings from firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            //user_account_settings node
            if (ds.getKey().equals(m_context.getString(R.string.dbname_user_account_settings)))
            {
                Log.d(TAG, String.format("getUserAccountSettings: datasnapshot: %s", ds));

                try
                {
                    settings.setDisplay_name(ds.child(userId)
                        .getValue(UserAccountSettings.class)
                        .getDisplay_name());

                    settings.setUsername(ds.child(userId)
                        .getValue(UserAccountSettings.class)
                        .getUsername());

                    settings.setWebsite(ds.child(userId)
                        .getValue(UserAccountSettings.class)
                        .getWebsite());

                    settings.setDescription(ds.child(userId)
                        .getValue(UserAccountSettings.class)
                        .getDescription());

                    settings.setProfile_photo(ds.child(userId)
                        .getValue(UserAccountSettings.class)
                        .getProfile_photo());

                    settings.setPosts(ds.child(userId)
                        .getValue(UserAccountSettings.class)
                        .getPosts());

                    settings.setFollowers(ds.child(userId)
                        .getValue(UserAccountSettings.class)
                        .getFollowers());

                    settings.setFollowing(ds.child(userId)
                        .getValue(UserAccountSettings.class)
                        .getFollowing());

                    Log.d(TAG,
                        String.format(
                            "getUserAccountSettings: retrieved user_account_settings information:" +
                                " %s",
                            settings.toString()));
                }
                catch (NullPointerException e)
                {
                    Log.e(TAG, "getUserAccountSettings: NullPointer Ecception: ", e);
                }

            }

            //user node
            if (ds.getKey().equals(m_context.getString(R.string.dbname_users)))
            {
                Log.d(TAG, String.format("getUserAccountSettings: datasnapshot: %s", ds));

                try
                {
                    user.setUsername(ds.child(userId)
                        .getValue(User.class)
                        .getUsername());

                    user.setEmail(ds.child(userId)
                        .getValue(User.class)
                        .getEmail());

                    user.setPhone_number(ds.child(userId)
                        .getValue(User.class)
                        .getPhone_number());

                    user.setUser_id(ds.child(userId)
                        .getValue(User.class)
                        .getUser_id());


                    Log.d(TAG,
                        String.format("getUserAccountSettings: retrieved user information: %s",
                            user.toString()));
                }
                catch (NullPointerException e)
                {
                    Log.e(TAG, "getUserAccountSettings: NullPointer Ecception: ", e);
                }
            }
        }
        return new UserSettings(user, settings);
    }
}
