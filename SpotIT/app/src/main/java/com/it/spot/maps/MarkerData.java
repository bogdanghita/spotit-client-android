package com.it.spot.maps;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.it.spot.maps.location.BasicLocation;
import com.it.spot.maps.main.LocationRouteService;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class MarkerData {

	// TODO: think of making these private
	public LocationRouteService.MarkerType markerType;
	public BasicLocation mMarkerLocation;

	public MarkerOptions mMarkerOptions;
	public Marker mMarker;

	public MarkerData() {
		markerType = LocationRouteService.MarkerType.NONE;
		mMarkerLocation = null;
		mMarker = null;
	}
}
