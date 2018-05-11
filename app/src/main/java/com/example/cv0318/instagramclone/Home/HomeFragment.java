package com.example.cv0318.instagramclone.Home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.cv0318.instagramclone.Models.Comment;
import com.example.cv0318.instagramclone.Models.Like;
import com.example.cv0318.instagramclone.Models.Photo;
import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.MainfeedListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment
{
    private static final String TAG = String.format("%s_TAG", HomeFragment.class.getSimpleName());

    //vars
    private List<Photo> m_photos;
    private List<String> m_following;
    private List<Photo> m_paginatedPhotos;
    private ListView m_listView;
    private MainfeedListAdapter m_adapter;
    private int m_results;

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        m_listView = view.findViewById(R.id.lvHome);
        m_following = new ArrayList<>();
        m_photos = new ArrayList<>();

        getFollowing();

        return view;
    }

    private void  getFollowing()
    {
        Log.d(TAG, "getFollowing: searching for following");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onDataChange: get following snapshot: " + dataSnapshot);
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: Found user: "+singleSnapshot.child(getString(R.string.field_user_id)).getValue());

                    m_following.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }
                m_following.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                //get photos
                getPhotos();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void getPhotos()
    {
        Log.d(TAG, "getPhotos: Getting photos.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for (int i = 0; i < m_following.size(); i++)
        {
            final int count = i;
            Query query = reference
                    .child(getString(R.string.dbname_user_photos))
                    .child(m_following.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(m_following.get(i));

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    Log.d(TAG, "onDataChange: get following snapshot: " + dataSnapshot);
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
                        m_photos.add(photo);
                    }
                    if (count >= m_following.size() - 1)
                    {
                        //display the photos
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    private void displayPhotos()
    {
        m_paginatedPhotos = new ArrayList<>();
        if (!m_photos.isEmpty())
        {
            try
            {
                Collections.sort(m_photos, new Comparator<Photo>()
                {
                    @Override
                    public int compare(Photo o1, Photo o2)
                    {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iterations = m_photos.size();
                if (iterations > 10)
                {
                    iterations = 10;
                }

                m_results = 10;
                for (int i = 0; i < iterations; i++)
                {
                    m_paginatedPhotos.add(m_photos.get(i));
                }

                m_adapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, m_paginatedPhotos);
                m_listView.setAdapter(m_adapter);
            }
            catch (NullPointerException | IndexOutOfBoundsException e)
            {
                Log.e(TAG, String.format("displayPhotos: %s", e.getMessage()));
            }
        }
    }

    public void displayMorePhotos()
    {
        Log.d(TAG, "displayMorePhotos: displaying more photos.");
        
        try
        {
            if (m_photos.size() > m_results && m_photos.size() > 0)
            {
                int iterations;
                if (m_photos.size() > (m_results + 10))
                {
                    Log.d(TAG, "displayMorePhotos: There are more than 10 photos.");

                    iterations = 10;
                }
                else
                {
                    Log.d(TAG, "displayMorePhotos: There is less than 10 more photos.");

                    iterations = m_photos.size() - m_results;
                }

                //add the new photos to the paginated results
                for (int i = m_results; i < m_results + iterations; i++)
                {
                    m_paginatedPhotos.add(m_photos.get(i));
                }
                m_results = m_results + iterations;
                m_adapter.notifyDataSetChanged();
            }
        }
        catch (NullPointerException | IndexOutOfBoundsException e)
        {
            Log.e(TAG, String.format("displayMorePhotos: %s", e.getMessage()));
        }
    }
}
