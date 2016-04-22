package com.it.spot.maps;

import com.it.spot.maps.directions.RouteData;
import com.it.spot.maps.main.LocationRouteService;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class MapItemsManager {

	// TODO: Think of thread safety for this class

	private RouteData mRouteData;
	private MarkerData mMarkerData;
	private boolean hasDirections;

	public MapItemsManager() {
	}

	public boolean hasDirections() {
		return hasDirections;
	}

	public boolean hasSavedSpot() {

		return mMarkerData != null &&
				mMarkerData.getMarkerType() == LocationRouteService.MarkerType.SAVED_SPOT;
	}

	public void setHasDirections(boolean hasDirections) {
		this.hasDirections = hasDirections;
	}

	public RouteData getRouteData() {
		return mRouteData;
	}

	public void setRouteData(RouteData mRouteData) {
		this.mRouteData = mRouteData;
	}

	public MarkerData getMarkerData() {
		return mMarkerData;
	}

	public void setMarkerData(MarkerData mMarkerData) {
		this.mMarkerData = mMarkerData;
	}

}
