package com.example.claudiu.gmtest;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMapsInterface {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        Thread thread = new Thread(new DrawRoute(this));
//        thread.start();

        LatLng sourcePosition = new LatLng(44, 26);
        LatLng destPosition = new LatLng(52, 13);

        // Draw a marker.
        addMarker(sourcePosition);

        // Get directions from source to destination as PolylineOptions.
        GoogleMapsDirections directions = new GoogleMapsDirections();

        directions.execute(new RouteOptions(sourcePosition, destPosition, GoogleMapsDirections.MODE_WALKING));

        PolylineOptions rectLine = null;
        try {
            rectLine = directions.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Draw directions.
        drawPolyline(rectLine);
    }

    @Override
    public void addMarker(final LatLng position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.addMarker(new MarkerOptions().position(position).title("Marker in ???"));
            }
        });
    }

    @Override
    public void drawPolyline(final PolylineOptions polylineOptions) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Polyline polyline = mMap.addPolyline(polylineOptions);
            }
        });
    }
}
