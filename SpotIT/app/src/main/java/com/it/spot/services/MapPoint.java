package com.it.spot.services;

import com.google.gson.annotations.Expose;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class MapPoint {

	// longitude
	@Expose
	private double x;

	//latitude
	@Expose
	private double y;

	public MapPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
}