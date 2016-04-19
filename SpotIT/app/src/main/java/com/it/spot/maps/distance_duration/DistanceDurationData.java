package com.it.spot.maps.distance_duration;

/**
 * Created by Claudiu on 19-Apr-16.
 */
public class DistanceDurationData {

	private String mDistance;
	private String mDuration;

	public DistanceDurationData(String mDistance, String mDuration) {
		this.mDistance = mDistance;
		this.mDuration = mDuration;
	}

	public String getDistance() {
		return mDistance;
	}

	public void setDistance(String distance) {
		this.mDistance = distance;
	}


	public String getDuration() {
		return mDuration;
	}

	public void setDuration(String duration) {
		this.mDuration = duration;
	}
}
