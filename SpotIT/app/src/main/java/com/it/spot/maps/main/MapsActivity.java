package com.it.spot.maps.main;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.common.collect.HashMultimap;
import com.it.spot.R;
import com.it.spot.events.EventListener;
import com.it.spot.events.MapItemsProvider;
import com.it.spot.events.RemoveMarkerEvent;
import com.it.spot.events.SetMarkerEvent;
import com.it.spot.maps.FileService;
import com.it.spot.maps.MapItemsService;
import com.it.spot.maps.MarkerData;
import com.it.spot.maps.UiController;
import com.it.spot.maps.address.AddressResponseListener;
import com.it.spot.common.Constants;
import com.it.spot.common.ServiceManager;
import com.it.spot.identity.IdentityActivity;
import com.it.spot.identity.IdentityManager;
import com.it.spot.identity.ImageLoaderAsyncTask;
import com.it.spot.identity.LoginActivity;
import com.it.spot.identity.TokenRequestAsyncTask;
import com.it.spot.identity.TokenRequestEventListener;
import com.it.spot.identity.UserInfo;
import com.it.spot.maps.location.BasicLocation;
import com.it.spot.maps.report.DialogReveal;
import com.it.spot.services.PolygonUI;
import com.it.spot.threading.Event;
import com.it.spot.threading.StateMonitorListener;
import com.it.spot.threading.StateMonitorThread;

import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends IdentityActivity implements OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
		LocationListener, TokenRequestEventListener, StateMonitorListener, MapItemsProvider, UiController {

	private ActionBarDrawerToggle mDrawerToggle;

	private GoogleMap mMap;
	private GoogleApiClient mMapsGoogleApiClient;

	private String mLastUpdateTime;
	private LocationRequest mLocationRequest;
	private LatLngBounds cameraBounds;

	private boolean firstTimeLocation = true;

	// Bool to track whether the app is already resolving an error
	private boolean mResolvingError = false;

	private MapUpdateService mapUpdateService;
//	private LocationRouteService locationRouteService;

	private Event onConnectedEvent, onMapReadyEvent;

	private DialogReveal mReportParkingStateDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Disable activity start transition
		overridePendingTransition(0, 0);
		setContentView(R.layout.activity_maps);

		// Configure toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Configure sidebar
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		configureNavigationDrawer(drawerLayout, toolbar);

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		mResolvingError = savedInstanceState != null && savedInstanceState.getBoolean(Constants.STATE_RESOLVING_ERROR, false);

		mapUpdateService = new MapUpdateService(mapUpdateCallbackClient);
//		locationRouteService = new LocationRouteService(this, routeUpdateCallbackClient);

		// Subscribe map event listener
		ServiceManager.getInstance().getEventManager().subscribe(new MapItemsService(this, this, this));

		startStateMonitor();

		buildGoogleApiClient();

		createLocationRequest();

		createUserProfile();

		loadSavedSpot();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(Constants.STATE_RESOLVING_ERROR, mResolvingError);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mServiceManager.getIdentityManager().getToken() == null) {
			updateToken();
		}

		if (!mResolvingError) {
			mMapsGoogleApiClient.connect();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mapUpdateService.startMapStatusUpdateLoop();
	}

	@Override
	public void onPause() {
		super.onPause();

		mapUpdateService.stopMapStatusUpdateLoop();
	}

	@Override
	public void onStop() {
		super.onStop();

		mMapsGoogleApiClient.disconnect();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onBackPressed() {

		MarkerData markerData = mServiceManager.getMapItemsManager().getMarkerData();

		if (markerData == null || markerData.markerType != LocationRouteService.MarkerType.DESTINATION) {

			super.onBackPressed();
		}
		else {

			mServiceManager.getEventManager().triggerEvent(new RemoveMarkerEvent());

			// TODO: DONE

			setDirectionsButtonIcon(false);
		}


	}

	@Override
	public void onMapReady(GoogleMap googleMap) {

		mMap = googleMap;

		mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {
				cameraBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

				Log.d(Constants.APP + Constants.CAMERA_CHANGE, cameraBounds.getCenter() + " - " + cameraBounds.southwest + ", " + cameraBounds.northeast);

				// TODO
				// Updating the zoom and redrawing the route if necessary.
//				locationRouteService.setZoom(mMap.getCameraPosition().zoom);
//				locationRouteService.redrawRouteToMarker();

				// Setting camera position and requesting a map status update
				mapUpdateService.setCameraPosition(cameraBounds);
				mapUpdateService.requestMapStatusUpdate();
			}
		});

		mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//			@Override
