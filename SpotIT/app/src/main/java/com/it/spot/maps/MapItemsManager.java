package com.it.spot.maps;

import com.it.spot.common.Constants;
import com.it.spot.maps.directions.RouteData;
import com.it.spot.maps.location.BasicLocation;
import com.it.spot.maps.main.LocationRouteService;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class MapItemsManager {

	// TODO: Think of thread safety for this class

	private RouteData routeData;
	private MarkerData markerData;

	private boolean hasDirections;

	private BasicLocation lastDirectionsSource, lastDirectionsDestination;

	private float zoom = Constants.DEFAULT_ZOOM;

	public MapItemsManager() {
	}

	public boolean hasDirections() {
		return hasDirections;
	}

	public boolean hasSavedSpot() {

		return markerData != null &&
				markerData.getMarkerType() == LocationRouteService.MarkerType.SAVED_SPOT;
	}

	public void setHasDirections(boolean hasDirections) {
		this.hasDirections = hasDirections;
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

	public float getZoom() {
		return zoom;
	}

	public void setZoom(float zoom) {
		this.zoom = zoom;
	}

	public BasicLocation getLastDirectionsDestination() {
		return lastDirectionsDestination;
	}

	public void setLastDirectionsDestination(BasicLocation lastDirectionsDestination) {
		this.lastDirectionsDestination = lastDirectionsDestination;
	}
}
