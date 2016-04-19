package com.it.spot.distance_duration;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Claudiu on 19-Apr-16.
 */
public class DistanceDurationOptions {
	public LatLng source;
	public LatLng destination;
	public String mode;

	public DistanceDurationOptions(LatLng source, LatLng destination, String mode){
		this.source = source;
		this.destination = destination;
		this.mode = mode;
	}
}
