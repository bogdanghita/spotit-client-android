package com.it.spot.maps.main;

import android.content.Context;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.it.spot.common.Constants;
import com.it.spot.common.ServiceManager;
import com.it.spot.events.EventManager;
import com.it.spot.events.RemoveMarkerEvent;
import com.it.spot.events.SetMarkerEvent;
import com.it.spot.maps.FileService;
import com.it.spot.maps.MarkerData;
import com.it.spot.maps.directions.DirectionsAsyncTask;
import com.it.spot.maps.directions.DirectionsResultListener;
import com.it.spot.maps.directions.RecomputeRouteAsyncTask;
import com.it.spot.maps.directions.RedrawCallback;
import com.it.spot.maps.directions.RouteData;
import com.it.spot.maps.directions.RouteOptions;
import com.it.spot.maps.distance_duration.DistanceDurationAsyncTask;
import com.it.spot.maps.distance_duration.DistanceDurationData;
import com.it.spot.maps.distance_duration.DistanceDurationOptions;
import com.it.spot.maps.distance_duration.DistanceDurationResponseListener;
import com.it.spot.maps.location.BasicLocation;
import com.it.spot.maps.location.LocationManager;

import java.util.List;

/**
 * Created by Bogdan on 31/03/2016.
 */
public class LocationRouteService {

	private EventManager mEventManager;

	public enum MarkerType {SAVED_SPOT, DESTINATION, NONE}

	private MarkerData mMarkerData = null;

	private boolean hasDirections;

	private RouteData mRouteData = null;
	private float mOldZoom = Constants.DEFAULT_ZOOM;
	private float mZoom = Constants.DEFAULT_ZOOM;
	private BasicLocation lastDirectionsSource, lastDirectionsDestination;

	private LocationManager mLocationManager;

	private RouteUpdateCallbackClient mRouteUpdateClient;

	private Context mContext;

	public LocationRouteService(Context context, RouteUpdateCallbackClient routeUpdateClient) {

		this.mContext = context;
		this.mRouteUpdateClient = routeUpdateClient;

		mEventManager = ServiceManager.getInstance().getEventManager();

		mLocationManager = ServiceManager.getInstance().getLocationManager();
		mMarkerData = new MarkerData();
		hasDirections = false;
	}

// -------------------------------------------------------------------------------------------------
// SAVED SPOT & DESTINATION ACTIONS
// -------------------------------------------------------------------------------------------------

	public boolean hasDirections() {
		return hasDirections;
	}

	public MarkerType getMarkerType() {
		return mMarkerData.markerType;
	}

	private void updateMarkerMapState() {

		switch (mMarkerData.markerType) {
			case NONE:

				mMarkerData.mMarkerLocation = null;
				mRouteData = null;
				clearMapItems();

				break;
			case SAVED_SPOT:

				clearMapItems();
				drawMarker();

				break;
			case DESTINATION:

				clearMapItems();
				drawMarker();

				break;
		}
	}

	private void clearMapItems() {

		if (mMarkerData.mMarker != null) {
			mRouteUpdateClient.removeMarker(mMarkerData.mMarker);
			mMarkerData.mMarker = null;
		}
	}

	public void saveSpot() {

		BasicLocation lastLocation = mLocationManager.getLastLocation();
		if (lastLocation == null) {
			return;
		}

		mMarkerData.markerType = MarkerType.SAVED_SPOT;
		mMarkerData.mMarkerLocation = lastLocation;

		SavedSpot spot = new SavedSpot(true, lastLocation);
		new FileService(mContext).writeSavedSpotFile(spot, Constants.SAVED_SPOT_FILE);

		hasDirections = false;
		clearDirections();

		updateMarkerMapState();
	}

	public void leaveSpot() {

		clearSavedSpotFile();

		mMarkerData.markerType = MarkerType.NONE;

		hasDirections = false;
		clearDirections();

		updateMarkerMapState();
	}

	public void clearSavedSpotFile() {

		new FileService(mContext).writeSavedSpotFile(new SavedSpot(false, null), Constants.SAVED_SPOT_FILE);
	}

