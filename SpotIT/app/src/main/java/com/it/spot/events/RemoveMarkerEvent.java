package com.it.spot.events;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class RemoveMarkerEvent extends  BaseEvent {

	public RemoveMarkerEvent() {
		super(EventManager.EventType.MAP_LISTENER);
	}
}
