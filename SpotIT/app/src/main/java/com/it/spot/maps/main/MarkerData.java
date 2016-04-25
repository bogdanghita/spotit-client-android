package com.it.spot.maps.main;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.it.spot.maps.location.BasicLocation;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class MarkerData {

	// TODO: think of making these private
	public MapItemsService.MarkerType markerType;
	public BasicLocation mMarkerLocation;

	public MarkerOptions mMarkerOptions;
	public Marker mMarker;

	public MarkerData() {
		markerType = MapItemsService.MarkerType.NONE;
		mMarkerLocation = null;
		mMarker = null;
	}
}