//			public void onMapClick(final LatLng latLng) {
//
//				locationRouteService.setDestination(latLng);
//
//				setDirectionsButtonIcon(false);
//				setLocationInfoBarTitle();
//			}

			@Override
			public void onMapClick(final LatLng latLng) {

				MarkerData markerData = mServiceManager.getMapItemsManager().getMarkerData();
				if (markerData != null && markerData.markerType == LocationRouteService.MarkerType.SAVED_SPOT) {
					return;
				}

				mServiceManager.getEventManager().triggerEvent(new SetMarkerEvent(
						new BasicLocation(latLng.latitude, latLng.longitude), LocationRouteService.MarkerType.DESTINATION));

				// TODO: DONE

				setDirectionsButtonIcon(false);
				setLocationInfoBarTitle();
			}
		});

		onMapReadyEvent.set();
		Log.d(Constants.APP + Constants.STATE_MONITOR, "onMapReadyEvent.set()");

		// TODO
//		locationRouteService.drawMarker();
	}

	private void enableLocation() {

		if (!permission_FINELOCATION(Constants.REQ_FINE_LOCATION_ENABLE_LOCATION)) {
			return;
		}
		enableLocationAction();
	}

	private void enableLocationAction() {
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
	}

	private void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(mMapsGoogleApiClient, mLocationRequest, this);
	}

	private void initLastLocation() {

		Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mMapsGoogleApiClient);
		if (lastLocation != null) {
			mServiceManager.getLocationManager().setLastLocation(
					new BasicLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

			onLocationChanged(lastLocation);
		}
	}

	void startStateMonitor() {

		onConnectedEvent = new Event();
		onMapReadyEvent = new Event();
		List<Event> eventList = new LinkedList<>();
		eventList.add(onConnectedEvent);
		eventList.add(onMapReadyEvent);
		new StateMonitorThread(this, eventList).start();
	}

	@Override
	public void notifyStateReady() {

		Log.d(Constants.APP + Constants.STATE_MONITOR, "notifyStateReady()");

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				// onConnected
				if (!permission_FINELOCATION(Constants.REQ_FINE_LOCATION_INIT_LOCATION)) {
					return;
				}

				initLastLocation();
				startLocationUpdates();

				// onMapReady
				enableLocation();
			}
		});
	}

	@Override
	public void onConnected(Bundle bundle) {

		Log.d(Constants.APP + Constants.CONNECTION, "onConnected");

		onConnectedEvent.set();
		Log.d(Constants.APP + Constants.STATE_MONITOR, "onConnectedEvent.set()");

//		if (!permission_FINELOCATION(Constants.REQ_FINE_LOCATION_INIT_LOCATION)) {
//			return;
//		}
//		initLastLocation();
//		startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int i) {

		Log.d(Constants.APP + Constants.CONNECTION, "onConnectionSuspended: " + i);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		Log.d(Constants.APP + Constants.CONNECTION, "onConnectionFailed");

		if (mResolvingError) {
			// Already attempting to resolve an error.
			return;
		}
		else if (connectionResult.hasResolution()) {
			try {
				mResolvingError = true;
				connectionResult.startResolutionForResult(this, Constants.REQUEST_RESOLVE_ERROR);
			}
			catch (IntentSender.SendIntentException e) {
				// There was an error with the resolution intent. Try again.
				mMapsGoogleApiClient.connect();
			}
		}
		else {
			// Show dialog using GoogleApiAvailability.getErrorDialog()
			showErrorDialog(connectionResult.getErrorCode());
			mResolvingError = true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_RESOLVE_ERROR) {
			mResolvingError = false;
			if (resultCode == RESULT_OK) {
				// Make sure the app is not already connected or attempting to connect
				if (!mMapsGoogleApiClient.isConnecting() && !mMapsGoogleApiClient.isConnected()) {
					mMapsGoogleApiClient.connect();
				}
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {

		mServiceManager.getLocationManager().setLastLocation(
				new BasicLocation(location.getLatitude(), location.getLongitude()));

		mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

		if (firstTimeLocation) {

			centerCameraOnLastLocation();
			firstTimeLocation = false;
		}

		updateUI();
	}

	private synchronized void buildGoogleApiClient() {
		mMapsGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}

	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
		mLocationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	private void centerCameraOnLastLocation() {

		if (!permission_FINELOCATION(Constants.REQ_FINE_LOCATION_CENTER_LOCATION)) {
			return;
		}
		centerCameraOnLastLocationAction();
	}

	private void centerCameraOnLastLocationAction() {

		BasicLocation lastLocation = mServiceManager.getLocationManager().getLastLocation();
		if (lastLocation == null) {
			return;
		}

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
				.zoom(Constants.DEFAULT_ZOOM)
				.build();
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

// -------------------------------------------------------------------------------------------------
// GUI
// -------------------------------------------------------------------------------------------------

	private void configureNavigationDrawer(DrawerLayout mDrawerLayout, Toolbar toolbar) {

		// Setting up Navigation Drawer
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

			@Override
			public void onDrawerOpened(View v) {
				super.onDrawerOpened(v);

				invalidateOptionsMenu();
				syncState();
			}

			@Override
			public void onDrawerClosed(View v) {
				super.onDrawerClosed(v);

				invalidateOptionsMenu();
				syncState();
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		mDrawerToggle.syncState();
	}

	private void toggleNavigationDrawer() {

		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
			mDrawerLayout.closeDrawer(Gravity.LEFT);
		}
		else {
			mDrawerLayout.openDrawer(Gravity.LEFT);
		}
	}

	private void createUserProfile() {

		IdentityManager identityManager = ServiceManager.getInstance().getIdentityManager();

		if (identityManager.getUserInfo() != null) {

			// Setting up user profile
			UserInfo userInfo = identityManager.getUserInfo();

			TextView name = (TextView) findViewById(R.id.name);
			TextView email = (TextView) findViewById(R.id.email);
			CircleImageView image = (CircleImageView) findViewById(R.id.circleView);

			name.setText(userInfo.getName());
			email.setText(userInfo.getEmail());
			new ImageLoaderAsyncTask(image).execute(userInfo.getPicture());
		}
	}

	private void updateUI() {
		BasicLocation lastLocation = mServiceManager.getLocationManager().getLastLocation();
		Log.d(Constants.APP + Constants.LOCATION, mLastUpdateTime + ": " + lastLocation.getLatitude() +
				", " + lastLocation.getLongitude());
	}

	private void drawPolygon(Iterable<LatLng> points, int color) {

		String text = "";
		for (LatLng point : points) {
			text += point.toString() + ", ";
		}
		Log.d(Constants.APP + Constants.DRAW, text);

		mMap.addPolygon(new PolygonOptions()
				.addAll(points)
				.strokeColor(color)
				.strokeWidth(0)
				.fillColor(color));
	}

// -------------------------------------------------------------------------------------------------
// LOGIN
// -------------------------------------------------------------------------------------------------

	public void buttonSignOut(View view) {

		Log.d(Constants.APP + Constants.MENU, "Button Sign out");

		// Clear saved spot
		new FileService(this).writeSavedSpotFile(new SavedSpot(false, null), Constants.SAVED_SPOT_FILE);

		// Signing out from Google
		Auth.GoogleSignInApi.signOut(mIdentityGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
			@Override
			public void onResult(Status status) {

				Log.d("Identity logout status", status.toString());

				// Clearing state of service manager
				ServiceManager.getInstance().clear();

				// Starting sign in activity
				startSignInActivity();
			}
		});
	}

	private void startSignInActivity() {

		Intent intent = new Intent(this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	private void updateToken() {

		if (mServiceManager.getIdentityManager().getUserInfo() == null) {
			Log.d(Constants.APP, "No user info available.");
			return;
		}

		String email = mServiceManager.getIdentityManager().getUserInfo().getEmail();

		new TokenRequestAsyncTask(this, this, email, Constants.TOKEN_REQUEST_SCOPE).execute();
	}

	@Override
	public void requestFailed() {

		Log.d(Constants.APP + Constants.TOKEN, "Token request failed.");
	}

	@Override
	public void requestSucceeded(String token) {

		mServiceManager.getIdentityManager().setToken(token);

		Log.d(Constants.APP + Constants.TOKEN, "Token obtained successfully.");
	}

// -------------------------------------------------------------------------------------------------
// MAPS ERROR DIALOG
// -------------------------------------------------------------------------------------------------

	/* Creates a dialog for an error message */
	private void showErrorDialog(int errorCode) {
		// Create a fragment for the error dialog
		ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
		// Pass the error that should be displayed
		Bundle args = new Bundle();
		args.putInt(Constants.DIALOG_ERROR, errorCode);
		dialogFragment.setArguments(args);
		dialogFragment.show(getSupportFragmentManager(), Constants.DIALOG_ERROR);
	}

	/* Called from ErrorDialogFragment when the dialog is dismissed. */
	public void onDialogDismissed() {
		mResolvingError = false;
	}

	/* A fragment to display an error dialog */
	public static class ErrorDialogFragment extends DialogFragment {
		public ErrorDialogFragment() {
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Get the error code and retrieve the appropriate dialog
			int errorCode = this.getArguments().getInt(Constants.DIALOG_ERROR);
			return GoogleApiAvailability.getInstance().getErrorDialog(
					this.getActivity(), errorCode, Constants.REQUEST_RESOLVE_ERROR);
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			((MapsActivity) getActivity()).onDialogDismissed();
		}
	}

// -------------------------------------------------------------------------------------------------
// PERMISSIONS
// -------------------------------------------------------------------------------------------------

	private boolean permission_FINELOCATION(int request_code) {

		List<String> listPermissionsNeeded = new ArrayList<>();

		int permissionGetAccounts = ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION);

		if (permissionGetAccounts != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
		}

		if (!listPermissionsNeeded.isEmpty()) {
			ActivityCompat.requestPermissions(this,
					listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
					request_code);
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

		switch (requestCode) {
			case Constants.REQ_FINE_LOCATION_ENABLE_LOCATION: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					enableLocationAction();
				}
				break;
			}
			case Constants.REQ_FINE_LOCATION_INIT_LOCATION: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					initLastLocation();
					startLocationUpdates();
				}
				break;
			}
			case Constants.REQ_FINE_LOCATION_CENTER_LOCATION: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					centerCameraOnLastLocationAction();
				}
				break;
			}
		}
	}

