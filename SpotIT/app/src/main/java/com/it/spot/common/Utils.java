package com.it.spot.common;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class Utils {

	public static double computeRadius(LatLngBounds cameraBounds) {

		return 0.01;
	}

	public static LatLng calculateDerivedPosition(LatLng source, double range, double bearing) {
		double latA = Math.toRadians(source.latitude);
		double lonA = Math.toRadians(source.longitude);
		double angularDistance = range / Constants.EARTH_RADIUS_M;
		double trueCourse = Math.toRadians(bearing);

		double lat = Math.asin(
				Math.sin(latA) * Math.cos(angularDistance) +
						Math.cos(latA) * Math.sin(angularDistance) * Math.cos(trueCourse));

		double dlon = Math.atan2(
				Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latA),
				Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

		double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

		return new LatLng(
				Math.toDegrees(lat),
				Math.toDegrees(lon));
	}

//	public static double atan2(double y, double x) {
//		if (y != 0) {
//			return 2 * Math.atan((Math.sqrt(x * x + y * y) - x) / y);
//		}
//		if (x > 0 && y == 0) {
//			return 0;
//		}
//		if (x < 0 && y == 0) {
//			return Math.PI;
//		}
//		return 0;
//	}

	public static double angleFromCoordinates(LatLng source, LatLng destination) {
		double lat1 = source.latitude;
		double long1 = source.longitude;
		double lat2 = destination.latitude;
		double long2 = destination.longitude;
		double dLon = (long2 - long1);

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
				* Math.cos(lat2) * Math.cos(dLon);

		double brng = Math.atan2(y, x);

		brng = Math.toDegrees(brng);
		brng = (brng + 360) % 360;
		//brng = 360 - brng;

		return brng;
	}

//	public static double angleFromCoordinates(LatLng source, LatLng destination) {
//		BigDecimal lat1 = new BigDecimal(source.latitude);
//		BigDecimal long1 = new BigDecimal(source.longitude);
//		BigDecimal lat2 = new BigDecimal(destination.latitude);
//		BigDecimal long2 = new BigDecimal(destination.longitude);
//		BigDecimal dLon = long2.subtract(long1);
//
//		BigDecimal y = new BigDecimal(Math.sin(dLon.doubleValue())).multiply(new BigDecimal(Math.cos(lat2.doubleValue())));
//		BigDecimal x = new BigDecimal(Math.cos(lat1.doubleValue()))
//				.multiply(new BigDecimal(Math.sin(lat2.doubleValue())))
//				.subtract(new BigDecimal(Math.sin(lat1.doubleValue()))
//						.multiply(new BigDecimal(Math.cos(lat2.doubleValue()))
//								.multiply(new BigDecimal(Math.cos(dLon.doubleValue())))));
//
//		BigDecimal brng = new BigDecimal(Math.atan2(y.doubleValue(), x.doubleValue()));
//
//		brng = new BigDecimal(Math.toDegrees(brng.doubleValue()));
//		brng = new BigDecimal(brng.doubleValue()).add(new BigDecimal(360)).remainder(new BigDecimal(360));
//		//brng = 360 - brng;
//
//		return brng.doubleValue();
//	}

	public static LatLng calculateDerivedPosition(LatLng source, LatLng destination, double range) {

		double rlat = Math.toRadians((source.latitude + destination.latitude) / 2);
		double mPerLat = 111132.92 - 559.82 * Math.cos(2 * rlat) + 1.175 * Math.cos(4 * rlat) - 0.0023 * Math.cos(6 * rlat);
		double mPerLong = 111412.84 * Math.cos(rlat) - 93.5 * Math.cos(3 * rlat) - 0.118 * Math.cos(5 * rlat);

		LatLng result = null;

		double errorCorrection = 0;
		long iter = 0;

		// Compute the result until it converges.
		do {

			result = new LatLng(source.latitude + (range / mPerLat) * Math.cos(Math.toRadians(Utils.angleFromCoordinates(source, destination) + errorCorrection)),
					source.longitude + (range / mPerLong) * Math.sin(Math.toRadians(Utils.angleFromCoordinates(source, destination) + errorCorrection)));

			// The angle between the desired vector direction and the actual vector direction.
			errorCorrection = Utils.angleFromCoordinates(source, destination) - Utils.angleFromCoordinates(source, result);
			iter++;

			Log.d("WTF", iter + " err:" + errorCorrection + " s:" + source + " d:" + destination + " r:" + result + " D:" + distanceBetween(source, destination));
		}
		while (Math.abs(errorCorrection) > Constants.MAX_ROUTE_ERROR && iter < Constants.MAX_ROUTE_ITER);

		return result;
	}

	public static double distanceBetween(LatLng source, LatLng destination) {
		Location s = new Location("");
		Location d = new Location("");

		s.setLatitude(source.latitude);
		s.setLongitude(source.longitude);

		d.setLatitude(destination.latitude);
		d.setLongitude(destination.longitude);

		return s.distanceTo(d);
	}
}
