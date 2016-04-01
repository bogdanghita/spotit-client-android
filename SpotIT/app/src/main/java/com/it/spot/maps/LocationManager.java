package com.it.spot.maps;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Bogdan on 31/03/2016.
 */
public class LocationManager {

	private BasicLocation mLastLocation;

	public synchronized void setLastLocation(BasicLocation mLastLocation) {
		this.mLastLocation = mLastLocation;
	}

	public synchronized BasicLocation getLastLocation() {
		if (mLastLocation != null) {
			return mLastLocation.clone();
		}
		else {
			return null;
		}
	}

	public synchronized void clear() {
		mLastLocation = null;
	}
}