// -------------------------------------------------------------------------------------------------
// SIDEBAR OPTIONS
// -------------------------------------------------------------------------------------------------

//	public void buttonSaveSpot(View view) {
//
//		locationRouteService.saveSpot();
//
//		toggleSaveSpotButton();
//
//		toggleNavigationDrawer();
//
//		setDirectionsButtonIcon(false);
//		setLocationInfoBarTitle();
//	}

	public void buttonSaveSpot(View view) {

		BasicLocation lastLocation = mServiceManager.getLocationManager().getLastLocation();
		if (lastLocation == null) {
			return;
		}

		mServiceManager.getEventManager().triggerEvent(new SetMarkerEvent(lastLocation,
				LocationRouteService.MarkerType.SAVED_SPOT));

		// TODO: DONE

		toggleSaveSpotButton();
		toggleNavigationDrawer();

		setDirectionsButtonIcon(false);
		setLocationInfoBarTitle();
	}

//	public void buttonLeaveSpot(View view) {
//
//		locationRouteService.leaveSpot();
//
//		toggleSaveSpotButton();
//
//		toggleNavigationDrawer();
//
//		setDirectionsButtonIcon(false);
//		setLocationInfoBarTitle();
//	}

	public void buttonLeaveSpot(View view) {

		mServiceManager.getEventManager().triggerEvent(new RemoveMarkerEvent());

		// TODO: DONE

		toggleSaveSpotButton();
		toggleNavigationDrawer();

		setDirectionsButtonIcon(false);
		setLocationInfoBarTitle();
	}

	void toggleSaveSpotButton() {

		RelativeLayout buttonSaveSpot = (RelativeLayout) findViewById(R.id.item_save_spot);
		RelativeLayout buttonLeaveSpot = (RelativeLayout) findViewById(R.id.item_leave_spot);

		if (mServiceManager.getMapItemsManager().hasSavedSpot()) {
			buttonLeaveSpot.setVisibility(View.VISIBLE);
			buttonSaveSpot.setVisibility(View.GONE);
		}
		else {
			buttonLeaveSpot.setVisibility(View.GONE);
			buttonSaveSpot.setVisibility(View.VISIBLE);
		}
	}

	public void buttonHistory(View view) {

	}

	public void buttonHelp(View view) {

	}

