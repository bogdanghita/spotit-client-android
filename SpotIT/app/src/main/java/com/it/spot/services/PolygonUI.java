package com.it.spot.services;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class PolygonUI {

	private List<LatLng> points;

	private int color;

	public PolygonUI(List<LatLng> points, int color) {
		this.points = points;
		this.color = color;
	}

	public List<LatLng> getPoints() {
		return points;
	}

	public void setPoints(List<LatLng> points) {
		this.points = points;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}
