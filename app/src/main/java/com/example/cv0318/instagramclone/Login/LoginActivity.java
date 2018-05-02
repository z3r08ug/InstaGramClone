package com.example.cv0318.instagramclone.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
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

import com.example.cv0318.instagramclone.Home.HomeActivity;
import com.example.cv0318.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity
{
    private static final String TAG = String.format("%s_TAG", LoginActivity.class.getSimpleName());

    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;

    private Context m_context;
    private ProgressBar m_progressBar;
    private EditText m_email, m_password;
    private TextView m_waiting;

    @Override
    public void onCreate(
        @Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        m_progressBar = findViewById(R.id.pbLogin);
        m_waiting = findViewById(R.id.tvWaitingLogin);
        m_email = findViewById(R.id.input_email);
        m_password = findViewById(R.id.input_password);
        m_context = LoginActivity.this;

        Log.d(TAG, "onCreate: started.");

        m_waiting.setVisibility(View.GONE);
        m_progressBar.setVisibility(View.GONE);

        setupFirebaseAuth();
        init();
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

    private void init()
    {
        //initialize button for logging in
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Attempting to login.");

                String email = m_email.getText().toString();
                String password = m_password.getText().toString();

                if (isStringNull(email) && isStringNull(password))
                {
                    Toast.makeText(m_context,
                        "You must fill out all the fields.",
                        Toast.LENGTH_SHORT).show();
                }
                else
                {
                    m_progressBar.setVisibility(View.VISIBLE);
                    m_waiting.setVisibility(View.VISIBLE);

                    m_auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this,
                            new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    Log.d(TAG, String.format("onComplete: %s", task.isSuccessful()));
                                    FirebaseUser user = m_auth.getCurrentUser();

                                    if (task.isSuccessful())
                                    {
                                        // Sign in success, update UI with the signed-in user's
                                        try
                                        {
                                            if (user.isEmailVerified())
                                            {
                                                Log.d(TAG, "onComplete: Email is verified, navigating to home activity.");
                                                startActivity(new Intent(m_context, HomeActivity.class));
                                            }
                                            else
                                            {
                                                Toast.makeText(m_context, "Email is not verified, check your inbox.", Toast.LENGTH_SHORT)
                                                    .show();
                                                m_progressBar.setVisibility(View.GONE);
                                                m_waiting.setVisibility(View.GONE);
                                                m_auth.signOut();
                                            }
                                        }
                                        catch (NullPointerException e)
                                        {
                                            Log.e(TAG,
                                                String.format("onComplete: Null Pointer Exception: %s",
                                                    e.getMessage()), e);
                                        }
                                    }
                                    else
                                    {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this,
                                            R.string.auth_failed,
                                            Toast.LENGTH_SHORT).show();
                                        m_progressBar.setVisibility(View.GONE);
                                        m_waiting.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        TextView linkSignUp = findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: navigating to register screen");
                startActivity(new Intent(m_context, RegisterActivity.class));
            }
        });
        
        /*
        If the user is logged in then navigate to the HomeActivity
         */
        if (m_auth.getCurrentUser() != null)
        {
            startActivity(new Intent(m_context, HomeActivity.class));
            finish();
        }
    }

    /**
     * Setup Firebase Auth Object.
     */
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "setupFirebaseAuth: Setting up Firebase Auth.");
        m_auth = FirebaseAuth.getInstance();
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
