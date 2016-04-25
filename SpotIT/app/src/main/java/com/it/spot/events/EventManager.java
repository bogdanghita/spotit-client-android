package com.it.spot.events;

import com.google.common.collect.HashMultimap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class EventManager {

	// TODO: make this thread safe

	public enum EventType {
		MAP_LISTENER
	}

	HashMultimap<EventType, EventListener> listeners;

	ExecutorService executorService = null;

	public EventManager() {

		listeners = HashMultimap.create();
	}

	public void subscribe(MapEventListener l) {
		listeners.put(EventType.MAP_LISTENER, l);
	}

	public void unsubscribe(MapEventListener l) {
		listeners.remove(EventType.MAP_LISTENER, l);
	}

	public void startExecutorService() {

		// newCachedThreadPool() is suitable for applications that launch many short-lived tasks
		// See: https://docs.oracle.com/javase/tutorial/essential/concurrency/pools.html
		executorService = Executors.newCachedThreadPool();
//		executorService = Executors.newSingleThreadExecutor();
	}

	public void stopExecutorService() {

		if (executorService == null) {
			return;
		}

		executorService.shutdown();
		executorService = null;
	}

	/**
	 * Triggers the event on a separate thread
	 *
	 * @param event
	 */
	public void triggerEvent(final BaseEvent event) {

		if (executorService == null || executorService.isTerminated()) {
			return;
		}

		executorService.execute(new Runnable() {
			@Override
			public void run() {

				for (EventListener l : listeners.get(event.getType())) {
					l.notify(event);
				}
			}
		});
	}

	/**
	 * Unsubscribes all listeners and stops executor service
	 */
	public void clear() {

		listeners.clear();

		stopExecutorService();
	}
}
