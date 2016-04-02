package com.it.spot.maps;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.it.spot.common.Constants;
import com.it.spot.common.SavedSpot;
import com.it.spot.common.ServiceManager;
import com.it.spot.directions.DirectionsAsyncTask;
import com.it.spot.directions.DirectionsListener;
import com.it.spot.directions.RouteOptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Bogdan on 31/03/2016.
 */
public class LocationRouteService {

	private enum MarkerType {SAVED_SPOT, DESTINATION, NONE}

	private MarkerType markerType;
	private boolean hasDirectionsPolyline;

	private BasicLocation mMarkerLocation = null;
	private Marker mMarker = null;
	private PolylineOptions mDirectionsPolylineOptions = null;
	private Polyline mDirectionsPolyline = null;

	private LocationManager mLocationManager;

	private RouteUpdateCallbackClient mRouteUpdateClient;

	private Context mContext;

	public LocationRouteService(Context context, RouteUpdateCallbackClient routeUpdateClient) {

		this.mContext = context;
		this.mRouteUpdateClient = routeUpdateClient;

		mLocationManager = ServiceManager.getInstance().getLocationManager();
		markerType = MarkerType.NONE;
		hasDirectionsPolyline = false;
	}

// -------------------------------------------------------------------------------------------------
// SAVED SPOT ACTIONS
// -------------------------------------------------------------------------------------------------

	private void updateMarkerMapState() {

		switch (markerType) {
			case NONE:

				mMarkerLocation = null;
				mDirectionsPolylineOptions = null;
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

		if (mMarker != null) {
			Object eventWait = new Object();
			mRouteUpdateClient.removeMarker(mMarker, eventWait);
//			try {
//				eventWait.wait();
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//				// TODO: ...
//			}
			mMarker = null;
		}

		hasDirectionsPolyline = false;
		if (mDirectionsPolyline != null) {
			Object eventWait = new Object();
			mRouteUpdateClient.removeRoute(mDirectionsPolyline, eventWait);
//			try {
//				eventWait.wait();
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//				// TODO: ...
//			}
			mDirectionsPolyline = null;
		}
	}

	public void saveSpot() {

		BasicLocation lastLocation = mLocationManager.getLastLocation();
		if (lastLocation == null) {
			return;
		}

		markerType = MarkerType.SAVED_SPOT;
		mMarkerLocation = lastLocation;

		SavedSpot spot = new SavedSpot(true, lastLocation);
		writeSavedSpotFile(spot, Constants.SAVED_SPOT_FILE);

		updateMarkerMapState();
	}

	public void leaveSpot() {

		writeSavedSpotFile(new SavedSpot(false, null), Constants.SAVED_SPOT_FILE);

		markerType = MarkerType.NONE;

		updateMarkerMapState();
	}

	public void setDestination(LatLng latLng) {

		if (markerType == MarkerType.SAVED_SPOT) {
			return;
		}

		markerType = MarkerType.DESTINATION;
		mMarkerLocation = new BasicLocation(latLng.latitude, latLng.longitude);

		updateMarkerMapState();
	}

	public boolean isSpotSaved() {

		return markerType == MarkerType.SAVED_SPOT;
	}

	public void loadSavedSpot() {

		SavedSpot savedSpot = readSavedSpotFile(Constants.SAVED_SPOT_FILE);

		if (savedSpot != null && savedSpot.hasSavedSpot) {
			markerType = MarkerType.SAVED_SPOT;
			mMarkerLocation = savedSpot.location;
		}

		updateMarkerMapState();
	}

	private void writeSavedSpotFile(SavedSpot spot, String filename) {

		try {
			FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE);

			Gson gson = new Gson();
			String jsonString = gson.toJson(spot);

			fos.write(jsonString.getBytes());

			fos.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.SAVED_SPOT, "Error writing saved spot to file: " + filename);
		}
	}

	private SavedSpot readSavedSpotFile(String filename) {

		SavedSpot result = null;

		try {
			FileInputStream fis = mContext.openFileInput(filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

			Gson gson = new Gson();
			result = gson.fromJson(reader, SavedSpot.class);

			reader.close();
			fis.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.SAVED_SPOT, "Error reading saved spot from file: " + filename);
		}

		return result;
	}

// -------------------------------------------------------------------------------------------------
// MAP ITEMS ACTIONS
// -------------------------------------------------------------------------------------------------

	public void notifyMapCleared() {

		updateMarkerMapState();
		drawDirectionsPolyline();
	}

	public void drawMarker() {

		if (mMarker != null) {
			Object eventWait = new Object();
			mRouteUpdateClient.removeMarker(mMarker, eventWait);
//			try {
//				eventWait.wait();
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//				// TODO: ...
//			}
		}

		if (mMarkerLocation == null) {
			return;
		}

		LatLng point = new LatLng(mMarkerLocation.getLatitude(), mMarkerLocation.getLongitude());

		MarkerOptions markerOptions = new MarkerOptions().position(point);
		mRouteUpdateClient.drawMarker(markerOptions, locationRouteUpdateClient);
	}

	public void drawRouteToMarker() {

		String directions_mode;
		if (markerType == MarkerType.NONE || mMarkerLocation == null) {
			return;
		}
		if (markerType == MarkerType.SAVED_SPOT) {
			directions_mode = Constants.MODE_WALKING;
		}
		else {
			directions_mode = Constants.MODE_DRIVING;
		}

		BasicLocation lastLocation = mLocationManager.getLastLocation();
		if (lastLocation == null) {
			return;
		}

		DirectionsAsyncTask directions = new DirectionsAsyncTask(directionsListener);

		LatLng source = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
		LatLng destination = new LatLng(mMarkerLocation.getLatitude(), mMarkerLocation.getLongitude());

		directions.execute(new RouteOptions(source, destination, directions_mode));
	}

	private void drawDirectionsPolyline() {

		if (mDirectionsPolyline != null) {
			Object eventWait = new Object();
			mRouteUpdateClient.removeRoute(mDirectionsPolyline, eventWait);
//			try {
//				eventWait.wait();
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//				// TODO: ...
//			}
		}

		if (mDirectionsPolylineOptions == null) {
			return;
		}

		hasDirectionsPolyline = true;
		mRouteUpdateClient.drawRoute(mDirectionsPolylineOptions, locationRouteUpdateClient);
	}

	private void clearDirectionsPolyline() {

		hasDirectionsPolyline = false;
		Object eventWait = new Object();
		mRouteUpdateClient.removeRoute(mDirectionsPolyline, eventWait);
//		try {
//			eventWait.wait();
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//			// TODO: ...
//		}

		mDirectionsPolyline = null;
		mDirectionsPolylineOptions = null;
	}

// -------------------------------------------------------------------------------------------------
// RESULT CALLBACKS
// -------------------------------------------------------------------------------------------------

	private DirectionsListener directionsListener = new DirectionsListener() {
		@Override
		public void notifyDirectionsResponse(PolylineOptions polylineOptions) {
			mDirectionsPolylineOptions = polylineOptions;
			drawDirectionsPolyline();
		}
	};

	private RouteUpdateResultCallbackClient locationRouteUpdateClient = new RouteUpdateResultCallbackClient() {
		@Override
		public void notifyPolylineResult(Polyline polyline) {
			mDirectionsPolyline = polyline;
		}

		@Override
		public void notifyMarkerResult(Marker marker) {
			mMarker = marker;
		}
	};
}
