package com.it.spot.maps;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
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
import com.it.spot.maps.directions.RecomputeRouteAsyncTask;
import com.it.spot.maps.directions.RedrawCallback;
import com.it.spot.maps.directions.RouteData;
import com.it.spot.maps.main.LocationRouteService;
import com.it.spot.maps.main.SavedSpot;
import com.it.spot.maps.report.MarkerLogic;
import com.it.spot.maps.report.UiItemsController;
import com.it.spot.services.PolygonUI;

import java.util.List;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class MapItemsService extends MapEventListener {

	private MapItemsManager mMapItemsManager;
	private FileService mFileService;

	private MapItemsProvider mMapItemsProvider;
	private UiController mUiController;

	private UiItemsController mUiItemsController;

	private RouteLogic mRouteLogic;
	private MarkerLogic mMarkerLogic;

	public MapItemsService(Context context, MapItemsProvider mapItemsProvider, UiController uiController) {

		mMapItemsProvider = mapItemsProvider;

		mFileService = new FileService(context);
		mMapItemsManager = ServiceManager.getInstance().getMapItemsManager();

		mUiController = uiController;

		mUiItemsController = new UiItemsController(context, mapItemsProvider, uiController);

		mRouteLogic = new RouteLogic(mapItemsProvider, uiController);
		mMarkerLogic = new MarkerLogic(mapItemsProvider, uiController, mUiItemsController);
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
		mUiItemsController.setDirectionsButtonIcon(false);

		clearDirections();

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
				mRouteLogic.removeRoute(routeData);

				// Set new circle options
				routeData.setRouteCircleOptionsList(circleOptionsList);

				// Draw the new circles only if the route is displayed
				if (!mMapItemsManager.isRouteDisplayed()) {
					return;
				}

				// Add new circles
				GoogleMap map = mMapItemsProvider.getMap();
				if (map != null) {
					List<Circle> circles = mRouteLogic.addCircles(map, circleOptionsList);
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
			mRouteLogic.removeRoute(routeData);
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
			mMarkerLogic.removeMarker(markerData.mMarker);
		}

		markerData.mMarker = null;
		markerData.mMarkerLocation = null;
		markerData.mMarkerOptions = null;
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
			markerData.mMarker = mMarkerLogic.addMarker(markerData.mMarkerOptions);
		}

		// Draw route only if it is displayed
		if (!mMapItemsManager.isRouteDisplayed()) {
			return;
		}

		// Redraw route
		RouteData routeData = mMapItemsManager.getRouteData();
		if (routeData != null && routeData.isDrawn()) {
			mRouteLogic.drawRoute(routeData);
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
}
