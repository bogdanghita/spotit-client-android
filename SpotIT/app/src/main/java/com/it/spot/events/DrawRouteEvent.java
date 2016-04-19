package com.it.spot.events;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class DrawRouteEvent extends BaseEvent {

	public DrawRouteEvent() {
		super(EventManager.EventType.MAP_LISTENER);
	}
}
