package com.it.spot.events;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class RemoveRouteEvent extends BaseEvent {

	public RemoveRouteEvent() {
		super(EventManager.EventType.MAP_LISTENER);
	}
}
