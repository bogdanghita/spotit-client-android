package com.it.spot.maps;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.it.spot.common.Constants;
import com.it.spot.common.Utils;
import com.it.spot.threading.TaskScheduler;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class MapUpdateService {

	private LatLngBounds cameraBounds = null;

	TaskScheduler taskScheduler;

	ServerAPI serverAPI;

	public MapUpdateService(MapUpdateCallbackClient uiClient) {

		taskScheduler = new TaskScheduler();

		serverAPI = new ServerAPI(uiClient);
	}

	public void setCameraPosition(LatLngBounds bounds) {

		this.cameraBounds = bounds;
	}

	public void startMapStatusUpdateLoop() {

		taskScheduler.start(new Runnable() {
			@Override
			public void run() {

				// Requesting map status update
				requestMapStatusUpdate();
			}
		}, Constants.MAP_UPDATE_INTERVAL);
	}

	public void stopMapStatusUpdateLoop() {

		taskScheduler.stop();
	}

	public void requestMapStatusUpdate() {

		// Camera bounds not set yet
		if (cameraBounds == null) {
			return;
		}

		Log.d(Constants.APP + Constants.MAP_UPDATE, "Requesting map status update...");

		double radius = Utils.computeRadius(cameraBounds);

		// Executing request
		serverAPI.getMapStatus(cameraBounds.getCenter(), radius);
	}

	public void sendMapStatus(Location location, String status) {

		int intStatus;
		switch (status) {
			case Constants.STATUS_GREEN_TEXT:
				intStatus = Constants.STATUS_GREEN;
				break;
			case Constants.STATUS_YELLOW_TEXT:
				intStatus = Constants.STATUS_YELLOW;
				break;
			case Constants.STATUS_RED_TEXT:
				intStatus = Constants.STATUS_RED;
				break;
			default:
				intStatus = Constants.STATUS_ERROR;
		}

		// Sending request
		serverAPI.postStatus(new LatLng(location.getLatitude(), location.getLongitude()), intStatus);
	}
}
