package com.it.spot.maps.main;

import android.content.Context;
import android.util.Log;
import android.view.View;

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
import com.it.spot.maps.directions.RouteData;
import com.it.spot.services.FileService;
import com.it.spot.threading.Event;
import com.it.spot.threading.StateMonitorListener;
import com.it.spot.threading.StateMonitorThread;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class MapItemsService extends MapEventListener implements StateMonitorListener {

	public enum MarkerType {SAVED_SPOT, DESTINATION, NONE}

	private MapItemsManager mMapItemsManager;
	private FileService mFileService;

	private MapItemsProvider mMapItemsProvider;
	private UiController mUiController;
	private UiItemsController mUiItemsController;

	private RouteLogic mRouteLogic;
	private MarkerLogic mMarkerLogic;
	private SpotsLogic mSpotsLogic;

	private Event mAddressEvent;
	private Event mDirectionsEvent;
	private Event mDurationEvent;

	public MapItemsService(Context context, MapItemsProvider mapItemsProvider, UiController uiController) {

		mFileService = new FileService(context);
		mMapItemsManager = ServiceManager.getInstance().getMapItemsManager();

		mMapItemsProvider = mapItemsProvider;
		mUiController = uiController;
		mUiItemsController = new UiItemsController(context, mapItemsProvider, uiController);

		mRouteLogic = new RouteLogic(mapItemsProvider, uiController);
		mMarkerLogic = new MarkerLogic(mapItemsProvider, uiController, mUiItemsController);
		mSpotsLogic = new SpotsLogic(mapItemsProvider, uiController);
	}

	void startLoading() {
		mUiController.doRunOnUiThread(
				new Runnable() {
					@Override
					public void run() {
						// Loading destination info animation.
						mMapItemsProvider.getView(R.id.location_address).setVisibility(View.GONE);
						mMapItemsProvider.getView(R.id.destination_time).setVisibility(View.GONE);
						mMapItemsProvider.getView(R.id.loading_address).setVisibility(View.VISIBLE);
					}
				}
		);
	}

	void stopLoading() {
		mUiController.doRunOnUiThread(
				new Runnable() {
					@Override
					public void run() {
						// Stop loading destination info animation.
						mMapItemsProvider.getView(R.id.loading_address).setVisibility(View.GONE);
						mMapItemsProvider.getView(R.id.destination_time).setVisibility(View.VISIBLE);
						mMapItemsProvider.getView(R.id.location_address).setVisibility(View.VISIBLE);
					}
				}
		);
	}

	void startStateMonitor() {
		mAddressEvent = new Event();
		mDirectionsEvent = new Event();
		mDurationEvent = new Event();
		List<Event> eventList = new LinkedList<>();
		eventList.add(mAddressEvent);
		eventList.add(mDirectionsEvent);
		eventList.add(mDurationEvent);
		new StateMonitorThread(this, eventList).start();
	}

	@Override
	public void notifyStateReady() {
		stopLoading();
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

				markerData.markerType = MarkerType.SAVED_SPOT;
				markerData.mMarkerLocation = event.getLocation();

				SavedSpot spot = new SavedSpot(true, event.getLocation());
				mFileService.writeSavedSpotFile(spot, Constants.SAVED_SPOT_FILE);

				break;
			case DESTINATION:

				markerData.markerType = MarkerType.DESTINATION;
				markerData.mMarkerLocation = event.getLocation();

				break;
			default:
				Log.d(Constants.APP + Constants.EVENT, "Invalid event state in notifySetMarker()");
				return;
		}

		// Mark that the route is no displayed & set corresponding icon
		mMapItemsManager.setRouteDisplayed(false);
		mUiItemsController.setDirectionsButtonIcon(false);

		mRouteLogic.clearDirections();

		// Start the loading animation.
		startLoading();
		// StateMonitor that awaits address, duration and directions.
		startStateMonitor();

		// Draws marker and obtains destination address.
		mMarkerLogic.setAddressEvent(mAddressEvent);
		mMarkerLogic.drawMarker();

		// Perform call to get directions and duration.
		// When result is ready, directions will be populated.
		mRouteLogic.setDirectionsEvent(mDirectionsEvent);
		mRouteLogic.setDurationEvent(mDurationEvent);
		mRouteLogic.populateDirections();
	}

	@Override
	public void notifyRemoveMarker(RemoveMarkerEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifyRemoveMarker()");

		// Close location info bar first
		mUiItemsController.closeLocationInfoBar();

		// Mark that the route is not displayed
		mMapItemsManager.setRouteDisplayed(false);

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData == null) {
			return;
		}

		// Clear saved spot file
		if (markerData.markerType == MarkerType.SAVED_SPOT) {
			mFileService.writeSavedSpotFile(new SavedSpot(false, null), Constants.SAVED_SPOT_FILE);
		}

		markerData.markerType = MarkerType.NONE;

		mRouteLogic.clearDirections();

		mMapItemsManager.setRouteData(null);
		mMarkerLogic.clearMarker();
	}

	@Override
	public void notifyDisplayRoute(DrawRouteEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifyDisplayRoute()");

		// Mark that we have displayed the route & set corresponding icon
		mMapItemsManager.setRouteDisplayed(true);
		mUiItemsController.setDirectionsButtonIcon(true);

		RouteData routeData = mMapItemsManager.getRouteData();
		if (routeData != null) {
			mRouteLogic.drawRoute(routeData);
		}
	}

	@Override
	public void notifyHideRoute(RemoveRouteEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifyHideRoute()");

		// Mark that the route is not displayed & set corresponding icon
		mMapItemsManager.setRouteDisplayed(false);
		mUiItemsController.setDirectionsButtonIcon(false);

		RouteData routeData = mMapItemsManager.getRouteData();
		if (routeData == null) {
			return;
		}

		if (routeData.isDrawn()) {
			mRouteLogic.removeRoute(routeData);
		}
	}

	@Override
	public void notifySpotsMap(SpotsMapEvent event) {

		Log.d(Constants.EVENT + Constants.ITEMS, "notifySpotsMap()");

		mSpotsLogic.removeSpots();

		mSpotsLogic.drawSpots(event.getPolygons());
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
		mRouteLogic.updateRouteToMarker(zoom, oldZoom);
	}
}
