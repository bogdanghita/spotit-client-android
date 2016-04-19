package com.it.spot.events;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class SpotsMapEvent extends BaseEvent {

	public SpotsMapEvent() {
		super(EventManager.EventType.MAP_LISTENER);
	}
}
