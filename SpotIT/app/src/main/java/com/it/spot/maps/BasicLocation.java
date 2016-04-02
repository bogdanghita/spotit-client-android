package com.it.spot.maps;

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
}
