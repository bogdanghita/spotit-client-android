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

	public final static int RC_SIGN_IN = 9001;
	public final static int USER_RECOVERABLE_AUTH_EXCEPTION = 7001;

	public final static String TOKEN_REQUEST_SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

	public final static int REQUEST_ID_GET_ACCOUNTS = 1;
	public final static int REQUEST_ID_ACCESS_FINE_LOCATION = 2;

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

	public static final String API = "http://192.168.1.105:3000";
	public static final int MAP_UPDATE_INTERVAL = 3 * 1000;
	public static final int HTTP_UNAUTHORIZED = 401;

	public static final String SAVED_SPOT_FILE = "saved_spot.json";
}
