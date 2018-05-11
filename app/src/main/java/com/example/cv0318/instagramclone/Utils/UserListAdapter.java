package com.example.cv0318.instagramclone.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.cv0318.instagramclone.Models.User;
import com.example.cv0318.instagramclone.Models.UserAccountSettings;
import com.example.cv0318.instagramclone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends ArrayAdapter<User>
{
    private static final String TAG = String.format("%s_TAG", UserListAdapter.class.getSimpleName());

    private LayoutInflater m_inflater;
    private List<User> m_users;
    private int layoutResource;
    private Context m_context;

    public UserListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects)
    {
        super(context, resource, objects);
        m_context = context;
        m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        m_users = objects;
    }

    private static class ViewHolder
    {
        TextView username, email;
        CircleImageView profileImage;
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

            holder.username = convertView.findViewById(R.id.tvSearchUsername);
            holder.email = convertView.findViewById(R.id.tvSearchEmail);
            holder.profileImage = convertView.findViewById(R.id.civProfileImage);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.username.setText(getItem(position).getUsername());
        holder.email.setText(getItem(position).getEmail());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(m_context.getString(R.string.dbname_user_account_settings))
                .orderByChild(m_context.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found user: "+singleSnapshot.getValue(UserAccountSettings.class).toString());

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        return convertView;
    }
}
