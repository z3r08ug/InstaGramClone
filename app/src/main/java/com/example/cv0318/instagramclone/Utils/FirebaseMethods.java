package com.example.cv0318.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.cv0318.instagramclone.Home.HomeActivity;
import com.example.cv0318.instagramclone.Models.Photo;
import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.Models.UserSettings;
import com.example.cv0318.instagramclone.Profile.AccountSettingsActivity;
import com.example.cv0318.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods
{
    private static final String TAG = String.format("%s_TAG",
            FirebaseMethods.class.getSimpleName());
    
    //firebase
    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;
    private FirebaseDatabase m_firebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference m_storageReference;
    
    //vars
    private String userId;
    private double m_photoUploadProgress = 0;
    private Context m_context;
    
    public FirebaseMethods(Context context)
    {
        m_auth = FirebaseAuth.getInstance();
        m_firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = m_firebaseDatabase.getReference();
        m_storageReference = FirebaseStorage.getInstance().getReference();
        m_context = context;
        
        if (m_auth.getCurrentUser() != null)
        {
            userId = m_auth.getCurrentUser().getUid();
        }
    }
    
    public void uploadNewPhoto(String photoType, final String caption, int count, String imgUrl, Bitmap bm)
    {
        Log.d(TAG, "uploadNewPhoto: attempting to upload a new photo.");
        
        FilePaths filePaths = new FilePaths();
        //case 1: new photo
        if (photoType.equals(m_context.getString(R.string.new_photo)))
        {
            Log.d(TAG, "uploadNewPhoto: uploading new photo.");
            
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = m_storageReference
                    .child(String.format("%s/%s/photo%d", filePaths.FIREBASE_IMAGE_STORAGE, user_id, count + 1));
            
            //convert image url to bitmap
            if (bm == null)
            {
                bm = ImageManager.getBitmap(imgUrl);
            }
            
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
            
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(m_context, "Photo upload failed.", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();
    
                    Toast.makeText(m_context, "Photo upload successful.", Toast.LENGTH_SHORT).show();
                    
                    //add the new photo to 'photo' node and 'user_photos' node
                    addPhotoToDatabase(caption, firebaseUrl.toString());
                    //navigate to the main feed so the user can wee their photo
                    Intent intent = new Intent(m_context, HomeActivity.class);
                    m_context.startActivity(intent);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                {
                    float progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    
                    if (progress - 15 > m_photoUploadProgress)
                    {
                        Toast.makeText(m_context, String.format("Photo upload in progess: %s", String.format("%.0f", progress)), Toast.LENGTH_SHORT).show();
                        m_photoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: "+progress+" % done");
                }
            });
        }
        //case new profile photo
        else if (photoType.equals(m_context.getString(R.string.profile_photo)))
        {
            Log.d(TAG, "uploadNewPhoto: uploading new profile photo.");
    
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = m_storageReference
                    .child(String.format("%s/%s/profile_photo", filePaths.FIREBASE_IMAGE_STORAGE, user_id));
    
            //convert image url to bitmap
            if (bm == null)
            {
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
    
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(m_context, "Photo upload failed.", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();
            
                    Toast.makeText(m_context, "Photo upload successful.", Toast.LENGTH_SHORT).show();
            
                    //insert into 'user_account_settings' node
                    setProfilePhoto(firebaseUrl.toString());
    
                    ((AccountSettingsActivity)m_context).setViewPager(
                            ((AccountSettingsActivity)m_context).m_pagerAdapter.getFragmentNumber(m_context.getString(R.string.edit_profile_fragment))
                    );
                    
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                {
                    float progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            
                    if (progress - 15 > m_photoUploadProgress)
                    {
                        Toast.makeText(m_context, String.format("Photo upload in progess: %s", String.format("%.0f", progress)), Toast.LENGTH_SHORT).show();
                        m_photoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: "+progress+" % done");
                }
            });
        }
    }
    
    private void setProfilePhoto(String url)
    {
        Log.d(TAG, String.format("setProfilePhoto: setting new profile image: %s", url));
    
        myRef.child(m_context.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(m_context.getString(R.string.profile_photo))
                .setValue(url);
    }
    
    private String getTimeStamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
        return sdf.format(new Date());
    }
    
    private void addPhotoToDatabase(String caption, String url)
    {
        Log.d(TAG, "addPhotoToDatabase: adding photo to database");
        
        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(m_context.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDateCreated(getTimeStamp());
        photo.setImagePath(url);
        photo.setTags(tags);
        photo.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhotoId(newPhotoKey);
        
        //insert into databse
        myRef.child(m_context.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey)
                .setValue(photo);
        myRef.child(m_context.getString(R.string.dbname_photos))
                .child(newPhotoKey)
                .setValue(photo);
        
    }
    
    public int getImageCount(DataSnapshot dataSnapshot)
    {
        int count = 0;
        for (DataSnapshot ds : dataSnapshot
                .child(m_context.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren())
        {
            count++;
        }
        return count;
    }
    
    /**
     * update 'user_account_settings' node for the current user
     *
     * @param displayName
     * @param website
     * @param description
     * @param phoneNumber
     */
    public void updateUserAccountSettings(
            String displayName,
            String website,
            String description,
            long phoneNumber)
    {
        Log.d(TAG, "updateUserAccountSettings: updating user account settings.");
        if (displayName != null)
        {
            myRef.child(m_context.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(m_context.getString(R.string.field_display_name))
                    .setValue(displayName);
        }
        
        if (website != null)
        {
            myRef.child(m_context.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(m_context.getString(R.string.field_website))
                    .setValue(website);
        }
        
        if (description != null)
        {
            myRef.child(m_context.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(m_context.getString(R.string.field_description))
                    .setValue(description);
        }
        
        if (phoneNumber != 0)
        {
            myRef.child(m_context.getString(R.string.dbname_users))
                    .child(userId)
                    .child(m_context.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }
    }
    
    /**
     * update the username in 'users' node and 'user_account_settings' node
     *
     * @param username
     */
    public void updateUsername(String username)
    {
        Log.d(TAG, String.format("updateUsername: updating username to: %s", username));
        
        myRef.child(m_context.getString(R.string.dbname_users))
                .child(userId)
                .child(m_context.getString(R.string.field_username))
                .setValue(username);
        
        myRef.child(m_context.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .child(m_context.getString(R.string.field_username))
                .setValue(username);
    }
    
    /**
     * update the email in the 'users' node
     *
     * @param email
     */
    public void updateEmail(String email)
    {
        Log.d(TAG, String.format("updateUsername: updating username to: %s", email));
        
        myRef.child(m_context.getString(R.string.dbname_users))
                .child(userId)
                .child(m_context.getString(R.string.field_email))
                .setValue(email);
    }

//    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot)
//    {
//        Log.d(TAG, "checkIfUsernameExists: Checking if username already exists.");
//
//        User user = new User();
//
//        for (DataSnapshot ds : dataSnapshot.child(userId).getChildren())
//        {
//            Log.d(TAG, String.format("checkIfUsernameExists: datasnapshot: %s", ds));
//
//            user.setUsername(ds.getValue(User.class).getUsername());
//            Log.d(TAG, "checkIfUsernameExists: username: "+user.getUsername());
//
//            if (StringManipulation.expandUsername(user.getUsername()).equals(username))
//            {
//                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: "+user.getUsername());
//                return true;
//            }
//        }
//        return false;
//    }
    
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
                                Toast.makeText(m_context,
                                        "Couldn't send verification email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    
    /**
     * Add information to the users node
     * Add information to the user_account_settings node
     *
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public void addNewUser(
            String email,
            String username,
            String description,
            String website,
            String profile_photo)
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
                website,
                userId
        );
        myRef.child(m_context.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .setValue(settings);
    }
    
    /**
     * Retrieves the account settings for the user currently logged in.
     * Databse: user_account_settings node
     *
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
