package com.example.cv0318.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.cv0318.instagramclone.Profile.AccountSettingsActivity;
import com.example.cv0318.instagramclone.R;
import com.example.cv0318.instagramclone.Utils.FilePaths;
import com.example.cv0318.instagramclone.Utils.FileSearch;
import com.example.cv0318.instagramclone.Utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class GalleryFragment extends Fragment
{
    private static final String TAG = String.format("%s_TAG",
            GalleryFragment.class.getSimpleName());

    private static final int NUM_GRID_COLS = 3;

    //widgets
    private GridView m_gridView;
    private ImageView m_galleryImage;
    private ProgressBar m_progressBar;
    private Spinner m_directorySpinner;

    //vars
    private ArrayList<String> directories;
    private String m_append = "file:/";
    private String m_SelectedImage;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        m_gridView = view.findViewById(R.id.gvGallery);
        m_galleryImage = view.findViewById(R.id.ivGallery);
        m_directorySpinner = view.findViewById(R.id.spDirectory);
        m_progressBar = view.findViewById(R.id.pbGallery);
        m_progressBar.setVisibility(View.GONE);
        directories = new ArrayList<>();
        Log.d(TAG, "onCreateView: started");

        ImageView shareClose = view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: closing gallery fragment");

                getActivity().finish();
            }
        });

        TextView nextScreen = view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: navigating to the final share screen");

                if (isRootTask())
                {
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image), m_SelectedImage);
                    startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image), m_SelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        init();

        return view;
    }

    private boolean isRootTask()
    {
        if (((ShareActivity) getActivity()).getTask() == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void init()
    {
        FilePaths filePaths = new FilePaths();

        directories.add(filePaths.CAMERA);
        directories.add(filePaths.PICTURES);
        directories.add(filePaths.ZEDGE);
        directories.add(filePaths.FACEBOOK);
        directories.add(filePaths.DOWNLOAD);
        directories.add(filePaths.SD_CAMERA);

        ArrayList<String> directoryNames = new ArrayList<>();
        for (int i = 0; i < directories.size(); i++)
        {
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index);
            directoryNames.add(string);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, directoryNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_directorySpinner.setAdapter(adapter);

        m_directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, "onItemSelected: selected: " + directories.get(position));

                //setup our image for the directory chosen
                setupGridView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    private void setupGridView(String selectedDirectory)
    {
        //Log.d(TAG, String.format("setupGridView: directory chosen: %s", selectedDirectory));
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectedDirectory);

        Log.d(TAG, "setupGridView: image list size: " + imgURLs.size());

        //set grid column width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLS;
        m_gridView.setColumnWidth(imageWidth);

        //use grid adapter to adapt the images to gridview
        GridImageAdapter adapter = new GridImageAdapter(getActivity(),
                R.layout.layout_grid_imageview,
                m_append,
                imgURLs);
        m_gridView.setAdapter(adapter);

        //set first image to be displayed when the activity fragment view is inflated.
        try
        {
            setImage(imgURLs.get(0), m_galleryImage, m_append);
            m_SelectedImage = imgURLs.get(0);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            Log.e(TAG, String.format("setupGridView: ArrayIndexOutOfBoundsException: %s", e.getMessage()));
        }

        m_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, "onItemClick: selected image: " + imgURLs.get(position));
                setImage(imgURLs.get(position), m_galleryImage, m_append);
                m_SelectedImage = imgURLs.get(position);
            }
        });
    }

    private void setImage(String imgURL, ImageView image, String append)
    {
        Log.d(TAG, "setImage: setting image");
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener()
        {
            @Override
            public void onLoadingStarted(String imageUri, View view)
            {
                m_progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(
                    String imageUri, View view, FailReason failReason)
            {
                m_progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
            {
                m_progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view)
            {
                m_progressBar.setVisibility(View.GONE);
            }
        });
    }
}
