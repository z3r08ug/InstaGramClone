package com.example.cv0318.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cv0318.instagramclone.Home.HomeActivity;
import com.example.cv0318.instagramclone.Models.Comment;
import com.example.cv0318.instagramclone.Models.Like;
import com.example.cv0318.instagramclone.Models.Photo;
import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.Profile.ProfileActivity;
import com.example.cv0318.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Photo>
{
    public interface OnLoadMoreItemsListener
    {
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener m_onLoadMoreItemsListener;

    private static final String TAG = String.format("%s_TAG", MainfeedListAdapter.class.getSimpleName());

    //vars
    private LayoutInflater m_inflater;
    private int m_layoutResource;
    private Context m_context;
    private DatabaseReference m_reference;
    private String currentUsername = "";

    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects)
    {
        super(context, resource, objects);

        m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_layoutResource = resource;
        m_context = context;
        m_reference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder
    {
        CircleImageView profileImage;
        String likesString;
        TextView username, timeDelta, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likedByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        final ViewHolder holder;

        if (convertView == null)
        {
            convertView = m_inflater.inflate(m_layoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = convertView.findViewById(R.id.postUserName);
            holder.image = convertView.findViewById(R.id.postImage);
            holder.heartRed = convertView.findViewById(R.id.ivRedHeart);
            holder.heartWhite = convertView.findViewById(R.id.ivHeartOutline);
            holder.comment = convertView.findViewById(R.id.ivSpeechBubble);
            holder.likes = convertView.findViewById(R.id.tvImageLikes);
            holder.comments = convertView.findViewById(R.id.tvImageCommentsLink);
            holder.caption = convertView.findViewById(R.id.tvImageCaption);
            holder.timeDelta = convertView.findViewById(R.id.tvImageTimePosted);
            holder.profileImage = convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(m_context, new GestureListener(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        //get the current user's username before getting likes string
        getCurrentUsername();

        //get likes string
        getLikesString(holder);

        //set the caption
        holder.caption.setText(getItem(position).getCaption());

        //set the comment
        List<Comment> comments = getItem(position).getComments();
        holder.comments.setText(String.format("View all %d comments.", comments.size()));
        holder.comments.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Loading comment thread for "+getItem(position).getPhoto_id());

                ((HomeActivity) m_context).onCommentThreadSelected(getItem(position), m_context.getString(R.string.home_activity));

                //going to need to do something else
                ((HomeActivity) m_context).hideLayout();
            }
        });

        //set the time it was posted
        String timestampDifference = getTimestampDifference(getItem(position));
        if (!timestampDifference.equals("0"))
        {
            holder.timeDelta.setText(String.format("%s DAYS AGO", timestampDifference));
        }
        else
        {
            holder.timeDelta.setText("TODAY");
        }

        //set the profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);

        //get the profile image and username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(m_context.getString(R.string.dbname_user_account_settings))
                .orderByChild(m_context.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onDataChange: Adding Like snapshot: " + dataSnapshot);
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Log.d(TAG, "onClick: navigating to profile of selected user");

                            Intent intent = new Intent(m_context, ProfileActivity.class);
                            intent.putExtra(m_context.getString(R.string.calling_activity),
                                    m_context.getString(R.string.home_activity));
                            intent.putExtra(m_context.getString(R.string.intent_user), holder.user);
                            m_context.startActivity(intent);
                        }
                    });

                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(), holder.profileImage);
                    holder.profileImage.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Log.d(TAG, "onClick: navigating to profile of selected user");

                            Intent intent = new Intent(m_context, ProfileActivity.class);
                            intent.putExtra(m_context.getString(R.string.calling_activity),
                                    m_context.getString(R.string.home_activity));
                            intent.putExtra(m_context.getString(R.string.intent_user), holder.user);
                            m_context.startActivity(intent);
                        }
                    });

                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);

                    holder.comment.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            ((HomeActivity) m_context).onCommentThreadSelected(getItem(position), m_context.getString(R.string.home_activity));

                            //another thing?
                            ((HomeActivity) m_context).hideLayout();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        //get the user object
        Query userQuery = m_reference
                .child(m_context.getString(R.string.dbname_users))
                .orderByChild(m_context.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onDataChange: User snapshot: " + dataSnapshot);
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: Found user: "+singleSnapshot.getValue(User.class).getUsername());

                    holder.user = singleSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        if (reachedEndOfList(position))
        {
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfList(int position)
    {
        return position == getCount() - 1;
    }

    private void loadMoreData()
    {
        try
        {
            m_onLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();
        }
        catch (ClassCastException e)
        {
            Log.e(TAG, String.format("loadMoreData: ClassCastException: %s", e.getMessage()));
        }

        try
        {
            m_onLoadMoreItemsListener.onLoadMoreItems();
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, String.format("loadMoreData: NullPointerException: %s", e.getMessage()));
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        ViewHolder m_holder;
        public GestureListener(ViewHolder holder)
        {
            m_holder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(m_context.getString(R.string.dbname_photos))
                    .child(m_holder.photo.getPhoto_id())
                    .child(m_context.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    Log.d(TAG, "onDataChange: Adding Like snapshot: " + dataSnapshot);
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                    {
                        String keyId = singleSnapshot.getKey();
                        //case 1: the user already liked the photo
                        if (m_holder.likedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        {
                            Log.d(TAG, "onDataChange: user has already liked the photo so lets remove the like.");
                            m_reference.child(m_context.getString(R.string.dbname_photos))
                                    .child(m_holder.photo.getPhoto_id())
                                    .child(m_context.getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            m_reference.child(m_context.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(m_holder.photo.getPhoto_id())
                                    .child(m_context.getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            m_holder.heart.toggleLike();
                            getLikesString(m_holder);
                        }

                        //case 2: the user has not liked the photo
                        else if (!m_holder.likedByCurrentUser)
                        {
                            Log.d(TAG, "onDataChange: User has not yet liked the photo so let's add a new like on it.");
                            //add new like
                            addNewLike(m_holder);
                            break;
                        }
                    }
                    if (!dataSnapshot.exists())
                    {
                        Log.d(TAG, "onDataChange: Snapshot doesn't exist so add the like");
                        //add new like
                        addNewLike(m_holder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

            return true;
        }
    }

    private void addNewLike(ViewHolder holder)
    {
        String newLikeId = m_reference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        m_reference.child(m_context.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(m_context.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        m_reference.child(m_context.getString(R.string.dbname_user_photos))
                .child(holder.user.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(m_context.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getCurrentUsername()
    {
        Log.d(TAG, "getCurrentUsername: Retrieving user account settings");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(m_context.getString(R.string.dbname_users))
                .orderByChild(m_context.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onDataChange: Adding Like snapshot: " + dataSnapshot);
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void getLikesString(final ViewHolder holder)
    {
        Log.d(TAG, "getLikesString: getting likes string for photoid: " + holder.photo.getPhoto_id());

        try
        {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(m_context.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(m_context.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    Log.d(TAG, "Looking for 'likes' node onDataChange: firstSnapshot: " + dataSnapshot);
                    holder.users = new StringBuilder();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                    {
                        Log.d(TAG, "Looking for 'likes' node onDataChange: firstSnapshot child: " + singleSnapshot);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(m_context.getString(R.string.dbname_users))
                                .orderByChild(m_context.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                                {
                                    String user = singleSnapshot.getValue(User.class).getUsername();
                                    Log.d(TAG, "Finding user from 'likes' node: User: " + user + " liked the photo");
                                    holder.users.append(user);
                                    holder.users.append(",");
                                    Log.d(TAG, "onDataChange: Current LikesString: " + holder.users.toString());
                                }
                                if (!dataSnapshot.exists())
                                {
                                    Log.d(TAG, "onDataChange: There were no user ids that matched the liked user ids");
                                }
                                else
                                {
                                    Log.d(TAG, "onDataChange: Users that liked photo: " + holder.users.toString());

                                    String[] splitUsers = holder.users.toString().split(",");

                                    if (holder.users.toString().contains(currentUsername + ","))
                                    {
                                        holder.likedByCurrentUser = true;
                                        Log.d(TAG, "onDataChange: liked by current user");
                                    }
                                    else
                                    {
                                        Log.d(TAG, "onDataChange: not liked by current user");
                                        holder.likedByCurrentUser = false;
                                    }

                                    int length = splitUsers.length;
                                    Log.d(TAG, "onDataChange: Length of SplitUsers: " + length);
                                    for (int i = 0; i < length; i++)
                                    {
                                        Log.d(TAG, "onDataChange: SplitUser # " + i + ": " + splitUsers[i]);
                                    }
                                    Log.d(TAG, "onDataChange: is user 0 blank: " + splitUsers[0].isEmpty());

                                    if (length == 1 && !splitUsers[0].isEmpty())
                                    {
                                        holder.likesString = String.format("Liked by %s", splitUsers[0]);
                                    }
                                    else if (length == 2)
                                    {
                                        holder.likesString = String.format("Liked by %s and %s", splitUsers[0], splitUsers[1]);
                                    }
                                    else if (length == 3)
                                    {
                                        holder.likesString = String.format("Liked by %s, %s, and %s", splitUsers[0], splitUsers[1], splitUsers[2]);
                                    }
                                    else if (length == 4)
                                    {
                                        holder.likesString = String.format("Liked by %s, %s, %s, and %s", splitUsers[0], splitUsers[1], splitUsers[2], splitUsers[3]);
                                    }
                                    else if (length > 4)
                                    {
                                        holder.likesString = String.format("Liked by %s, %s, %s, and %s others", splitUsers[0], splitUsers[1], splitUsers[2], (splitUsers.length - 3));
                                    }


                                    Log.d(TAG, String.format("onDataChange: likes string:%s", holder.likesString));

                                    //setup LikesString
                                    setupLikesString(holder, holder.likesString);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                    }

                    if (!dataSnapshot.exists())
                    {
                        Log.d(TAG, "onDataChange: snapshot did not exist. There are no likes on the photo.");
                        holder.likesString = "";
                        holder.likedByCurrentUser = false;
                        setupLikesString(holder, holder.likesString);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, String.format("getLikesString: NullPointerException: %s", e.getMessage()));
            holder.likesString = "";
            holder.likedByCurrentUser = false;
            setupLikesString(holder, holder.likesString);
        }
    }

    private void setupLikesString(final ViewHolder holder, String likesString)
    {
        Log.d(TAG, String.format("setupLikesString: likes string: %s", holder.likesString));

        if (holder.likedByCurrentUser)
        {
            Log.d(TAG, "setupLikesString: photo is liked by current user.");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        else
        {
            Log.d(TAG, "setupLikesString: photo is NOT liked by current user.");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.likes.setText(likesString);
    }

    /**
     * Returns a string representing the number of days ago the post was made.
     *
     * @return
     */
    private String getTimestampDifference(Photo photo)
    {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
        Date today = c.getTime();
        sdf.format(today);
        Date timeStamp;
        final String photoTimeStamp = photo.getDate_created();
        try
        {
            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round(((today.getTime() - timeStamp.getTime()) / 1000 / 60 / 60 / 24)));
        }
        catch (ParseException e)
        {
            Log.e(TAG, String.format("getTimestampDifference: %s", e.getMessage()));
            difference = "0";
        }
        return difference;
    }
}
