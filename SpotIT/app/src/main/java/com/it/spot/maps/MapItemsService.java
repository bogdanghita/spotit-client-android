package com.it.spot.maps;

import android.content.Context;
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
import com.it.spot.maps.directions.RouteData;
import com.it.spot.maps.location.BasicLocation;
import com.it.spot.maps.main.LocationRouteService;
import com.it.spot.maps.main.RouteUpdateResultCallbackClient;
import com.it.spot.maps.main.SavedSpot;
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

	MapItemsProvider mMapItemsProvider;

	UiController mUiController;

	public MapItemsService(Context context, MapItemsProvider mapItemsProvider, UiController uiController) {
		this.mContext = context;
		mMapItemsProvider = mapItemsProvider;

		mFileService = new FileService(context);
		mMapItemsManager = ServiceManager.getInstance().getMapItemsManager();

		mMapItemsManager.setHasDirections(false);

		mUiController = uiController;
	}

	@Override
	public void notifySetMarker(SetMarkerEvent event) {

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

		mMapItemsManager.setHasDirections(false);
		clearDirections();
		drawMarker();
	}

	@Override
	public void notifyRemoveMarker(RemoveMarkerEvent event) {

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData == null) {
			return;
		}

		if (markerData.markerType == LocationRouteService.MarkerType.SAVED_SPOT) {
			mFileService.writeSavedSpotFile(new SavedSpot(false, null), Constants.SAVED_SPOT_FILE);
		}

		markerData.markerType = LocationRouteService.MarkerType.NONE;

		mMapItemsManager.setHasDirections(false);
		clearDirections();

		markerData.mMarkerLocation = null;
		mMapItemsManager.setRouteData(null);
		clearMarker();
	}

	@Override
	public void notifyDrawRoute(DrawRouteEvent event) {

	}

	@Override
	public void notifyRemoveRoute(RemoveRouteEvent event) {

	}

	@Override
	public void notifySpotsMap(SpotsMapEvent event) {

	}

	@Override
	public void notifyLocationChange(LocationChangeEvent event) {

	}

	@Override
	public void notifyCameraChange(CameraChangeEvent event) {

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

		mMapItemsManager.setRouteData(null);
	}

	private void clearMarker() {

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData == null) {
			return;
		}

		if (markerData.mMarker != null) {
			removeMarker(markerData.mMarker);
			markerData.mMarker = null;
		}
	}

// ------------------------------------------------------------------------------------------------
// MAIN
// ------------------------------------------------------------------------------------------------

	private void openLocationInfoBar(BasicLocation location) {

		LinearLayout bottom_layout = (LinearLayout) mMapItemsProvider.getView(R.id.location_info_bar);
		View directions_fab = mMapItemsProvider.getView(R.id.directions_fab);

		bottom_layout.setTranslationY(0);
		directions_fab.setVisibility(View.VISIBLE);

		AddressAsyncTask addressAsyncTask = new AddressAsyncTask(addressResponseListener);
		addressAsyncTask.execute(location);
	}

	private void closeLocationInfoBar() {

		LinearLayout bottom_layout = (LinearLayout) mMapItemsProvider.getView(R.id.location_info_bar);
		View directions_fab = mMapItemsProvider.getView(R.id.directions_fab);

		bottom_layout.setTranslationY(bottom_layout.getHeight());
		directions_fab.setVisibility(View.INVISIBLE);

		TextView locationAddress = (TextView) mMapItemsProvider.getView(R.id.location_address);
		locationAddress.setText("");
	}

// ------------------------------------------------------------------------------------------------
// MAP ITEMS
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

		final MarkerOptions markerOptions = new MarkerOptions().position(point);

		markerData.mMarker = addMarker(markerOptions);

		LatLng position = markerOptions.getPosition();
		openLocationInfoBar(new BasicLocation(position.latitude, position.longitude));
	}

	private Marker addMarker(final MarkerOptions markerOptions) {

		final GoogleMap map = mMapItemsProvider.getMap();
		if (map == null) {
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
			eventHandler.wait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for marked to be added.");
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

		closeLocationInfoBar();
	}

	private void drawRoute(final RouteData routeData, final RouteUpdateResultCallbackClient client) {

		final GoogleMap map = mMapItemsProvider.getMap();
		if (map == null) {
			return;
		}

		switch (routeData.getRouteType()) {
			case DRIVING:

				List<Polyline> polylines = addPolylines(map, routeData.getRoutePolylineOptionsList());
				mMapItemsManager.getRouteData().setRoutePolylines(polylines);

				break;
			case WALKING:

				List<Circle> circles = addCircles(map, routeData.getRouteCircleOptionsList());
				mMapItemsManager.getRouteData().setRouteCircles(circles);

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
			eventHandler.wait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for marked to be added.");
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
			eventHandler.wait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for marked to be added.");
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
// LISTENERS
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
}
