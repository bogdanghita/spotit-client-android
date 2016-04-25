package com.it.spot.events;

import com.it.spot.maps.main.MapItemsService;
import com.it.spot.maps.location.BasicLocation;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class SetMarkerEvent extends BaseEvent {

	private BasicLocation location;
	private MapItemsService.MarkerType markerType;

	public SetMarkerEvent(BasicLocation location, MapItemsService.MarkerType markerType) {
		super(EventManager.EventType.MAP_LISTENER);

		this.location = location;
		this.markerType = markerType;
	}

	public BasicLocation getLocation() {
		return location;
	}

	public MapItemsService.MarkerType getMarkerType() {
		return markerType;
	}
}
