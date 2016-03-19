package com.it.spot.common;

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
}
