package com.it.spot.maps;

import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.it.spot.R;
import com.it.spot.common.Constants;
import com.it.spot.common.ServiceManager;
import com.it.spot.events.MapItemsProvider;
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
import com.it.spot.threading.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bogdan on 25/04/2016.
 */
public class RouteLogic {

	private MapItemsManager mMapItemsManager;
	private LocationManager mLocationManager;

	private MapItemsProvider mMapItemsProvider;
	private UiController mUiController;

	public RouteLogic(MapItemsProvider mapItemsProvider, UiController uiController) {

		mMapItemsProvider = mapItemsProvider;
		mUiController = uiController;

		mMapItemsManager = ServiceManager.getInstance().getMapItemsManager();
		mLocationManager = ServiceManager.getInstance().getLocationManager();
	}

	public void populateDirections() {

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData.markerType == MapItemsService.MarkerType.NONE || markerData.mMarkerLocation == null) {
			return;
		}

		BasicLocation lastLocation = mLocationManager.getLastLocation();
		if (lastLocation == null) {
			return;
		}

		// Checking if this is the same route as the last one
		if (mMapItemsManager.isRouteDisplayed() && checkSameRoute(lastLocation, markerData.mMarkerLocation)) {
			return;
		}

		// Set last directions source and destination
		mMapItemsManager.setLastDirectionsSource(lastLocation.clone());
		mMapItemsManager.setLastDirectionsDestination(markerData.mMarkerLocation.clone());

		// Get route params
		LatLng source = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
		LatLng destination = new LatLng(markerData.mMarkerLocation.getLatitude(), markerData.mMarkerLocation.getLongitude());
		float zoom = mMapItemsManager.getZoom();

		String directions_mode;
		if (markerData.markerType == MapItemsService.MarkerType.SAVED_SPOT) {
			directions_mode = Constants.MODE_WALKING;
		}
		else {
			// If anyone but Claudiu, ignore this.
			// !!!!!!!!!!!!!!!!!!!!!!!!!
			// !!!!!    DRIVING    !!!!!
			// !!!!!!!!!!!!!!!!!!!!!!!!!

			// TODO NOTE: DEBUG - Change this to walking to force walking route.
			directions_mode = Constants.MODE_DRIVING;
		}

		// Perform call to get directions
		DirectionsAsyncTask directions = new DirectionsAsyncTask(directionsListener);
		directions.execute(new RouteOptions(source, destination, directions_mode, zoom));

		// Get distance and duration
		DistanceDurationAsyncTask distanceDurationAsyncTask = new DistanceDurationAsyncTask(distanceDurationResponseListener);
		distanceDurationAsyncTask.execute(new DistanceDurationOptions(source, destination, directions_mode));
	}

	private boolean checkSameRoute(BasicLocation source, BasicLocation destination) {

		BasicLocation lastDirectionsSource = mMapItemsManager.getLastDirectionsSource();
		BasicLocation lastDirectionsDestination = mMapItemsManager.getLastDirectionsDestination();

		if (lastDirectionsSource == null || lastDirectionsDestination == null) {
			return false;
		}

		return !(source.equals(lastDirectionsSource) && destination.equals(lastDirectionsDestination));
	}

	public void updateRouteToMarker(float zoom, float oldZoom) {

		// Nothing to do for driving directions
		RouteData routeData = mMapItemsManager.getRouteData();
		if (routeData == null || routeData.getRouteType() == RouteData.RouteType.DRIVING) {
			return;
		}

		// Only redraw if the zoom has changed.
		if (oldZoom == zoom) {
			return;
		}

		// Recompute and redraw circles
		redrawRouteToMarker(zoom);
	}

	private void redrawRouteToMarker(float zoom) {

		final RouteData routeData = mMapItemsManager.getRouteData();
		if (routeData == null || routeData.getRoutePoints() == null) {
			return;
		}

		// Compute the new circles
		RecomputeRouteAsyncTask computeTask = new RecomputeRouteAsyncTask(new RedrawCallback() {
			@Override
			public void notifyRedraw(List<CircleOptions> circleOptionsList) {

				// Remove current circles
				removeRoute(routeData);

				// Set new circle options
				routeData.setRouteCircleOptionsList(circleOptionsList);

				// Draw the new circles only if the route is displayed
				if (!mMapItemsManager.isRouteDisplayed()) {
					return;
				}

				// Add new circles
				GoogleMap map = mMapItemsProvider.getMap();
				if (map != null) {
					List<Circle> circles = addCircles(map, circleOptionsList);
					mMapItemsManager.getRouteData().setRouteCircles(circles);
				}
			}
		}, zoom);

		computeTask.execute(routeData.getRoutePoints());
	}

