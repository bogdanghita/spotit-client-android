package com.it.spot.maps.location;

import com.it.spot.maps.location.BasicLocation;

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
