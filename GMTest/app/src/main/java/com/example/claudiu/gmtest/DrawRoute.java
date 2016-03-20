package com.example.claudiu.gmtest;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;

/**
 * Created by Claudiu on 19-Mar-16.
 */
public class DrawRoute implements Runnable {

    private GoogleMapsInterface mMap;
    private LatLng sourcePosition;
    private LatLng destPosition;

    // TODO: Extend constructor with source and dest
    public DrawRoute (GoogleMapsInterface googleMapsInterface){
        mMap = googleMapsInterface;
    }

    @Override
    public void run() {

        try {
            GoogleMapsDirections md = new GoogleMapsDirections();
            //mMap = ((SupportMapFragment) getSupportFragmentManager()
            //        .findFragmentById(R.id.map)).getMap();
            LatLng sourcePosition = new LatLng(44, 26);
            LatLng destPosition = new LatLng(52, 13);

            mMap.addMarker(sourcePosition);

            PolylineOptions rectLine = md.getDirections(sourcePosition, destPosition,
                    GoogleMapsDirections.MODE_DRIVING);

            mMap.drawPolyline(rectLine);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
