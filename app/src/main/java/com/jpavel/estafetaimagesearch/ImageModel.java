package com.jpavel.estafetaimagesearch;

import android.location.Location;
import android.net.Uri;

import java.util.Arrays;
import java.util.List;

public class ImageModel {
    public String title;
//    public String url;
    public Uri uri;
    public long timestamp;
    public Location location;
    public String filePath;


    public ImageModel(String title, Uri uri) {
        this.title = title;
        this.uri = uri;
    }

    public ImageModel(String title, Uri uri, long timestamp, Location loc) {
        this.title = title;
        this.uri = uri;
        this.timestamp = timestamp;
        this.location = loc;
    }

}
