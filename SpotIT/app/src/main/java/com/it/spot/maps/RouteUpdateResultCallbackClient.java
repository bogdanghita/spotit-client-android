package com.it.spot.maps;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

/**
 * Created by Bogdan on 01/04/2016.
 */
public interface RouteUpdateResultCallbackClient {

	void notifyPolylineResult(Polyline polyline);

	void notifyMarkerResult(Marker marker);
}
