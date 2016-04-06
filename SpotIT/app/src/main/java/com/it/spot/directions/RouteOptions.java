package com.it.spot.directions;

import com.google.android.gms.maps.model.LatLng;
import com.it.spot.common.Constants;

/**
 * Created by Claudiu on 19-Mar-16.
 */
public class RouteOptions {
    public LatLng source;
    public LatLng destination;
    public String mode;
	public float zoom = Constants.DEFAULT_ZOOM;

	public RouteOptions(LatLng source, LatLng destination, String mode){
		this.source = source;
		this.destination = destination;
		this.mode = mode;
	}

	public RouteOptions(LatLng source, LatLng destination, String mode, float zoom){
		this(source,destination,mode);
		this.zoom=zoom;
	}
}
