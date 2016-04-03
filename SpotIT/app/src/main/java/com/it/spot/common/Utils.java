package com.it.spot.common;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class Utils {

    public static double computeRadius(LatLngBounds cameraBounds) {

        return 0.01;
    }

    public static LatLng calculateDerivedPosition(LatLng source, double range, double bearing) {
        double latA = Math.toRadians(source.latitude);
        double lonA = Math.toRadians(source.longitude);
        double angularDistance = range / Constants.EARTH_RADIUS_M;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                        Math.cos(latA) * Math.sin(angularDistance) * Math.cos(trueCourse));

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        return new LatLng(
                Math.toDegrees(lat),
                Math.toDegrees(lon));
    }

    public static double angleFromCoordinates(LatLng source, LatLng destination) {
        double lat1 = source.latitude;
        double long1 = source.longitude;
        double lat2 = destination.latitude;
        double long2 = destination.longitude;
        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        //brng = 360 - brng;

        return brng;
    }

    public static LatLng calculateDerivedPosition(LatLng source, LatLng destination, double range) {
        double bearing = angleFromCoordinates(source, destination);
        return calculateDerivedPosition(source, range, bearing);
    }
}
