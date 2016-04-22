package com.it.spot.common;

import com.it.spot.events.EventManager;
import com.it.spot.identity.IdentityManager;
import com.it.spot.maps.MapItemsManager;
import com.it.spot.maps.location.LocationManager;

/**
 * Created by Bogdan on 19/03/2016.
 */
public class ServiceManager {

	private static ServiceManager instance = null;

	private IdentityManager identityManager;

	private LocationManager locationManager;

	private EventManager eventManager;

	private MapItemsManager mapItemsManager;

	private ServiceManager() {
		identityManager = new IdentityManager();
		locationManager = new LocationManager();
		eventManager = new EventManager();
		mapItemsManager = new MapItemsManager();
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

	public EventManager getEventManager() {
		return eventManager;
	}

	public MapItemsManager getMapItemsManager() {
		return mapItemsManager;
	}

	public void clear() {
		identityManager.clear();
		locationManager.clear();
		eventManager.clear();
	}
}
