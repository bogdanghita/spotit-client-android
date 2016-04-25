package com.it.spot.maps;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.it.spot.R;
import com.it.spot.common.Constants;
import com.it.spot.common.ServiceManager;
import com.it.spot.events.CameraChangeEvent;
import com.it.spot.events.DrawRouteEvent;
import com.it.spot.events.LocationChangeEvent;
import com.it.spot.events.MapEventListener;
import com.it.spot.events.MapItemsProvider;
import com.it.spot.events.RemoveMarkerEvent;
import com.it.spot.events.RemoveRouteEvent;
import com.it.spot.events.SetMarkerEvent;
import com.it.spot.events.SpotsMapEvent;
import com.it.spot.maps.address.AddressAsyncTask;
import com.it.spot.maps.address.AddressResponseListener;
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
import com.it.spot.maps.main.LocationRouteService;
import com.it.spot.maps.main.SavedSpot;
import com.it.spot.services.PolygonUI;
import com.it.spot.threading.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class MapItemsService extends MapEventListener {

	private Context mContext;

	private MapItemsManager mMapItemsManager;

	private FileService mFileService;

	private MapItemsProvider mMapItemsProvider;

	private UiController mUiController;

	private LocationManager mLocationManager;

	public MapItemsService(Context context, MapItemsProvider mapItemsProvider, UiController uiController) {
		this.mContext = context;
		mMapItemsProvider = mapItemsProvider;

		mFileService = new FileService(context);
		mMapItemsManager = ServiceManager.getInstance().getMapItemsManager();
		mLocationManager = ServiceManager.getInstance().getLocationManager();

		mUiController = uiController;
	}

	@Override
	public void notifySetMarker(SetMarkerEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifySetMarker()");

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData == null) {
			markerData = new MarkerData();
			mMapItemsManager.setMarkerData(markerData);
		}

		switch (event.getMarkerType()) {
			case SAVED_SPOT:

				markerData.markerType = LocationRouteService.MarkerType.SAVED_SPOT;
				markerData.mMarkerLocation = event.getLocation();

				SavedSpot spot = new SavedSpot(true, event.getLocation());
				mFileService.writeSavedSpotFile(spot, Constants.SAVED_SPOT_FILE);

				break;
			case DESTINATION:

				markerData.markerType = LocationRouteService.MarkerType.DESTINATION;
				markerData.mMarkerLocation = event.getLocation();

				break;
			default:
				Log.d(Constants.APP + Constants.EVENT, "Invalid event state in notifySetMarker()");
				return;
		}

		// Mark that the route is no displayed & set corresponding icon
		mMapItemsManager.setRouteDisplayed(false);
		setDirectionsButtonIcon(false);

		clearDirections();

		drawMarker();

		// Perform call to get directions. When result is ready, directions will be populated
		populateDirections();
	}

	@Override
	public void notifyRemoveMarker(RemoveMarkerEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifyRemoveMarker()");

		// Close location info bar first
		closeLocationInfoBar();

		// Mark that the route is not displayed
		mMapItemsManager.setRouteDisplayed(false);

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData == null) {
			return;
		}

		// Clear saved spot file
		if (markerData.markerType == LocationRouteService.MarkerType.SAVED_SPOT) {
			mFileService.writeSavedSpotFile(new SavedSpot(false, null), Constants.SAVED_SPOT_FILE);
		}

		markerData.markerType = LocationRouteService.MarkerType.NONE;

		clearDirections();

		mMapItemsManager.setRouteData(null);
		clearMarker();
	}

	@Override
	public void notifyDisplayRoute(DrawRouteEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifyDisplayRoute()");

		// Mark that we have displayed the route & set corresponding icon
		mMapItemsManager.setRouteDisplayed(true);
		setDirectionsButtonIcon(true);

		RouteData routeData = mMapItemsManager.getRouteData();
		if (routeData != null) {
			drawRoute(routeData);
		}
	}

	@Override
	public void notifyHideRoute(RemoveRouteEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifyHideRoute()");

		// Mark that the route is not displayed & set corresponding icon
		mMapItemsManager.setRouteDisplayed(false);
		setDirectionsButtonIcon(false);

		RouteData routeData = mMapItemsManager.getRouteData();
		if (routeData == null) {
			return;
		}

		if (routeData.isDrawn()) {
			removeRoute(routeData);
		}
	}

	@Override
	public void notifySpotsMap(SpotsMapEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifySpotsMap()");

		clearSpots();

		drawSpots(event.getPolygons());
	}

	@Override
	public void notifyLocationChange(LocationChangeEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifyLocationChange()");
	}

	@Override
	public void notifyCameraChange(CameraChangeEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifyCameraChange()");

		float zoom = event.getZoom();
		float oldZoom = mMapItemsManager.getZoom();

		// Update zoom
		mMapItemsManager.setZoom(zoom);

		// Update route to marker
		updateRouteToMarker(zoom, oldZoom);
	}

	// TODO: move this from here
	private void updateRouteToMarker(float zoom, float oldZoom) {

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

	// TODO: move this from here
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
// L R SERVICE
// ------------------------------------------------------------------------------------------------

	private void clearDirections() {

		RouteData routeData = mMapItemsManager.getRouteData();
		if (routeData == null) {
			return;
		}

		if (routeData.isDrawn()) {
			removeRoute(routeData);
		}

		routeData.clearRoute();
		mMapItemsManager.setRouteData(null);
	}

	private void clearMarker() {

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData == null) {
			return;
		}

		if (markerData.mMarker != null) {
			removeMarker(markerData.mMarker);
		}

		markerData.mMarker = null;
		markerData.mMarkerLocation = null;
		markerData.mMarkerOptions = null;
	}

