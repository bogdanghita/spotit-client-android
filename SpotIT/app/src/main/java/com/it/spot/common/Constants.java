package com.it.spot.common;

import android.graphics.Color;

/**
 * Created by Bogdan on 19/03/2016.
 */
public class Constants {

	public final static String APP = "APP-";
	public final static String LIFECYCLE = "LIFEC-";
	public final static String SIGN_IN = "SIGN_IN-";
	public final static String MENU = "MENU-";
	public final static String PERMISSION = "PERM-";
	public final static String TOKEN = "TOKEN-";
	public final static String CONNECTION = "CONNECT-";
	public final static String LOCATION = "LOCATION-";
	public final static String CAMERA_CHANGE = "CAMERA-";
	public final static String MAP_UPDATE = "MAP_UPDATE-";
	public final static String SERVER_API = "SERVER_API-";
	public final static String DRAW = "DRAW-";
	public final static String SAVED_SPOT = "SAVED-";
	public final static String DIRECTIONS = "DIRECT-";
	public final static String URL = "URL-";
	public final static String DIRECTION_TYPE = "DIR_TYPE-";
	public final static String ADDRESS = "ADDR-";
	public final static String DISTANCE_DURATION = "DIS_DUR-";
	public final static String STATE_MONITOR = "STATE_M-";
	public final static String EVENT = "EVENT-";
	public final static String ITEMS = "ITEMS-";
	public final static String INTERNET = "INTER-";
	public final static String TAG_TIMER = "TIMER";

	public final static int RC_SIGN_IN = 9001;
	public final static int USER_RECOVERABLE_AUTH_EXCEPTION = 7001;

	public final static String TOKEN_REQUEST_SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

	public final static int REQ_GET_ACCOUNTS = 1;
	public final static int REQ_FINE_LOCATION_ENABLE_LOCATION = 2;
	public final static int REQ_FINE_LOCATION_INIT = 3;
	public final static int REQ_FINE_LOCATION_CENTER = 4;

	public static final String STATE_RESOLVING_ERROR = "resolving_error";

	// Request code to use when launching the resolution activity
	public static final int REQUEST_RESOLVE_ERROR = 1001;
	// Unique tag for the error dialog fragment
	public static final String DIALOG_ERROR = "dialog_error";

	public static final int LOCATION_REQUEST_INTERVAL = 1000;
	public static final int LOCATION_REQUEST_FASTEST_INTERVAL = 500;

	public static final int STATUS_RED = 3;
	public static final int STATUS_YELLOW = 2;
	public static final int STATUS_GREEN = 1;
	public static final int STATUS_ERROR = 0;

	public final static String STATUS_RED_TEXT = "0 spots";
	public final static String STATUS_YELLOW_TEXT = "1-3 spots";
	public final static String STATUS_GREEN_TEXT = "> 3 spots";

	// Colors
	public static final int ALPHA = 60;
	public static final int COLOR_RED = Color.argb(ALPHA, 255, 0, 0);
	public static final int COLOR_GREEN = Color.argb(ALPHA, 0, 255, 0);
	public static final int COLOR_YELLOW = Color.argb(ALPHA, 255, 255, 0);
	public static final int COLOR_ERROR = Color.argb(255, 0, 0, 0);

	public static final String SERVER_ADDRESS = "http://spot-it-app.com";
	public static final int MAP_UPDATE_INTERVAL = 10 * 1000;
	public static final int HTTP_UNAUTHORIZED = 401;

	public static final String SAVED_SPOT_FILE = "saved_spot.json";
	public static final String USER_LIST_FILE = "user_list.json";

	public static final String GOOGLE_MAPS_API = "http://maps.googleapis.com";

	public final static String MODE_DRIVING = "driving";
	public final static String MODE_WALKING = "walking";

	public static final String NO_ADDRESS = "No address available.";

	public static final float DEFAULT_ZOOM = 17.3f;
	public static final int DIRECTIONS_STROKE_WIDTH = 30;
	public static final int DIRECTIONS_STROKE_COLOR = 0xFF3A7EC5;
	public static final int DIRECTIONS_LINE_WIDTH = 20;
	public static final int DIRECTIONS_LINE_COLOR = 0xFF00B3FD;

	// Good value for circle distance
//	public static final double CIRCLE_DISTANCE = 38;
	public static final double CIRCLE_DISTANCE = 50;
	public static final int CIRCLE_STROKE_WIDTH = 6;
	public static final int CIRCLE_SIZE = 15;
	public static final double SCALE_FACTOR_RECTIFIER = 1 / 3.7;
	public static final float CIRCLE_Z_INDEX = 1;

	public static final double MAX_ROUTE_ERROR = 0.1;
	public static final long MAX_ROUTE_ITER = 2;
	public static final double MIN_ROUTE_DISTANCE = 5;
	public static final double MAX_CIRCLES_PER_SEGMENT = 5;

	public static final int EARTH_RADIUS_M = 6371000;

	public static final int INITIAL_LOGIN_DELAY = 1000;
}
