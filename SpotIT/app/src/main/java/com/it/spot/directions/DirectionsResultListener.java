package com.it.spot.directions;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Bogdan on 20/03/2016.
 */
public interface DirectionsResultListener {

	void notifyDirectionsResponse(RouteData routeData);
}