// -------------------------------------------------------------------------------------------------
// ON SCREEN BUTTONS
// -------------------------------------------------------------------------------------------------

	public void buttonOpenParkingStateOptions(View v) {

		mReportParkingStateDialog = new DialogReveal(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		mReportParkingStateDialog.show();
	}

	public void buttonReportParkingState(View v) {
		int buttonId = v.getId();

		// Dismiss dialog
		mReportParkingStateDialog.dismiss();

		BasicLocation lastLocation = mServiceManager.getLocationManager().getLastLocation();
		if (lastLocation == null) {
			return;
		}

		switch (buttonId) {
			case R.id.fab_free:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_GREEN_TEXT);
				break;
			case R.id.fab_moderate:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_YELLOW_TEXT);
				break;
			case R.id.fab_full:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_RED_TEXT);
				break;
		}

		// Request map update status to give the user instant feedback
		mapUpdateService.requestMapStatusUpdate();
	}

	public void buttonCenterOnLocation(View v) {

		centerCameraOnLastLocation();
	}

	public void buttonDirections(View v) {

		// TODO
//		if (!locationRouteService.hasDirections()) {
//			locationRouteService.drawRouteToMarker();
//		}
//		else {
//			locationRouteService.removeRouteToMarker();
//		}
	}

	private void setDirectionsButtonIcon(boolean iconClosed) {

		int icon_id;

		MarkerData markerData = mServiceManager.getMapItemsManager().getMarkerData();

		if (iconClosed) {
			icon_id = R.drawable.ic_close_white_24dp;
		}
		else if (markerData == null) {
			return;
		}
		else if (markerData.getMarkerType() == LocationRouteService.MarkerType.DESTINATION) {
			icon_id = R.drawable.ic_directions_car_white_24dp;
		}
		else if (markerData.getMarkerType() == LocationRouteService.MarkerType.SAVED_SPOT) {
			icon_id = R.drawable.ic_directions_walk_white_24dp;
		}
		else {
			return;
		}

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.directions_fab);
		fab.setImageDrawable(getResources().getDrawable(icon_id));
	}

	private void setLocationInfoBarTitle() {

		String text;

		MarkerData markerData = mServiceManager.getMapItemsManager().getMarkerData();
		if (markerData == null) {
			return;
		}

		if (markerData.getMarkerType() == LocationRouteService.MarkerType.DESTINATION) {
			text = getResources().getString(R.string.location_info_bar_title_destination);
		}
		else if (markerData.getMarkerType() == LocationRouteService.MarkerType.SAVED_SPOT) {
			text = getResources().getString(R.string.location_info_bar_title_saved_spot);
		}
		else {
			return;
		}

		TextView tv = (TextView) findViewById(R.id.location_title);
		tv.setText(text);
	}

