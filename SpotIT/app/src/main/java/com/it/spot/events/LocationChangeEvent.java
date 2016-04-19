package com.it.spot.events;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class LocationChangeEvent extends BaseEvent {

	public LocationChangeEvent() {
		super(EventManager.EventType.MAP_LISTENER);
	}
}
