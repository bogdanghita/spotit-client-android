package com.it.spot.maps;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
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
public class RouteService {

	private BasicLocation mSavedSpot = null;
	private Marker mSavedMarker = null;
	private PolylineOptions mDirectionsPolylineOptions = null;
	private Polyline mDirectionsPolyline = null;

	private LatLng destinationSpot = null;
	private Marker destinationMarker = null;

	private LocationManager mLocationManager;

	private RouteUpdateCallbackClient mRouteUpdateClient;

	private Context mContext;

	public RouteService(Context context, RouteUpdateCallbackClient routeUpdateClient) {

		this.mContext = context;
		this.mRouteUpdateClient = routeUpdateClient;

		mLocationManager = ServiceManager.getInstance().getLocationManager();
	}

// -------------------------------------------------------------------------------------------------
// SAVED SPOT ACTIONS
// -------------------------------------------------------------------------------------------------

	public void saveSpot() {

		if (destinationSpot != null) {
			destinationMarker.remove();
			destinationMarker = null;
			destinationSpot = null;
		}
		if (mDirectionsPolylineOptions != null) {
			mDirectionsPolyline.remove();
			mDirectionsPolyline = null;
			mDirectionsPolylineOptions = null;
		}

		BasicLocation lastLocation = mLocationManager.getLastLocation();
		if(lastLocation == null) {
			return;
		}

		SavedSpot spot = new SavedSpot(true, lastLocation);

		writeSavedSpotFile(spot, Constants.SAVED_SPOT_FILE);

		mSavedSpot = spot.location;
		drawSavedSpot();
	}

	public void leaveSpot() {

		writeSavedSpotFile(new SavedSpot(false, null), Constants.SAVED_SPOT_FILE);

		mSavedSpot = null;
		if (mSavedMarker != null) {
			mSavedMarker.remove();
			mSavedMarker = null;
		}

		mDirectionsPolylineOptions = null;
		if (mDirectionsPolyline != null) {
			mDirectionsPolyline.remove();
			mDirectionsPolyline = null;
		}
	}

	public void setDestination(LatLng latLng) {

		if (mSavedSpot != null) {
			return;
		}

		destinationSpot = latLng;
		drawDestinationSpot();
	}

	public boolean isSpotSaved() {

		return mSavedSpot != null;
	}

	public void loadSavedSpot() {

		SavedSpot savedSpot = readSavedSpotFile(Constants.SAVED_SPOT_FILE);

		if (savedSpot != null && savedSpot.hasSavedSpot) {
			mSavedSpot = savedSpot.location;
		}
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

		drawSavedSpot();
		drawDestinationSpot();
	}

	public void drawSavedSpot() {

		if (mSavedMarker != null) {
			mRouteUpdateClient.removeMarker(mSavedMarker);
		}

		if (mSavedSpot == null) {
			return;
		}

		LatLng point = new LatLng(mSavedSpot.getLatitude(), mSavedSpot.getLongitude());

		MarkerOptions markerOptions = new MarkerOptions().position(point);
		mRouteUpdateClient.drawMarker(markerOptions, savedSpotClient);
	}

	private void drawDestinationSpot() {

		if (destinationMarker != null) {
			mRouteUpdateClient.removeMarker(destinationMarker);
		}

		if (destinationSpot == null) {
			return;
		}

		if (mDirectionsPolyline != null) {
			mDirectionsPolyline.remove();
			mDirectionsPolyline = null;
			mDirectionsPolylineOptions = null;
		}

		MarkerOptions markerOptions = new MarkerOptions().position(destinationSpot);
		mRouteUpdateClient.drawMarker(markerOptions, destinationClient);
	}

	private void drawRouteToSavedSpotButton() {

		if (mSavedSpot == null) {
			return;
		}

		BasicLocation lastLocation = mLocationManager.getLastLocation();
		if(lastLocation == null) {
			return;
		}

		// Get directions from source to destination as PolylineOptions.
		DirectionsAsyncTask directions = new DirectionsAsyncTask(directionsListener);

		LatLng source = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
		LatLng destination = new LatLng(mSavedSpot.getLatitude(), mSavedSpot.getLongitude());

		directions.execute(new RouteOptions(source, destination, Constants.MODE_WALKING));
	}

	private void drawRouteToDestinationSpot(LatLng position) {

		BasicLocation lastLocation = mLocationManager.getLastLocation();
		if(lastLocation == null) {
			return;
		}

		// Get directions from source to destination as PolylineOptions.
		DirectionsAsyncTask directions = new DirectionsAsyncTask(directionsListener);

		LatLng source = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

		directions.execute(new RouteOptions(source, position, Constants.MODE_DRIVING));
	}

	private void drawDirectionsPolyline() {

		if (mDirectionsPolyline != null) {
//			mDirectionsPolyline.remove();
			mRouteUpdateClient.removeRoute(mDirectionsPolyline);
		}

		if (mDirectionsPolylineOptions == null) {
			return;
		}

//		mDirectionsPolyline = mMap.addPolyline(mDirectionsPolylineOptions);
		// TODO: Saved spot client is used here. Solve this
		mRouteUpdateClient.drawRoute(mDirectionsPolylineOptions, savedSpotClient);
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

	private RouteUpdateResultCallbackClient savedSpotClient = new RouteUpdateResultCallbackClient() {
		@Override
		public void notifyPolylineResult(Polyline polyline) {
			// TODO: ...
			mDirectionsPolyline = polyline;
		}

		@Override
		public void notifyMarkerResult(Marker marker) {
			mSavedMarker = marker;
		}
	};

	private RouteUpdateResultCallbackClient destinationClient = new RouteUpdateResultCallbackClient() {
		@Override
		public void notifyPolylineResult(Polyline polyline) {
			// TODO: ...
			mDirectionsPolyline = polyline;
		}

		@Override
		public void notifyMarkerResult(Marker marker) {
			destinationMarker = marker;
		}
	};
}
