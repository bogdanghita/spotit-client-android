package com.it.spot.maps.main;

import com.it.spot.common.Constants;
import com.it.spot.maps.directions.RouteData;
import com.it.spot.maps.location.BasicLocation;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class MapItemsManager {

	// TODO: Think of thread safety for this class

	private RouteData routeData;
	private MarkerData markerData;

	private boolean routeDisplayed;

	private BasicLocation lastDirectionsSource, lastDirectionsDestination;

	private float zoom = Constants.DEFAULT_ZOOM;

	public MapItemsManager() {
		routeDisplayed = false;
	}

	public boolean isRouteDisplayed() {
		return routeDisplayed;
	}

	public boolean hasSavedSpot() {

		return markerData != null &&
				markerData.markerType == MapItemsService.MarkerType.SAVED_SPOT;
	}

	public void setRouteDisplayed(boolean routeDisplayed) {
		this.routeDisplayed = routeDisplayed;
	}

	public RouteData getRouteData() {
		return routeData;
	}

	public void setRouteData(RouteData mRouteData) {
		this.routeData = mRouteData;
	}

	public MarkerData getMarkerData() {
		return markerData;
	}

	public void setMarkerData(MarkerData mMarkerData) {
		this.markerData = mMarkerData;
	}

	public BasicLocation getLastDirectionsSource() {
		return lastDirectionsSource;
	}

	public void setLastDirectionsSource(BasicLocation lastDirectionsSource) {
		this.lastDirectionsSource = lastDirectionsSource;
	}

	public void setZoom(float zoom) {
		this.zoom = zoom;
	}

	public float getZoom() {
		return zoom;
	}

	public BasicLocation getLastDirectionsDestination() {
		return lastDirectionsDestination;
	}

	public void setLastDirectionsDestination(BasicLocation lastDirectionsDestination) {
		this.lastDirectionsDestination = lastDirectionsDestination;
	}
}
