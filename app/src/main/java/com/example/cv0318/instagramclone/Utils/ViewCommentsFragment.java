package com.example.cv0318.instagramclone.Utils;

import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cv0318.instagramclone.Models.Comment;
import com.example.cv0318.instagramclone.Models.Like;
import com.example.cv0318.instagramclone.Models.Photo;
import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewCommentsFragment extends Fragment
{
    private static final String TAG = String.format("%s_TAG", ViewCommentsFragment.class.getSimpleName());

    //firebase
    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authStateListener;
    private FirebaseDatabase m_firebaseDatabase;
    private DatabaseReference myRef;
    //vars
    private Photo m_photo;
    private ArrayList<Comment> m_comments;

    //widgets
    private ImageView m_backArrow, m_checkMark;
    private EditText m_comment;
    private ListView m_listview;

    public ViewCommentsFragment()
    {
        super();
        setArguments(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        m_backArrow = view.findViewById(R.id.ivCommentsBackArrow);
        m_checkMark = view.findViewById(R.id.ivPostComment);
        m_comment = view.findViewById(R.id.etComment);
        m_comments = new ArrayList<>();
        m_listview = view.findViewById(R.id.lvComments);

        setupFirebaseAuth();

        try
        {
            m_photo = getPhotoFromBundle();
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, String.format("onCreateView: NullPointerException: %s", e.getMessage()));
        }

        return view;
    }

    private void setupWidgets()
    {

        CommentListAdapter adapter = new CommentListAdapter(getActivity(), R.layout.layout_comment, m_comments);
        m_listview.setAdapter(adapter);

        m_checkMark.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!m_comment.getText().toString().isEmpty())
                {
                    Log.d(TAG, "onClick: attempting to submit a new comment...");
                    addNewComment(m_comment.getText().toString());

                    m_comment.setText("");
                    closeKeyboard();
                }
                else
                {
                    Toast.makeText(getActivity(), "You cannot post a blank comment.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void closeKeyboard()
    {
        View view = getActivity().getCurrentFocus();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment)
    {
        Log.d(TAG, String.format("addNewComment: Adding new comment: %s", newComment));

        String commentId = myRef.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimeStamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //insert into 'photos' node
        myRef.child(getString(R.string.dbname_photos))
                .child(m_photo.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentId)
                .setValue(comment);

        //insert into 'user_photos' node
        myRef.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(m_photo.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentId)
                .setValue(comment);
    }

    private String getTimeStamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
        return sdf.format(new Date());
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

        myRef.child(getString(R.string.dbname_photos))
                .child(m_photo.getPhoto_id())
                .child(getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Query query = myRef
                                .child(getString(R.string.dbname_photos))
                                .orderByChild(getString(R.string.field_photo_id))
                                .equalTo(m_photo.getPhoto_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                                {
                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                                    m_comments.clear();
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(m_photo.getCaption());
                                    firstComment.setUser_id(m_photo.getUser_id());
                                    firstComment.setDate_created(m_photo.getDate_created());

                                    m_comments.add(firstComment);

                                    for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_comments)).getChildren())
                                    {
                                        Comment comment = new Comment();
                                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                        m_comments.add(comment);
                                    }

                                    photo.setComments(m_comments);

                                    m_photo = photo;

                                    setupWidgets();

//                    List<Like> likeList = new ArrayList<>();
//                    for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_likes)).getChildren())
//                    {
//                        Like like = new Like();
//                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//                        likeList.add(like);
//                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {
                                Log.d(TAG, "onCancelled: query cancelled.");
                            }
                        });
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s)
                    {

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