// ------------------------------------------------------------------------------------------------
// GUI ITEMS
// ------------------------------------------------------------------------------------------------

	private void openLocationInfoBar(final BasicLocation location) {

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {

				// Clear items first (useful when location info bar was not closed)
				clearLocationInfoBarItems();

				LinearLayout bottom_layout = (LinearLayout) mMapItemsProvider.getView(R.id.location_info_bar);
				View directions_fab = mMapItemsProvider.getView(R.id.directions_fab);

				bottom_layout.setTranslationY(0);
				directions_fab.setVisibility(View.VISIBLE);

				AddressAsyncTask addressAsyncTask = new AddressAsyncTask(addressResponseListener);
				addressAsyncTask.execute(location);

				// Set appropriate title
				setLocationInfoBarTitle();
			}
		});
	}

	private void closeLocationInfoBar() {

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {

				LinearLayout bottom_layout = (LinearLayout) mMapItemsProvider.getView(R.id.location_info_bar);
				View directions_fab = mMapItemsProvider.getView(R.id.directions_fab);

				bottom_layout.setTranslationY(bottom_layout.getHeight());
				directions_fab.setVisibility(View.INVISIBLE);

				clearLocationInfoBarItems();
			}
		});
	}

	private void setLocationInfoBarTitle() {

		final String text;

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData == null) {
			return;
		}

		if (markerData.markerType == LocationRouteService.MarkerType.DESTINATION) {
			text = mContext.getResources().getString(R.string.location_info_bar_title_destination);
		}
		else if (markerData.markerType == LocationRouteService.MarkerType.SAVED_SPOT) {
			text = mContext.getString(R.string.location_info_bar_title_saved_spot);
		}
		else {
			text = "";
		}

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView tv = (TextView) mMapItemsProvider.getView(R.id.location_title);
				tv.setText(text);
			}
		});
	}

	private void clearLocationInfoBarItems() {

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {

				TextView locationTitle = (TextView) mMapItemsProvider.getView(R.id.location_title);
				locationTitle.setText("");

				TextView locationAddress = (TextView) mMapItemsProvider.getView(R.id.location_address);
				locationAddress.setText("");

				TextView destinationTime = (TextView) mMapItemsProvider.getView(R.id.destination_time);
				destinationTime.setText("");
			}
		});
	}

	private void setDirectionsButtonIcon(boolean iconClosed) {

		final int icon_id;

		MarkerData markerData = mMapItemsManager.getMarkerData();

		if (iconClosed) {
			icon_id = R.drawable.ic_close_white_24dp;
		}
		else if (markerData == null) {
			return;
		}
		else if (markerData.markerType == LocationRouteService.MarkerType.DESTINATION) {
			icon_id = R.drawable.ic_directions_car_white_24dp;
		}
		else if (markerData.markerType == LocationRouteService.MarkerType.SAVED_SPOT) {
			icon_id = R.drawable.ic_directions_walk_white_24dp;
		}
		else {
			return;
		}

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {
				FloatingActionButton fab = (FloatingActionButton) mMapItemsProvider.getView(R.id.directions_fab);
				fab.setImageDrawable(mContext.getResources().getDrawable(icon_id));
			}
		});
	}

