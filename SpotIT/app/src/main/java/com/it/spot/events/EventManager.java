package com.it.spot.events;

import com.google.common.collect.HashMultimap;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class EventManager {

	public enum EventType {
		MAP_LISTENER
	}

	HashMultimap<EventType, EventListener> listeners;

	public EventManager() {

		listeners = HashMultimap.create();
	}

	public void subscribe(MapEventListener l) {
		listeners.put(EventType.MAP_LISTENER, l);
	}

	public void unsubscribe(MapEventListener l) {
		listeners.remove(EventType.MAP_LISTENER, l);
	}

	public void triggerEvent(BaseEvent event) {

		for (EventListener l : listeners.get(event.getType())) {
			l.notify(event);
		}
	}

	public void clear() {

		listeners.clear();
	}
}
