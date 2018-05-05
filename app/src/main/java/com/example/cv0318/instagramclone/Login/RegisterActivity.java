package com.example.cv0318.instagramclone.Login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity
{
    private static final String TAG = String.format("%s_TAG",
        RegisterActivity.class.getSimpleName());

    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;
    private FirebaseMethods m_firebaseMethods;
    private FirebaseDatabase m_firebaseDatabase;
    private DatabaseReference myRef;

    private Context m_context;
    private String m_email, m_password, m_username;
    private EditText m_etEmail, m_etPassword, m_etUserName;
    private TextView m_waiting;
    private Button m_btnRegister;
    private ProgressBar m_progressBar;

    private String append = "";

    @Override
    public void onCreate(
        @Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        m_context = RegisterActivity.this;
        m_firebaseMethods = new FirebaseMethods(m_context);
        Log.d(TAG, "onCreate: started.");

        initWidgets();
        setupFirebaseAuth();
        init();
    }

    private void init()
    {
        m_btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_email = m_etEmail.getText().toString();
                m_username = m_etUserName.getText().toString();
                m_password = m_etPassword.getText().toString();

                if (checkInputs(m_email, m_username, m_password))
                {
                    m_progressBar.setVisibility(View.VISIBLE);
                    m_waiting.setVisibility(View.VISIBLE);

                    m_firebaseMethods.registerNewEmail(m_email, m_password, m_username);
                }
            }
        });
    }

    private boolean checkInputs(String email, String username, String password)
    {
        Log.d(TAG, "checkInputs: checking inputs for null values");
        if (email.isEmpty() || username.isEmpty() || password.isEmpty())
        {
            Toast.makeText(m_context, "All fields must be filled out.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Initialize the activity widgets
     */
    private void initWidgets()
    {
        Log.d(TAG, "initWidgets: initializing widgets");
        m_btnRegister = findViewById(R.id.btnRegister);
        m_etEmail = findViewById(R.id.input_email);
        m_etPassword = findViewById(R.id.input_password);
        m_etUserName = findViewById(R.id.input_username);
        m_progressBar = findViewById(R.id.pbRegister);
        m_context = RegisterActivity.this;
        m_waiting = findViewById(R.id.tvWaitingRegister);
        m_progressBar.setVisibility(View.GONE);
        m_waiting.setVisibility(View.GONE);
    }

    private boolean isStringNull(String string)
    {
        Log.d(TAG, "isStringNull: checking if string is null");

        if (string.isEmpty())
        { return true; }
        else
        { return false; }
    }

    /**
     * ---------------------------------------Firebase--------------------------------------
     */

    /**
     * Check if @param username already exists in the database
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
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren())
                {
                    if (singleSnapshot.exists())
                    {
                        Log.d(TAG, "onDataChange: FOUND A MATCH: "+singleSnapshot.getValue(User.class).getUsername());
                        append = myRef.push().getKey().substring(3, 10);
                        Log.d(TAG, String.format(
                            "onDataChange: Username already exists. Appending random " +
                                "string to name: %s", append));

                    }
                }

                m_username += append;
                //add new user to database
                m_firebaseMethods.addNewUser(m_email, m_username, "", "", "");

                Toast.makeText(m_context, "Signup successful. Sending verification email.", Toast.LENGTH_SHORT).show();

                m_auth.signOut();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

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
                final FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null)
                {
                    //user is signed in
                    Log.d(TAG, String.format("onAuthStateChanged: signed in: %s", user.getUid()));

                    myRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            checkIfUsernameExists(m_username);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });

                    finish();
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
    protected void onStart()
    {
        super.onStart();
        m_auth.addAuthStateListener(m_authStateListener);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (m_authStateListener != null)
        { m_auth.removeAuthStateListener(m_authStateListener); }
    }
}