// ------------------------------------------------------------------------------------------------
// SPOT ITEMS
// ------------------------------------------------------------------------------------------------

	private void clearSpots() {

		final GoogleMap map = mMapItemsProvider.getMap();
		if (map == null) {
			return;
		}

		// Clear all map items
		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {
				map.clear();
			}
		});

		// Redraw marker
		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData != null && markerData.mMarkerOptions != null) {
			markerData.mMarker = addMarker(markerData.mMarkerOptions);
		}

		// Draw route only if it is displayed
		if (!mMapItemsManager.isRouteDisplayed()) {
			return;
		}

		// Redraw route
		RouteData routeData = mMapItemsManager.getRouteData();
		if (routeData != null && routeData.isDrawn()) {
			drawRoute(routeData);
		}
	}

	private void drawSpots(List<PolygonUI> polygons) {

		GoogleMap map = mMapItemsProvider.getMap();
		if (map == null) {
			return;
		}

		for (PolygonUI polygon : polygons) {
			drawPolygon(map, polygon.getPoints(), polygon.getColor());
		}
	}

	private void drawPolygon(GoogleMap map, Iterable<LatLng> points, int color) {

		String text = "";
		for (LatLng point : points) {
			text += point.toString() + ", ";
		}
		Log.d(Constants.APP + Constants.DRAW, text);

		map.addPolygon(new PolygonOptions()
				.addAll(points)
				.strokeColor(color)
				.strokeWidth(0)
				.fillColor(color));
	}

// ------------------------------------------------------------------------------------------------
// MARKER
// ------------------------------------------------------------------------------------------------

	private void drawMarker() {

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData == null) {
			return;
		}

		if (markerData.mMarker != null) {
			removeMarker(markerData.mMarker);
		}

		if (markerData.mMarkerLocation == null) {
			return;
		}

		LatLng point = new LatLng(markerData.mMarkerLocation.getLatitude(), markerData.mMarkerLocation.getLongitude());

		// Open location info bar only if marker data is OK
		openLocationInfoBar(new BasicLocation(point.latitude, point.longitude));

		// Draw marker
		MarkerOptions markerOptions = new MarkerOptions().position(point);
		markerData.mMarkerOptions = markerOptions;
		markerData.mMarker = addMarker(markerOptions);
	}

	private Marker addMarker(final MarkerOptions markerOptions) {

		final GoogleMap map = mMapItemsProvider.getMap();
		if (map == null || markerOptions == null) {
			return null;
		}

		final Marker[] marker = new Marker[1];
		final Event eventHandler = new Event();

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {

				marker[0] = map.addMarker(markerOptions);
				eventHandler.set();
			}
		});

		try {
			eventHandler.doWait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for marker to be added.");
		}

		return marker[0];
	}

	private void removeMarker(final Marker marker) {

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {
				marker.remove();
			}
		});
	}

// ------------------------------------------------------------------------------------------------
// ROUTE
// ------------------------------------------------------------------------------------------------

	// <THIS>
	private void populateDirections() {

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData.markerType == LocationRouteService.MarkerType.NONE || markerData.mMarkerLocation == null) {
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
		if (markerData.markerType == LocationRouteService.MarkerType.SAVED_SPOT) {
			directions_mode = Constants.MODE_WALKING;
		}
		else {
			// If anyone but Claudiu, ignore this.
			// !!!!!!!!!!!!!!!!!!!!!!!!!
			// !!!!!    DRIVING    !!!!!
			// !!!!!!!!!!!!!!!!!!!!!!!!!

			// TODO NOTE: DEBUG - Change this to walking to force walking route.
			directions_mode = Constants.MODE_WALKING;
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
	// </THIS>

	private void drawRoute(RouteData routeData) {

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
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for marker to be added.");
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
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for marker to be added.");
		}

		return circles;
	}

	private void removeRoute(final RouteData routeData) {

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {
				switch (routeData.getRouteType()) {
					case DRIVING:
						for (Polyline polyline : routeData.getRoutePolylines()) {
							polyline.remove();
						}
						break;
					case WALKING:
						for (Circle circle : routeData.getRouteCircles()) {
							circle.remove();
						}
						break;
				}
			}
		});
	}

// ------------------------------------------------------------------------------------------------
// GOOGLE CALLBACK LISTENERS
// ------------------------------------------------------------------------------------------------

	private AddressResponseListener addressResponseListener = new AddressResponseListener() {
		@Override
		public void notifyAddressResponse(final String address) {

			mUiController.doRunOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView locationAddress = (TextView) mMapItemsProvider.getView(R.id.location_address);
					locationAddress.setText(address);
				}
			});
		}
	};

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
