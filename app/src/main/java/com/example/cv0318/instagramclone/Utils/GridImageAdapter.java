package com.example.cv0318.instagramclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.cv0318.instagramclone.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class GridImageAdapter extends ArrayAdapter<String>
{
    private Context m_context;
    private LayoutInflater m_layoutInflater;
    private int m_layoutResource;
    private String m_append;
    private ArrayList<String> m_imageUrls;

    public GridImageAdapter(
        Context context,
        int layoutResource,
        String append, ArrayList<String> imageUrls)
    {
        super(context, layoutResource, imageUrls);
        m_context = context;
        m_layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_layoutResource = layoutResource;
        m_append = append;
        m_imageUrls = imageUrls;
    }

    private static class ViewHolder
    {
        SquareImageView m_image;
        ProgressBar m_progressBar;
    }

    @NonNull
    @Override
    public View getView(
        int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        final ViewHolder holder;

        if (convertView == null)
        {
            convertView = m_layoutInflater.inflate(m_layoutResource, parent, false);
            holder = new ViewHolder();
            holder.m_progressBar = convertView.findViewById(R.id.gvProgressBar);
            holder.m_image = convertView.findViewById(R.id.gvImageView);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        String imgUrl = getItem(position);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(m_append + imgUrl, holder.m_image, new ImageLoadingListener()
        {
            @Override
            public void onLoadingStarted(String imageUri, View view)
            {
                if (holder.m_progressBar != null)
                {
                    holder.m_progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(
                String imageUri, View view, FailReason failReason)
            {
                if (holder.m_progressBar != null)
                {
                    holder.m_progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(
                String imageUri, View view, Bitmap loadedImage)
            {
                if (holder.m_progressBar != null)
                {
                    holder.m_progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view)
            {
                if (holder.m_progressBar != null)
                {
                    holder.m_progressBar.setVisibility(View.GONE);
                }
            }
        });

        return convertView;
    }
}