	// Always keep the last 2 values of the zoom in case the user just moves around the map without zooming.
	public void setZoom(float zoom) {
		mOldZoom = mZoom;
		mZoom = zoom;
	}

	public void setDestination(LatLng latLng) {

		if (mMarkerData.markerType == MarkerType.SAVED_SPOT) {
			return;
		}

		mMarkerData.markerType = MarkerType.DESTINATION;
		mMarkerData.mMarkerLocation = new BasicLocation(latLng.latitude, latLng.longitude);

		hasDirections = false;
		clearDirections();

		updateMarkerMapState();
	}

//	public void removeDestination() {
//		if (markerType != MarkerType.DESTINATION) {
//			return;
//		}
//
//		markerType = MarkerType.NONE;
//
//		isRouteDisplayed = false;
//		clearDirections();
//
//		notifySetMarker();
//	}

	public void removeDestination() {

		if (mMarkerData.markerType != MarkerType.DESTINATION) {
			return;
		}

		mEventManager.triggerEvent(new RemoveMarkerEvent());

		// TODO
	}

	public boolean isSpotSaved() {

		return mMarkerData.markerType == MarkerType.SAVED_SPOT;
	}

//	public void loadSavedSpot() {
//
//		SavedSpot savedSpot = readSavedSpotFile(Constants.SAVED_SPOT_FILE);
//
//		if (savedSpot != null && savedSpot.hasSavedSpot) {
//			markerType = MarkerType.SAVED_SPOT;
//			mMarkerLocation = savedSpot.location;
//		}
//
//		updateMarkerMapState();
//	}

	public void loadSavedSpot() {

		SavedSpot savedSpot = new FileService(mContext).readSavedSpotFile(Constants.SAVED_SPOT_FILE);

		if (savedSpot != null && savedSpot.hasSavedSpot) {
			mEventManager.triggerEvent(new SetMarkerEvent(savedSpot.location, MarkerType.SAVED_SPOT));
		}

		// TODO
	}

// -------------------------------------------------------------------------------------------------
// MAP ITEMS ACTIONS
// -------------------------------------------------------------------------------------------------

	public void notifyMapCleared() {

		mMarkerData.mMarker = null;
		if (mRouteData != null)
			mRouteData.clearRoute();

		updateMarkerMapState();
		drawDirections();
	}

	public void drawMarker() {

		if (mMarkerData.mMarker != null) {
			mRouteUpdateClient.removeMarker(mMarkerData.mMarker);
		}

		if (mMarkerData.mMarkerLocation == null) {
			return;
		}

		LatLng point = new LatLng(mMarkerData.mMarkerLocation.getLatitude(), mMarkerData.mMarkerLocation.getLongitude());

		MarkerOptions markerOptions = new MarkerOptions().position(point);
		mRouteUpdateClient.drawMarker(markerOptions, locationRouteUpdateClient);
	}

	public void redrawRouteToMarker() {
		// Only redraw if the zoom has changed.
		if (mRouteData == null || mOldZoom == mZoom)
			return;
		new RecomputeRouteAsyncTask(mRedrawCallback, mZoom).execute(mRouteData.getRoutePoints());
	}