// -------------------------------------------------------------------------------------------------
// UPDATE CALLBACK CLIENT
// -------------------------------------------------------------------------------------------------

	private MapUpdateCallbackClient mapUpdateCallbackClient = new MapUpdateCallbackClient() {
		@Override
		public void updateMapStatus(final List<PolygonUI> polygons) {

			Log.d(Constants.APP + Constants.MAP_UPDATE, "Received update map status. Polygons: " + polygons.size());

			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if (mMap == null) {
						return;
					}

					// Clearing polygons
					mMap.clear();

					// TODO
					// Notify route service
//					locationRouteService.notifyMapCleared();

					// Drawing all polygons
					for (PolygonUI polygon : polygons) {

						drawPolygon(polygon.getPoints(), polygon.getColor());
					}
				}
			});
		}

		@Override
		public void notifyRequestFailure() {
			updateToken();
		}
	};

//	private RouteUpdateCallbackClient routeUpdateCallbackClient = new RouteUpdateCallbackClient() {
//
//		@Override
//		public void drawRoute(final RouteData routeData, final RouteUpdateResultCallbackClient client) {
//
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					if (mMap != null) {
//						setDirectionsButtonIcon(true);
//						switch (routeData.getRouteType()) {
//							case DRIVING:
//								List<Polyline> polylines = new ArrayList<>();
//								for (PolylineOptions polylineOptions : routeData.getRoutePolylineOptionsList()) {
//									polylines.add(mMap.addPolyline(polylineOptions));
//								}
//								client.notifyDrivingResult(polylines);
//								break;
//							case WALKING:
//								List<Circle> circles = new ArrayList<>();
//								for (CircleOptions circleOptions : routeData.getRouteCircleOptionsList()) {
//									circles.add(mMap.addCircle(circleOptions));
//								}
//								client.notifyWalkingResult(circles);
//								break;
//						}
//					}
//				}
//			});
//		}
//
//		@Override
//		public void removeRoute(final RouteData routeData) {
//
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					switch (routeData.getRouteType()) {
//						case DRIVING:
//							for (Polyline polyline : routeData.getRoutePolylines()) {
//								polyline.remove();
//							}
//							break;
//						case WALKING:
//							for (Circle circle : routeData.getRouteCircles()) {
//								circle.remove();
//							}
//							break;
//					}
//					setDirectionsButtonIcon(false);
//				}
//			});
//		}
//
//		@Override
//		public void drawMarker(final MarkerOptions markerOptions, final RouteUpdateResultCallbackClient client) {
//
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					Marker result = null;
//					if (mMap != null) {
//						result = mMap.addMarker(markerOptions);
//					}
//					client.notifyMarkerResult(result);
//
//					LatLng position = markerOptions.getPosition();
//					openLocationInfoBar(new BasicLocation(position.latitude, position.longitude));
//				}
//			});
//		}
//
//		@Override
//		public void removeMarker(final Marker marker) {
//
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					marker.remove();
//
//					closeLocationInfoBar();
//				}
//			});
//		}
//	};

// -------------------------------------------------------------------------------------------------
// NEW
// -------------------------------------------------------------------------------------------------

	private void loadSavedSpot() {

//		locationRouteService.loadSavedSpot();
//		toggleSaveSpotButton();

		SavedSpot savedSpot = new FileService(this).readSavedSpotFile(Constants.SAVED_SPOT_FILE);

		if (savedSpot != null && savedSpot.hasSavedSpot) {
			mServiceManager.getEventManager().triggerEvent(new SetMarkerEvent(savedSpot.location, LocationRouteService.MarkerType.SAVED_SPOT));
		}

		// TODO: DONE

		toggleSaveSpotButton();
	}

	@Override
	public GoogleMap getMap() {
		return mMap;
	}

	@Override
	public View getView(int id) {
		return findViewById(id);
	}

	@Override
	public void doRunOnUiThread(Runnable runnable) {
		runOnUiThread(runnable);
	}
}
