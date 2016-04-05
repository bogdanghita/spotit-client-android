package com.it.spot.directions;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Created by Claudiu on 04-Apr-16.
 */
public class RouteData {
	public enum RouteType {DRIVING, WALKING}

	private RouteType mRouteType;

	private List<LatLng> mRoutePoints;

	private List<PolylineOptions> mRoutePolylineOptionsList;
	private List<Polyline> mRoutePolylines = null;
	private List<CircleOptions> mRouteCircleOptionsList;
	private List<Circle> mRouteCircles = null;

	public List<LatLng> getRoutePoints() {
		return mRoutePoints;
	}

	public void setRoutePoints(List<LatLng> mRoutePoints) {
		this.mRoutePoints = mRoutePoints;
	}

	public boolean isDrawn() {
		return mRouteCircles != null || mRoutePolylines != null;
	}

	public void undraw() {
		switch (mRouteType) {
			case DRIVING:
				this.setRouteCircles(null);
				break;
			case WALKING:
				this.setRoutePolylines(null);
				break;
		}
	}

	public RouteType getRouteType() {
		return mRouteType;
	}

	public void setRouteType(RouteType mRouteType) {
		this.mRouteType = mRouteType;
	}

	public List<PolylineOptions> getRoutePolylineOptionsList() {
		return mRoutePolylineOptionsList;
	}

	public void setRoutePolylineOptionsList(List<PolylineOptions> mRoutePolylineOptionsList) {
		this.mRoutePolylineOptionsList = mRoutePolylineOptionsList;
	}

	public List<CircleOptions> getRouteCircleOptionsList() {
		return mRouteCircleOptionsList;
	}

	public void setRouteCircleOptionsList(List<CircleOptions> mRouteCircleOptionsList) {
		this.mRouteCircleOptionsList = mRouteCircleOptionsList;
	}

	public List<Polyline> getRoutePolylines() {
		return mRoutePolylines;
	}

	public void setRoutePolylines(List<Polyline> mRoutePolylines) {
		this.mRoutePolylines = mRoutePolylines;
	}

	public List<Circle> getRouteCircles() {
		return mRouteCircles;
	}

	public void setRouteCircles(List<Circle> mRouteCircles) {
		this.mRouteCircles = mRouteCircles;
	}
}
