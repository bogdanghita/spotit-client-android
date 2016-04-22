package com.it.spot.maps;

import com.google.android.gms.maps.model.Marker;
import com.it.spot.maps.location.BasicLocation;
import com.it.spot.maps.main.LocationRouteService;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class MarkerData {

	// TODO: think of making these private
	public LocationRouteService.MarkerType markerType;
	public BasicLocation mMarkerLocation;
	public Marker mMarker;

	public MarkerData() {
		markerType = LocationRouteService.MarkerType.NONE;
		mMarkerLocation = null;
		mMarker = null;
	}

	public Marker getmMarker() {
		return mMarker;
	}

	public void setmMarker(Marker mMarker) {
		this.mMarker = mMarker;
	}

	public LocationRouteService.MarkerType getMarkerType() {
		return markerType;
	}

	public void setMarkerType(LocationRouteService.MarkerType markerType) {
		this.markerType = markerType;
	}

	public BasicLocation getmMarkerLocation() {
		return mMarkerLocation;
	}

	public void setmMarkerLocation(BasicLocation mMarkerLocation) {
		this.mMarkerLocation = mMarkerLocation;
	}
}
