package com.it.spot.maps;

import com.google.gson.annotations.Expose;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class Message {

	public static class GeoMessage {

		@Expose
		public double latitude;

		@Expose
		public double longitude;

		public GeoMessage(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
	}

	public static class StatusMessage extends GeoMessage {

		@Expose
		public int status;

		public StatusMessage(double latitude, double longitude, int rating) {
			super(latitude, longitude);
			this.status = rating;
		}
	}

	public static class StatusMessageID extends StatusMessage {

		@Expose
		public String _id;

		public StatusMessageID(double latitude, double longitude, int status, String id) {
			super(latitude, longitude, status);
			this._id = id;
		}
	}

	public static class GetMapMessage extends GeoMessage {

		@Expose
		public double radius;

		public GetMapMessage(double latitude, double longitude, double radius) {
			super(latitude, longitude);
			this.radius = radius;
		}
	}

	public static class MapMessage {
		// TODO: Insert magic dreptunghi here.
	}

	public static class MapMessageID extends MapMessage {

		@Expose
		public String _id;

		public MapMessageID(String id) {
			super();
			this._id = id;
		}
	}
}
