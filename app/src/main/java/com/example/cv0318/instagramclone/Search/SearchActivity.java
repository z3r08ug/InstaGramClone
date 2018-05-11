package com.example.cv0318.instagramclone.Search;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.Profile.ProfileActivity;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.cv0318.instagramclone.Utils.UserListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity
{
    private static final String TAG = String.format("%s_TAG", SearchActivity.class.getSimpleName());
    private static final int ACTIVITY_NUM = 1;

    //vars
    private Context m_context = SearchActivity.this;
    private List<User> m_userList;
    private UserListAdapter m_adapter;

    //widgets
    private EditText m_searchParam;
    private ListView m_listView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        m_searchParam = findViewById(R.id.etSearchParam);
        m_listView = findViewById(R.id.lvSearch);
        Log.d(TAG, "onCreate: started.");

        hideSoftKeyboard();
        setupBottomNavigationView();
        initTextListener();
    }

    private void initTextListener()
    {
        Log.d(TAG, "initTextListener: initializing....");

        m_userList = new ArrayList<>();

        m_searchParam.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
//                String text = m_searchParam.getText().toString().toLowerCase(Locale.getDefault());
                String text = m_searchParam.getText().toString();
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword)
    {
        Log.d(TAG, String.format("searchForMatch: searching for a match: %s", keyword));

        m_userList.clear();
        if (keyword.length() == 0)
        {

        }
        else
        {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username))
                    .equalTo(keyword);
            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                    {
                        Log.d(TAG, "onDataChange: found user: "+singleSnapshot.getValue(User.class).toString());

                        m_userList.add(singleSnapshot.getValue(User.class));
                        //update the users list view
                        updateUsersList();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    private void updateUsersList()
    {
        Log.d(TAG, "updateUsersList: updating users list.");

        m_adapter = new UserListAdapter(SearchActivity.this, R.layout.layout_user_listitem, m_userList);
        
        m_listView.setAdapter(m_adapter);
        
        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, "onItemClick: selected user: "+m_userList.get(position).toString());

                //navigate to Profile Activity
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user), m_userList.get(position));
                startActivity(intent);
            }
        });
    }

    private void hideSoftKeyboard()
    {
        if (getCurrentFocus() != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "setupBottomNavigationView: Setting up nav.");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(this, this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
