package com.it.spot.maps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.it.spot.directions.RouteData;

/**
 * Created by Bogdan on 01/04/2016.
 */
public interface RouteUpdateCallbackClient {

	void drawRoute(RouteData routeData, RouteUpdateResultCallbackClient client);

	void removeRoute(RouteData routeData);

	void drawMarker(MarkerOptions markerOptions, RouteUpdateResultCallbackClient client);

	void removeMarker(Marker marker);
}
