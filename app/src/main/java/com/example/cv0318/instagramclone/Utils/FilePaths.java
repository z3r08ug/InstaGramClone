package com.example.cv0318.instagramclone.Utils;

import android.os.Environment;

public class FilePaths
{
    //"storage/emulated/0"
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    //"storage/0000-0000"
    public String SD_ROOT_DIR = "/storage/0000-0000";

    public String ZEDGE = String.format("%s/zedge/wallpaper", ROOT_DIR);
    public String DOWNLOAD = String.format("%s/Download", ROOT_DIR);
    public String FACEBOOK = String.format("%s/DCIM/Facebook", ROOT_DIR);
    public String CAMERA = String.format("%s/DCIM/camera", ROOT_DIR);
    public String PICTURES = String.format("%s/Pictures", ROOT_DIR);

    public String SD_CAMERA = String.format("%s/DCIM/Camera", SD_ROOT_DIR);
}
