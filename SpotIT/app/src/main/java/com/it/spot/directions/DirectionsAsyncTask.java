package com.it.spot.directions;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.it.spot.common.Constants;
import com.it.spot.common.Utils;
import com.it.spot.directions.RouteData.RouteType;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Claudiu on 19-Mar-16.
 */
public class DirectionsAsyncTask extends AsyncTask<RouteOptions, Void, Void> {

	private DirectionsResultListener mDirectionsResultListener;

	public DirectionsAsyncTask(DirectionsResultListener directionsResultListener) {

		this.mDirectionsResultListener = directionsResultListener;
	}

	@Override
	protected Void doInBackground(RouteOptions... params) {

		RouteOptions routeOptions = params[0];
		RouteData routeData = getDirections(routeOptions.source, routeOptions.destination, routeOptions.mode, routeOptions.zoom);

		if (routeData == null)
			return null;

		mDirectionsResultListener.notifyDirectionsResponse(routeData);

		return null;
	}

	public Document getDocument(LatLng start, LatLng end, String mode) {

		String url = "http://maps.googleapis.com/maps/api/directions/xml?"
				+ "origin=" + start.latitude + "," + start.longitude
				+ "&destination=" + end.latitude + "," + end.longitude
				+ "&sensor=false&units=metric&mode=" + mode;
		Log.d(Constants.DIRECTIONS + Constants.URL, url);

		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(url);
			HttpResponse response = httpClient.execute(httpPost, localContext);
			InputStream in = response.getEntity().getContent();

			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(in);
			// TODO: NullPointerException on doc.getElementsByTagName("travel_mode").item(0)
			Log.d(Constants.DIRECTIONS + Constants.DIRECTION_TYPE,
					doc.getElementsByTagName("travel_mode").item(0).getTextContent());
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getDurationText(Document doc) {
		try {

			NodeList nl1 = doc.getElementsByTagName("duration");
			Node node1 = nl1.item(0);
			NodeList nl2 = node1.getChildNodes();
			Node node2 = nl2.item(getNodeIndex(nl2, "text"));
			Log.i("DurationText", node2.getTextContent());
			return node2.getTextContent();
		} catch (Exception e) {
			return "0";
		}
	}

	public int getDurationValue(Document doc) {
		try {
			NodeList nl1 = doc.getElementsByTagName("duration");
			Node node1 = nl1.item(0);
			NodeList nl2 = node1.getChildNodes();
			Node node2 = nl2.item(getNodeIndex(nl2, "value"));
			Log.i("DurationValue", node2.getTextContent());
			return Integer.parseInt(node2.getTextContent());
		} catch (Exception e) {
			return -1;
		}
	}

	public String getDistanceText(Document doc) {

		try {
			NodeList nl1;
			nl1 = doc.getElementsByTagName("distance");

			Node node1 = nl1.item(nl1.getLength() - 1);
			NodeList nl2 = null;
			nl2 = node1.getChildNodes();
			Node node2 = nl2.item(getNodeIndex(nl2, "value"));
			Log.d("DistanceText", node2.getTextContent());
			return node2.getTextContent();
		} catch (Exception e) {
			return "-1";
		}
	}

	public int getDistanceValue(Document doc) {
		try {
			NodeList nl1 = doc.getElementsByTagName("distance");
			Node node1 = null;
			node1 = nl1.item(nl1.getLength() - 1);
			NodeList nl2 = node1.getChildNodes();
			Node node2 = nl2.item(getNodeIndex(nl2, "value"));
			Log.i("DistanceValue", node2.getTextContent());
			return Integer.parseInt(node2.getTextContent());
		} catch (Exception e) {
			return -1;
		}
	}

	public String getStartAddress(Document doc) {
		try {
			NodeList nl1 = doc.getElementsByTagName("start_address");
			Node node1 = nl1.item(0);
			Log.i("StartAddress", node1.getTextContent());
			return node1.getTextContent();
		} catch (Exception e) {
			return "-1";
		}
	}

	public String getEndAddress(Document doc) {
		try {
			NodeList nl1 = doc.getElementsByTagName("end_address");
			Node node1 = nl1.item(0);
			Log.i("StartAddress", node1.getTextContent());
			return node1.getTextContent();
		} catch (Exception e) {
			return "-1";
		}
	}

	public String getCopyRights(Document doc) {
		try {
			NodeList nl1 = doc.getElementsByTagName("copyrights");
			Node node1 = nl1.item(0);
			Log.i("CopyRights", node1.getTextContent());
			return node1.getTextContent();
		} catch (Exception e) {
			return "-1";
		}

	}

	public RouteData getDirections(LatLng start, LatLng end, String mode, float zoom) {
		Document doc = getDocument(start, end, mode);

		if (doc == null)
			return null;

		ArrayList<LatLng> directions = getDirection(doc);
		if (directions == null)
			return null;

		RouteData routeData = null;

		if (mode.equals(Constants.MODE_WALKING)) {
			List<CircleOptions> circleOptionsList = getWalkingDirections(directions, zoom);
			routeData = new RouteData();
			routeData.setRouteType(RouteType.WALKING);
			routeData.setRouteCircleOptionsList(circleOptionsList);
			routeData.setRoutePoints(directions);
		}else{
			if(mode.equals(Constants.MODE_DRIVING)){
				List<PolylineOptions> polylineOptionsList = getDrivingDirections(directions);
				routeData = new RouteData();
				routeData.setRouteType(RouteType.DRIVING);
				routeData.setRoutePolylineOptionsList(polylineOptionsList);
				routeData.setRoutePoints(directions);
			}
		}

		return routeData;
	}

	public List<PolylineOptions> getDrivingDirections(List<LatLng> points) {

		List<PolylineOptions> polylineOptionsList = new ArrayList<>();

		polylineOptionsList.add(new PolylineOptions().width(Constants.DIRECTIONS_STROKE_WIDTH).color(
				Constants.DIRECTIONS_STROKE_COLOR));
		polylineOptionsList.add(new PolylineOptions().width(Constants.DIRECTIONS_LINE_WIDTH).color(
				Constants.DIRECTIONS_LINE_COLOR));

		for (int i = 0; i < points.size(); i++) {
			polylineOptionsList.get(0).add(points.get(i));
			polylineOptionsList.get(1).add(points.get(i));
		}

		return polylineOptionsList;
	}

	public static List<CircleOptions> getWalkingDirections(List<LatLng> points, float zoom) {

		List<CircleOptions> circleOptionsList = new ArrayList<>();

		LatLng pointA, pointB, intermPoint;
		Location locationA = new Location("");
		Location locationB = new Location("");
		Location intermLocation = new Location("");

		if (points.size() < 2)
			return null;

		double padding = 0;
		double scaleFactor = Math.pow(2, Constants.DEFAULT_ZOOM - zoom) * Constants.SCALE_FACTOR_RECTIFIER;

		for (int i = 1; i < points.size(); i++) {
			pointA = points.get(i - 1);
			pointB = points.get(i);

			locationA.setLatitude(pointA.latitude);
			locationA.setLongitude(pointA.longitude);
			locationB.setLatitude(pointB.latitude);
			locationB.setLongitude(pointB.longitude);

			// Compute first intermediate point based on previous padding.
			intermPoint = Utils.calculateDerivedPosition(
					pointA,
					pointB,
					padding);

			intermLocation.setLatitude(intermPoint.latitude);
			intermLocation.setLongitude(intermPoint.longitude);

			int chunks = 0;

			// While intermPoint is still on segment AB.
			while (locationA.distanceTo(locationB) > locationA.distanceTo(intermLocation)) {
				// Add current circle.
				circleOptionsList.add(new CircleOptions()
						.center(intermPoint)
						.fillColor(Constants.DIRECTIONS_LINE_COLOR)
						.strokeColor(Constants.DIRECTIONS_STROKE_COLOR)
						.radius(Constants.CIRCLE_SIZE * scaleFactor)
						.strokeWidth(Constants.CIRCLE_STROKE_WIDTH));
				// Prepare the next one.
				chunks++;

				intermPoint = Utils.calculateDerivedPosition(
						pointA,
						pointB,
						(chunks * Constants.CIRCLE_DISTANCE + padding) * scaleFactor);

				intermLocation.setLatitude(intermPoint.latitude);
				intermLocation.setLongitude(intermPoint.longitude);
			}
			// Prepair padding for the next draw.
			padding = locationB.distanceTo(intermLocation);
		}

		return circleOptionsList;
	}

	public ArrayList<LatLng> getDirection(Document doc) {
		NodeList nl1, nl2, nl3;
		ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
		nl1 = doc.getElementsByTagName("step");
		if (nl1.getLength() > 0) {
			for (int i = 0; i < nl1.getLength(); i++) {
				Node node1 = nl1.item(i);
				nl2 = node1.getChildNodes();

				Node locationNode = nl2
						.item(getNodeIndex(nl2, "start_location"));
				nl3 = locationNode.getChildNodes();
				Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
				double lat = Double.parseDouble(latNode.getTextContent());
				Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
				double lng = Double.parseDouble(lngNode.getTextContent());
				listGeopoints.add(new LatLng(lat, lng));

				locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
				nl3 = locationNode.getChildNodes();
				latNode = nl3.item(getNodeIndex(nl3, "points"));
				ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
				for (int j = 0; j < arr.size(); j++) {
					listGeopoints.add(new LatLng(arr.get(j).latitude, arr
							.get(j).longitude));
				}

				locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
				nl3 = locationNode.getChildNodes();
				latNode = nl3.item(getNodeIndex(nl3, "lat"));
				lat = Double.parseDouble(latNode.getTextContent());
				lngNode = nl3.item(getNodeIndex(nl3, "lng"));
				lng = Double.parseDouble(lngNode.getTextContent());
				listGeopoints.add(new LatLng(lat, lng));
			}
		}

		return listGeopoints;
	}

	private int getNodeIndex(NodeList nl, String nodename) {
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().equals(nodename))
				return i;
		}
		return -1;
	}

	private ArrayList<LatLng> decodePoly(String encoded) {
		ArrayList<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;
		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
			poly.add(position);
		}
		return poly;
	}
}