package com.it.spot.maps.main;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.it.spot.common.Constants;
import com.it.spot.events.MapItemsProvider;
import com.it.spot.services.PolygonUI;
import com.it.spot.threading.Event;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bogdan on 26/04/2016.
 */
public class SpotsLogic {

	private MapItemsProvider mMapItemsProvider;
	private UiController mUiController;

	private List<Polygon> spotsList;

	public SpotsLogic(MapItemsProvider mapItemsProvider, UiController uiController) {

		mMapItemsProvider = mapItemsProvider;
		mUiController = uiController;

		spotsList = new LinkedList<>();
	}

//	public void removeSpots() {
//
//		final GoogleMap map = mMapItemsProvider.getMap();
//		if (map == null) {
//			return;
//		}
//
//		// Clear all map items
//		mUiController.doRunOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				map.clear();
//			}
//		});
//
//		// Redraw marker
//		MarkerData markerData = mMapItemsManager.getMarkerData();
//		if (markerData != null && markerData.mMarkerOptions != null) {
//			markerData.mMarker = mMarkerLogic.addMarker(markerData.mMarkerOptions);
//		}
//
//		// Draw route only if it is displayed
//		if (!mMapItemsManager.isRouteDisplayed()) {
//			return;
//		}
//
//		// Redraw route
//		RouteData routeData = mMapItemsManager.getRouteData();
//		if (routeData != null && routeData.isDrawn()) {
//			mRouteLogic.drawRoute(routeData);
//		}
//	}

	public void removeSpots() {

		final Event eventHandler = new Event();

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (Polygon polygon : spotsList) {
					polygon.remove();
				}
				eventHandler.set();
			}
		});

		try {
			eventHandler.doWait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for spots to be removed.");
		}

		spotsList.clear();
	}

	public void drawSpots(final List<PolygonUI> polygons) {

		final GoogleMap map = mMapItemsProvider.getMap();
		if (map == null) {
			return;
		}

		final Event eventHandler = new Event();

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (PolygonUI polygonUI : polygons) {
					Polygon polygon = drawPolygon(map, polygonUI.getPoints(), polygonUI.getColor());

					spotsList.add(polygon);
				}
				eventHandler.set();
			}
		});

		try {
			eventHandler.doWait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for spots to be added.");
		}
	}

	private Polygon drawPolygon(GoogleMap map, Iterable<LatLng> points, int color) {

		String text = "";
		for (LatLng point : points) {
			text += point.toString() + ", ";
		}
		Log.d(Constants.APP + Constants.DRAW, text);

		return map.addPolygon(new PolygonOptions()
				.addAll(points)
				.strokeColor(color)
				.strokeWidth(0)
				.fillColor(color));
	}
}
