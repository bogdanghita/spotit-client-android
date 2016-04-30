package com.it.spot.maps.main;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.it.spot.common.Constants;
import com.it.spot.common.Utils;
import com.it.spot.maps.location.BasicLocation;
import com.it.spot.services.ServerAPI;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class MapUpdateService {

	private LatLngBounds cameraBounds = null;

	private ScheduledExecutorService timer;

	private ServerAPI serverAPI;

	private final Object syncObject = new Object();

	public MapUpdateService(MapUpdateCallbackClient uiClient) {

		serverAPI = new ServerAPI(uiClient);
	}

	public void setCameraPosition(LatLngBounds bounds) {

		this.cameraBounds = bounds;
	}

	public void startMapStatusUpdateLoop() {

		synchronized (syncObject) {

			if (timer != null) {
				timer.shutdown();
			}

			timer = Executors.newSingleThreadScheduledExecutor();
			timer.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					// Requesting map status update
					requestMapStatusUpdate();
				}
			}, 0, Constants.MAP_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
		}
	}

	public void stopMapStatusUpdateLoop() {

		synchronized (syncObject) {

			if (timer != null) {
				timer.shutdown();
				timer = null;
			}
		}
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

	public void sendMapStatus(BasicLocation location, String status) {

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
