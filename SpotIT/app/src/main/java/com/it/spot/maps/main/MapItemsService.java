package com.it.spot.maps.main;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
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
import com.it.spot.services.FileService;
import com.it.spot.maps.directions.RouteData;
import com.it.spot.services.PolygonUI;
import com.it.spot.threading.Event;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class MapItemsService extends MapEventListener {

	public enum MarkerType {SAVED_SPOT, DESTINATION, NONE}

	private MapItemsManager mMapItemsManager;
	private FileService mFileService;

	private UiItemsController mUiItemsController;

	private RouteLogic mRouteLogic;
	private MarkerLogic mMarkerLogic;
	private SpotsLogic mSpotsLogic;

	public MapItemsService(Context context, MapItemsProvider mapItemsProvider, UiController uiController) {

		mFileService = new FileService(context);
		mMapItemsManager = ServiceManager.getInstance().getMapItemsManager();

		mUiItemsController = new UiItemsController(context, mapItemsProvider, uiController);

		mRouteLogic = new RouteLogic(mapItemsProvider, uiController);
		mMarkerLogic = new MarkerLogic(mapItemsProvider, uiController, mUiItemsController);
		mSpotsLogic = new SpotsLogic(mapItemsProvider, uiController);
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

		mMarkerLogic.drawMarker();

		// Perform call to get directions. When result is ready, directions will be populated
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
