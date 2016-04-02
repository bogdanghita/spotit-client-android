package com.it.spot.maps;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Bogdan on 31/03/2016.
 */
public class BasicLocation {

	private double latitude;
	private double longitude;

	public BasicLocation(double latitude, double longitude) {

		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public BasicLocation clone() {
		return new BasicLocation(latitude, longitude);
	}

	@Override
	public String toString() {
		return latitude + ", " + longitude;
	}

	@Override
	public int hashCode() {

		return new HashCodeBuilder().
				append(latitude).
				append(longitude).
				toHashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof BasicLocation))
			return false;
		if (obj == this)
			return true;

		BasicLocation location = (BasicLocation) obj;

		if (latitude != location.latitude) {
			return false;
		}
		if (longitude != location.longitude) {
			return false;
		}

		return true;
	}
}
