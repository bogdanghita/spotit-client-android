package com.it.spot.events;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class CameraChangeEvent extends BaseEvent {

	private float zoom;

	public CameraChangeEvent(float zoom) {
		super(EventManager.EventType.MAP_LISTENER);

		this.zoom = zoom;
	}

	public float getZoom() {
		return zoom;
	}

	public void setZoom(float zoom) {
		this.zoom = zoom;
	}
}
