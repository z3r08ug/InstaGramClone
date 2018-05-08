package com.example.cv0318.instagramclone.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cv0318.instagramclone.Models.Comment;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.R;
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

public class CommentListAdapter extends ArrayAdapter<Comment>
{
    private static final String TAG = String.format("%s_TAG", CommentListAdapter.class.getSimpleName());

    private LayoutInflater m_inflater;
    private int layoutResource;
    private Context m_context;

    public CommentListAdapter(@NonNull Context context, int resource, @NonNull List<Comment> objects)
    {
        super(context, resource, objects);
        m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_context = context;
        layoutResource = resource;
    }

    private static class ViewHolder
    {
        TextView comment, username, timestamp, reply, likes;
        CircleImageView profileImage;
        ImageView like;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        final ViewHolder holder;

        if (convertView == null)
        {
            convertView = m_inflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.comment = convertView.findViewById(R.id.tvComment);
            holder.username = convertView.findViewById(R.id.tvCommentUsername);
            holder.timestamp = convertView.findViewById(R.id.tvCommentTimePosted);
            holder.reply = convertView.findViewById(R.id.tvCommentReply);
            holder.likes = convertView.findViewById(R.id.tvCommentLikes);
            holder.profileImage = convertView.findViewById(R.id.ivCommentProfileImage);
            holder.like = convertView.findViewById(R.id.ivCommentLike);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        //set the comment
        holder.comment.setText(getItem(position).getComment());

        //set the timestamp difference
        String timestampDifference = getTimestampDifference(getItem(position));
        if (!timestampDifference.equals("0"))
        {
            holder.timestamp.setText(String.format("%s d", timestampDifference));
        }
        else
        {
            holder.timestamp.setText("Today");
        }

        //set the username and profile image
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
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });

        try
        {
            if (position == 0)
            {
                holder.like.setVisibility(View.GONE);
                holder.likes.setVisibility(View.GONE);
                holder.reply.setVisibility(View.GONE);
            }
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, String.format("getView: NullPointerException: %s", e.getMessage()));
        }


        return convertView;
    }

    /**
     * Returns a string representing the number of days ago the post was made.
     *
     * @return
     */
    private String getTimestampDifference(Comment comment)
    {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
        Date today = c.getTime();
        sdf.format(today);
        Date timeStamp;
        final String photoTimeStamp = comment.getDate_created();
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
