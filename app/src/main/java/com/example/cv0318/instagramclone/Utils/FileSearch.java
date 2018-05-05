package com.example.cv0318.instagramclone.Utils;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

public class FileSearch
{
    /**
     * search directory and return list of all directories contained inside
     *
     * @param directory
     *
     * @return
     */
    public static ArrayList<String> getDirectoryPaths(String directory)
    {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for (int i = 0; i < listFiles.length; i++)
        {
            if (listFiles[i].isDirectory())
            {
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

    /**
     * search directory and return list of all files contained inside
     *
     * @param directory
     *
     * @return
     */
    public static ArrayList<String> getFilePaths(String directory)
    {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        if (listFiles != null)
        {
            for (int i = 0; i < listFiles.length; i++)
            {
                if (listFiles[i].isFile())
                {
                    pathArray.add(listFiles[i].getAbsolutePath());
                }
            }
        }
        return pathArray;
    }

    /**
     * Getting All Images Path
     *
     * @param activity
     * @return ArrayList with images Path
     */
    public static ArrayList<String> getAllShownImagesPath(Activity activity)
    {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null,
            null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext())
        {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }
}
