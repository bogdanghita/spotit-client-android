package com.it.spot.maps.main;

import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.it.spot.R;
import com.it.spot.common.Constants;
import com.it.spot.common.ServiceManager;
import com.it.spot.events.MapItemsProvider;
import com.it.spot.maps.address.AddressResponseListener;
import com.it.spot.maps.location.BasicLocation;
import com.it.spot.threading.Event;

/**
 * Created by Bogdan on 25/04/2016.
 */
public class MarkerLogic {

	private MapItemsManager mMapItemsManager;

	private MapItemsProvider mMapItemsProvider;
	private UiController mUiController;

	private UiItemsController mUiItemsController;

	public MarkerLogic(MapItemsProvider mapItemsProvider, UiController uiController, UiItemsController uiItemsController) {

		mMapItemsProvider = mapItemsProvider;
		mUiController = uiController;
		mUiItemsController = uiItemsController;

		mMapItemsManager = ServiceManager.getInstance().getMapItemsManager();
	}

	public void drawMarker() {

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
		BasicLocation location = new BasicLocation(point.latitude, point.longitude);
		mUiItemsController.openLocationInfoBar(location, addressResponseListener);

		// Draw marker
		MarkerOptions markerOptions = new MarkerOptions().position(point);
		markerData.mMarkerOptions = markerOptions;
		markerData.mMarker = addMarker(markerOptions);
	}

	public Marker addMarker(final MarkerOptions markerOptions) {

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

	public void removeMarker(final Marker marker) {

		final Event eventHandler = new Event();

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {
				marker.remove();
				eventHandler.set();
			}
		});

		try {
			eventHandler.doWait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for marker to be removed.");
		}
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
}
