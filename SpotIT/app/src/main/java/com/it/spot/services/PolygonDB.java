package com.it.spot.services;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.it.spot.common.Constants;
import com.it.spot.maps.MapPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class PolygonDB {

	@Expose
	private List<MapPoint> points;

	@Expose
	private int color;

	public PolygonDB(List<MapPoint> points, int color) {
		this.points = points;
		this.color = color;
	}

	public List<MapPoint> getPoints() {
		return points;
	}

	public void setPoints(List<MapPoint> points) {
		this.points = points;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public PolygonUI toPolygonUI() {

		List<LatLng> points = new ArrayList<>();

		for (MapPoint point : this.points) {
			points.add(new LatLng(point.getY(), point.getX()));
		}

		int convertedColor;

		switch (color) {
			case Constants.STATUS_GREEN:
				convertedColor = Constants.COLOR_GREEN;
				break;
			case Constants.STATUS_RED:
				convertedColor = Constants.COLOR_RED;
				break;
			case Constants.STATUS_YELLOW:
				convertedColor = Constants.COLOR_YELLOW;
				break;
			default:
				convertedColor = Constants.COLOR_ERROR;
		}

		return new PolygonUI(points, convertedColor);
	}
}