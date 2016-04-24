package com.it.spot.events;

import com.it.spot.services.PolygonUI;

import java.util.List;

/**
 * Created by Bogdan on 19/04/2016.
 */
public class SpotsMapEvent extends BaseEvent {

	private List<PolygonUI> polygons;

	public SpotsMapEvent(List<PolygonUI> polygons) {
		super(EventManager.EventType.MAP_LISTENER);

		this.polygons = polygons;
	}

	public List<PolygonUI> getPolygons() {
		return polygons;
	}

	public void setPolygons(List<PolygonUI> polygons) {
		this.polygons = polygons;
	}
}
