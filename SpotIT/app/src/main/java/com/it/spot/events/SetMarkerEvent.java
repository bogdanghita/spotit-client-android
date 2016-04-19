package com.it.spot.events;

import com.it.spot.maps.location.BasicLocation;
import com.it.spot.maps.main.LocationRouteService;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class SetMarkerEvent extends BaseEvent {

	private BasicLocation location;
	private LocationRouteService.MarkerType markerType;

	public SetMarkerEvent(BasicLocation location, LocationRouteService.MarkerType markerType) {
		super(EventManager.EventType.MAP_LISTENER);

		this.location = location;
		this.markerType = markerType;
	}

	public BasicLocation getLocation() {
		return location;
	}

	public LocationRouteService.MarkerType getMarkerType() {
		return markerType;
	}
}
