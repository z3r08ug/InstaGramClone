package com.example.cv0318.instagramclone.Share;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class NextActivity extends AppCompatActivity
{
    private static final String TAG = String.format("%s_TAG", NextActivity.class.getSimpleName());
    
    //firebase
    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;
    private FirebaseDatabase m_firebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods m_firebaseMethods;
    
    //widgets
    private EditText m_etCaption;
    
    //vars
    private String m_append = "file:/";
    private int imageCount = 0;
    private String imgUrl;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        m_firebaseMethods = new FirebaseMethods(this);
        m_etCaption = findViewById(R.id.etCaption);
        
        setupFirebaseAuth();
    
        ImageView nextClose = findViewById(R.id.ivCloseNext);
        nextClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: closing next activity.");
            
                finish();
            }
        });
    
        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: navigating to the final share screen");
                //upload the image to Firebase
                Toast.makeText(NextActivity.this, "Attempting to upload a new photo.", Toast.LENGTH_SHORT).show();
                String caption = m_etCaption.getText().toString();
                m_firebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl, null);
            }
        });
        setImage();
    }
    
    private void someMethod()
    {
        /*
            Step 1: Create a data model for Photos
            
            Step 2: Add properties to the Photo Objects (caption, date, imageUrl,
            photo_id, tags, user_id)
            
            Step 3: Count the number of photos the user already has.
            
            Step 4: Upload the photo to Firebase Storage and insert 2 new nodes in the database
            a) 'photos' node
            b) 'user_photos' node
         */
        
        
    }
    
    /**
     * gets the image url from the incoming intent and displays the chosen image.
     */
    private void setImage()
    {
        Intent intent = getIntent();
        ImageView image = findViewById(R.id.imageShare);
        imgUrl = intent.getStringExtra(getString(R.string.selected_image));
        Log.d(TAG, "setImage: image url: " + imgUrl);
        UniversalImageLoader.setImage(imgUrl, image, null, m_append);
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
        Log.d(TAG, String.format("onDataChange: image count: %d", imageCount));
    
    
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
                imageCount = m_firebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, String.format("onDataChange: image count: %d", imageCount));
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
