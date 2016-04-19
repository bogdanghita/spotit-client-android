package com.it.spot.common;

import com.it.spot.identity.IdentityManager;
import com.it.spot.maps.location.LocationManager;

/**
 * Created by Bogdan on 19/03/2016.
 */
public class ServiceManager {

	private static ServiceManager instance = null;

	private IdentityManager identityManager;

	private LocationManager locationManager;

	private ServiceManager() {
		identityManager = new IdentityManager();
		locationManager = new LocationManager();
	}

	public synchronized static ServiceManager getInstance() {
		if (instance == null) {
			instance = new ServiceManager();
		}
		return instance;
	}

	public IdentityManager getIdentityManager() {
		return identityManager;
	}

	public LocationManager getLocationManager() {
		return locationManager;
	}

	public void clear() {
		identityManager.clear();
		locationManager.clear();
	}
}
