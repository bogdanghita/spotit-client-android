package com.it.spot.events;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class BaseEvent {

	EventManager.EventType type;

	protected BaseEvent(EventManager.EventType type) {

		this.type = type;
	}

	public EventManager.EventType getType() {

		return type;
	}
}