// ------------------------------------------------------------------------------------------------
// UI ACTIONS
// ------------------------------------------------------------------------------------------------

	public void drawRoute(RouteData routeData) {

		if (routeData == null) {
			return;
		}

		GoogleMap map = mMapItemsProvider.getMap();
		if (map == null) {
			return;
		}

		switch (routeData.getRouteType()) {
			case DRIVING:

				List<PolylineOptions> polylineOptions = routeData.getRoutePolylineOptionsList();
				if (polylineOptions != null) {
					List<Polyline> polylines = addPolylines(map, polylineOptions);
					mMapItemsManager.getRouteData().setRoutePolylines(polylines);
				}

				break;
			case WALKING:

				List<CircleOptions> circleOptions = routeData.getRouteCircleOptionsList();
				if (circleOptions != null) {
					List<Circle> circles = addCircles(map, circleOptions);
					mMapItemsManager.getRouteData().setRouteCircles(circles);
				}

				break;
		}
	}

	private List<Polyline> addPolylines(final GoogleMap map, final List<PolylineOptions> polylineOptionsList) {

		final List<Polyline> polylines = new ArrayList<>();

		final Event eventHandler = new Event();

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {

				for (PolylineOptions polylineOptions : polylineOptionsList) {
					polylines.add(map.addPolyline(polylineOptions));
				}
				eventHandler.set();
			}
		});

		try {
			eventHandler.doWait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for polylines to be added.");
		}

		return polylines;
	}

	private List<Circle> addCircles(final GoogleMap map, final List<CircleOptions> circleOptionsList) {

		final List<Circle> circles = new ArrayList<>();

		final Event eventHandler = new Event();

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {

				for (CircleOptions circleOptions : circleOptionsList) {
					circles.add(map.addCircle(circleOptions));
				}
				eventHandler.set();
			}
		});

		try {
			eventHandler.doWait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for circles to be added.");
		}

		return circles;
	}

	public void removeRoute(final RouteData routeData) {

		final Event eventHandler = new Event();

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {

				switch (routeData.getRouteType()) {
					case DRIVING:
						if (routeData.getRoutePolylines() != null) {
							for (Polyline polyline : routeData.getRoutePolylines()) {
								polyline.remove();
							}
						}
						break;
					case WALKING:
						if (routeData.getRouteCircles() != null) {
							for (Circle circle : routeData.getRouteCircles()) {
								circle.remove();
							}
						}
						break;
				}

				eventHandler.set();
			}
		});

		try {
			eventHandler.doWait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for route to be removed.");
		}
	}

// ------------------------------------------------------------------------------------------------
// GOOGLE CALLBACK LISTENERS
// ------------------------------------------------------------------------------------------------

	private DirectionsResultListener directionsListener = new DirectionsResultListener() {
		@Override
		public void notifyDirectionsResponse(RouteData routeData) {
			// Set route data
			mMapItemsManager.setRouteData(routeData);

			// Draw route if the user already clicked on the directions button
			if (mMapItemsManager.isRouteDisplayed()) {
				drawRoute(routeData);
			}
		}
	};

	private DistanceDurationResponseListener distanceDurationResponseListener = new DistanceDurationResponseListener() {
		@Override
		public void notifyAddressResponse(final DistanceDurationData distanceDurationData) {

			mUiController.doRunOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView destinationTime = (TextView) mMapItemsProvider.getView(R.id.destination_time);
					destinationTime.setText(distanceDurationData.getDuration());
				}
			});
		}
	};
}
