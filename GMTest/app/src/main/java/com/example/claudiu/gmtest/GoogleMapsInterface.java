package com.example.claudiu.gmtest;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Claudiu on 19-Mar-16.
 */
public interface GoogleMapsInterface {
    public void addMarker(LatLng position);
    public void drawPolyline(PolylineOptions polylineOptions);
}