	public void drawRouteToMarker() {

		String directions_mode;
		if (mMarkerData.markerType == MarkerType.NONE || mMarkerData.mMarkerLocation == null) {
			return;
		}
		if (mMarkerData.markerType == MarkerType.SAVED_SPOT) {
			directions_mode = Constants.MODE_WALKING;
		}
		else {
			// If anyone but Claudiu, ignore this.

			// !!!!!!!!!!!!!!!!!!!!!!!!!
			// !!!!!    DRIVING    !!!!!
			// !!!!!!!!!!!!!!!!!!!!!!!!!

			// !!!!!!!!!!!!!!!!!!!!!!!!!
			// !!!!!    DRIVING    !!!!!
			// !!!!!!!!!!!!!!!!!!!!!!!!!

			// !!!!!!!!!!!!!!!!!!!!!!!!!
			// !!!!!    DRIVING    !!!!!
			// !!!!!!!!!!!!!!!!!!!!!!!!!

			// Change this to walking to force walking route.
			directions_mode = Constants.MODE_DRIVING;
		}

		BasicLocation lastLocation = mLocationManager.getLastLocation();
		if (lastLocation == null) {
			return;
		}

		// Not drawing the same route again
		if (hasDirections && checkSameRoute(lastLocation, mMarkerData.mMarkerLocation)) {
			return;
		}
		lastDirectionsSource = lastLocation.clone();
		lastDirectionsDestination = mMarkerData.mMarkerLocation.clone();

		// Get route
		hasDirections = true;
		LatLng source = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
		LatLng destination = new LatLng(mMarkerData.mMarkerLocation.getLatitude(), mMarkerData.mMarkerLocation.getLongitude());
		DirectionsAsyncTask directions = new DirectionsAsyncTask(directionsResultListener);
		directions.execute(new RouteOptions(source, destination, directions_mode, mZoom));

		// DEBUG / EXAMPLE
		// TODO: REMOVE!
		// Get distance and duration
		DistanceDurationOptions distanceDurationOptions = new DistanceDurationOptions(source, destination, directions_mode);
		DistanceDurationAsyncTask distanceDurationAsyncTask = new DistanceDurationAsyncTask(distanceDurationResponseListener);
		distanceDurationAsyncTask.execute(distanceDurationOptions);
	}

	public void removeRouteToMarker() {

		hasDirections = false;
		clearDirections();
	}

	private boolean checkSameRoute(BasicLocation source, BasicLocation destination) {

		if (lastDirectionsSource == null || lastDirectionsDestination == null) {
			return false;
		}

		return !(source.equals(lastDirectionsSource) && destination.equals(lastDirectionsDestination));
	}

	private void drawDirections() {

		if (mRouteData != null && mRouteData.isDrawn()) {
			mRouteUpdateClient.removeRoute(mRouteData);
		}

		if (mRouteData == null) {
			return;
		}
		mRouteUpdateClient.drawRoute(mRouteData, locationRouteUpdateClient);
	}

	private void clearDirections() {

		if (mRouteData != null && mRouteData.isDrawn()) {
			mRouteUpdateClient.removeRoute(mRouteData);
		}

		if (mRouteData != null)
			mRouteData.clearRoute();
		mRouteData = null;
	}

// -------------------------------------------------------------------------------------------------
// RESULT CALLBACKS
// -------------------------------------------------------------------------------------------------

	private DirectionsResultListener directionsResultListener = new DirectionsResultListener() {
		@Override
		public void notifyDirectionsResponse(RouteData routeData) {
			mRouteData = routeData;
			drawDirections();
		}
	};

	private RedrawCallback mRedrawCallback = new RedrawCallback() {
		@Override
		public void notifyRedraw(List<CircleOptions> circleOptionsList) {
			RouteData backup = mRouteData;
			clearDirections();
			mRouteData = backup;
			mRouteData.setRouteCircleOptionsList(circleOptionsList);
			drawDirections();
		}
	};

	private RouteUpdateResultCallbackClient locationRouteUpdateClient = new RouteUpdateResultCallbackClient() {
		@Override
		public void notifyDrivingResult(List<Polyline> polylineList) {
			mRouteData.setRoutePolylines(polylineList);
		}

		@Override
		public void notifyWalkingResult(List<Circle> circleList) {
			mRouteData.setRouteCircles(circleList);
		}

		@Override
		public void notifyMarkerResult(Marker marker) {
			mMarkerData.mMarker = marker;
	}
	};

	// DEBUG / EXAMPLE
	// TODO: REMOVE!
	private DistanceDurationResponseListener distanceDurationResponseListener = new DistanceDurationResponseListener() {
		@Override
		public void notifyAddressResponse(DistanceDurationData distanceDurationData) {
			String duration = distanceDurationData.getDuration();

			// Do stuff with "duration" variable.
		}
	};
}
