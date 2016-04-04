package com.it.spot.maps;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;

/**
 * Created by Bogdan on 01/04/2016.
 */
public interface RouteUpdateResultCallbackClient {

	void notifyDrivingResult(List<Polyline> polylineList);

	void notifyWalkingResult(List<Circle> circleList);

	void notifyMarkerResult(Marker marker);
}
