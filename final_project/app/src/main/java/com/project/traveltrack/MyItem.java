package com.project.traveltrack;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    public final Bitmap mbitmap;
    public final String myItemImgPath;


    public MyItem(double lat, double lng,Bitmap bitmap,String imgPath) {
        mbitmap = bitmap;
        mPosition = new LatLng(lat, lng);
        myItemImgPath = imgPath;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

}