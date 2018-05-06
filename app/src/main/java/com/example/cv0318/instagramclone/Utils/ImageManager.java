package com.example.cv0318.instagramclone.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageManager
{
    private static final String TAG = String.format("%s_TAG", ImageManager.class.getSimpleName());
    
    public static Bitmap getBitmap(String imgUrl)
    {
        File imageFile = new File(imgUrl);
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try
        {
            fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        }
        catch (FileNotFoundException e)
        {
            Log.e(TAG, String.format("getBitmap: FileNotFoundException: %s", e.getMessage()));
        }
        finally
        {
            try
            {
                fis.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, String.format("getBitmap: IOException: %s", e.getMessage()));
            }
        }
        return bitmap;
    }
    
    /**
     * return byte array from bitmap.
     * quality is between 0 and 100
     * @param bm
     * @param quality
     * @return
     */
    public static byte[] getBytesFromBitmap(Bitmap bm, int quality)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, quality, stream);
        return stream.toByteArray();
    }
}
