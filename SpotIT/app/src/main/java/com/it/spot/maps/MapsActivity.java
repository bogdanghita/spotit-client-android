package com.it.spot.maps;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.Window;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.it.spot.R;
import com.it.spot.address.AddressAsyncTask;
import com.it.spot.address.AddressResponseListener;
import com.it.spot.common.Constants;
import com.it.spot.common.ServiceManager;
import com.it.spot.common.Utils;
import com.it.spot.identity.IdentityActivity;
import com.it.spot.identity.IdentityManager;
import com.it.spot.identity.ImageLoaderAsyncTask;
import com.it.spot.identity.LoginActivity;
import com.it.spot.identity.TokenRequestAsyncTask;
import com.it.spot.identity.TokenRequestEventListener;
import com.it.spot.identity.UserInfo;
import com.it.spot.services.PolygonUI;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends IdentityActivity implements OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
		LocationListener, TokenRequestEventListener {

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
	private LocationRouteService locationRouteService;

	private boolean parking_state_button_flag = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		locationRouteService = new LocationRouteService(this, routeUpdateCallbackClient);

		buildGoogleApiClient();

		createLocationRequest();

		createUserProfile();

		locationRouteService.loadSavedSpot();
		toggleSaveSpotButton();
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
		if (locationRouteService.getMarkerType() == LocationRouteService.MarkerType.DESTINATION) {
			locationRouteService.removeDestination();
			setDirectionsButtonIcon(false);
		}
		else {
			super.onBackPressed();
		}
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {

		mMap = googleMap;

		mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {
				cameraBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

				Log.d(Constants.APP + Constants.CAMERA_CHANGE, cameraBounds.getCenter() + " - " + cameraBounds.southwest + ", " + cameraBounds.northeast);

				// Setting camera position and requesting a map status update
				mapUpdateService.setCameraPosition(cameraBounds);
				mapUpdateService.requestMapStatusUpdate();
			}
		});

		mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(final LatLng latLng) {

				locationRouteService.setDestination(latLng);

				setDirectionsButtonIcon(false);
			}
		});

		// TODO: solve the permissions problem
		if (checkAndRequestPermissionACCESS_FINE_LOCATION()) {
			enableLocation();
		}

		locationRouteService.drawMarker();
	}

	void enableLocation() {
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
	}

	@Override
	public void onConnected(Bundle bundle) {

		Log.d(Constants.APP + Constants.CONNECTION, "onConnected");

		// TODO: Problem here. FINE_LOCATION is needed in 2 different places. However there is only
		// one callback on permission granted. Solve this...
		if (!checkAndRequestPermissionACCESS_FINE_LOCATION()) {
			return;
		}

		Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mMapsGoogleApiClient);
		if (lastLocation != null) {
			mServiceManager.getLocationManager().setLastLocation(
					new BasicLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

			onLocationChanged(lastLocation);
		}

		startLocationUpdates();
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

	protected synchronized void buildGoogleApiClient() {
		mMapsGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
		mLocationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	protected void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(mMapsGoogleApiClient, mLocationRequest, this);
	}

	private void centerCameraOnLastLocation() {
		//mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));

		BasicLocation lastLocation = mServiceManager.getLocationManager().getLastLocation();
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

	// The rest of this code is all about building the error dialog

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

	private boolean checkAndRequestPermissionACCESS_FINE_LOCATION() {

		List<String> listPermissionsNeeded = new ArrayList<>();

		int permissionGetAccounts = ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION);

		if (permissionGetAccounts != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
		}

		if (!listPermissionsNeeded.isEmpty()) {
			ActivityCompat.requestPermissions(this,
					listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
					Constants.REQUEST_ID_ACCESS_FINE_LOCATION);
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

		switch (requestCode) {
			case Constants.REQUEST_ID_ACCESS_FINE_LOCATION: {

				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					enableLocation();
				}
				else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				return;
			}
		}
	}

// -------------------------------------------------------------------------------------------------
// SIDEBAR OPTIONS
// -------------------------------------------------------------------------------------------------

	public void buttonSaveSpot(View view) {

		locationRouteService.saveSpot();

		toggleSaveSpotButton();

		toggleNavigationDrawer();

		setDirectionsButtonIcon(false);
	}

	public void buttonLeaveSpot(View view) {

		locationRouteService.leaveSpot();

		toggleSaveSpotButton();

		toggleNavigationDrawer();

		setDirectionsButtonIcon(false);
	}

	void toggleSaveSpotButton() {

		RelativeLayout buttonSaveSpot = (RelativeLayout) findViewById(R.id.item_save_spot);
		RelativeLayout buttonLeaveSpot = (RelativeLayout) findViewById(R.id.item_leave_spot);

		if (locationRouteService.isSpotSaved()) {
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

		int visibility;

		if (parking_state_button_flag) {
			// Open
			visibility = View.VISIBLE;
		}
		else {
			// Close
			visibility = View.GONE;
		}

		parking_state_button_flag = !parking_state_button_flag;

		findViewById(R.id.fab_free_2).setVisibility(visibility);
		findViewById(R.id.fab_moderate_2).setVisibility(visibility);
		findViewById(R.id.fab_full_2).setVisibility(visibility);
	}

	public void buttonOpenParkingStateOptionsV2(View v) {

		Dialog alertDialog = new Dialog(this, R.style.AlertDialogSpotReport);
		alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		alertDialog.setContentView(R.layout.report_parking_spot_layout);
		alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		alertDialog.show();
	}

	public void buttonReportParkingState(View v) {

		findViewById(R.id.fab_free_2).setVisibility(View.GONE);
		findViewById(R.id.fab_moderate_2).setVisibility(View.GONE);
		findViewById(R.id.fab_full_2).setVisibility(View.GONE);

		parking_state_button_flag = true;

		BasicLocation lastLocation = mServiceManager.getLocationManager().getLastLocation();
		if (lastLocation == null) {
			return;
		}

		// TODO: keep the desired set and remove the other one
		switch (v.getId()) {
			// First set of buttons
			case R.id.fab_free_2:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_GREEN_TEXT);
				break;
			case R.id.fab_moderate_2:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_YELLOW_TEXT);
				break;
			case R.id.fab_full_2:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_RED_TEXT);
				break;
			// Second set of buttons
			case R.id.fab_free:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_GREEN_TEXT);
				break;
			case R.id.fab_moderate:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_YELLOW_TEXT);
				break;
			case R.id.fab_full:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_RED_TEXT);
				break;
			// Third set of buttons
			case R.id.fab_free_3:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_GREEN_TEXT);
				break;
			case R.id.fab_moderate_3:
				mapUpdateService.sendMapStatus(lastLocation, Constants.STATUS_YELLOW_TEXT);
				break;
			case R.id.fab_full_3:
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

		if (!locationRouteService.hasDirectionsPolyline()) {
			locationRouteService.drawRouteToMarker();
		}
		else {
			locationRouteService.removeRouteToMarker();
		}
	}

	private void openLocationInfoBar(BasicLocation location) {

		LinearLayout bottom_layout = (LinearLayout) findViewById(R.id.location_info_bar);
		View directions_fab = findViewById(R.id.directions_fab);

		bottom_layout.setTranslationY(0);
		directions_fab.setVisibility(View.VISIBLE);

		AddressAsyncTask addressAsyncTask = new AddressAsyncTask(addressResponseListener);
		addressAsyncTask.execute(location);
	}

	private void closeLocationInfoBar() {

		LinearLayout bottom_layout = (LinearLayout) findViewById(R.id.location_info_bar);
		View directions_fab = findViewById(R.id.directions_fab);

		bottom_layout.setTranslationY(bottom_layout.getHeight());
		directions_fab.setVisibility(View.INVISIBLE);

		TextView locationAddress = (TextView) findViewById(R.id.location_address);
		locationAddress.setText("");
	}

	private void setDirectionsButtonIcon(boolean iconClosed) {

		int icon_id;

		if (iconClosed) {
			icon_id = R.drawable.ic_close_white_24dp;
		}
		else if (locationRouteService.getMarkerType() == LocationRouteService.MarkerType.DESTINATION) {
			icon_id = R.drawable.ic_directions_car_white_24dp;
		}
		else if (locationRouteService.getMarkerType() == LocationRouteService.MarkerType.SAVED_SPOT) {
			icon_id = R.drawable.ic_directions_walk_white_24dp;
		}
		else {
			return;
		}

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.directions_fab);
		fab.setImageDrawable(getResources().getDrawable(icon_id));
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

					// Notify route service
					locationRouteService.notifyMapCleared();

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

	private RouteUpdateCallbackClient routeUpdateCallbackClient = new RouteUpdateCallbackClient() {

		@Override
		public void drawRoute(final PolylineOptions directionsPolylineOptions, final RouteUpdateResultCallbackClient client) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Polyline result = null;
					if (mMap != null) {
						// Directions for driving.

						// TODO: Switch like a sane person would, not with if true/false.
						if (false) {
							// Draw stroke.
							directionsPolylineOptions.width(Constants.DIRECTIONS_STROKE_WIDTH);
							directionsPolylineOptions.color(Constants.DIRECTIONS_STROKE_COLOR);
							result = mMap.addPolyline(directionsPolylineOptions);
							// Draw actual line.
							directionsPolylineOptions.width(Constants.DIRECTIONS_LINE_WIDTH);
							directionsPolylineOptions.color(Constants.DIRECTIONS_LINE_COLOR);
							result = mMap.addPolyline(directionsPolylineOptions);
						}
						else {
							// Draw circles.
							List<LatLng> points = directionsPolylineOptions.getPoints();
							LatLng pointA, pointB, intermPoint;
							Location locationA = new Location("");
							Location locationB = new Location("");
							Location intermLocation = new Location("");

							if (points.size() < 2)
								return;

							double padding = 0;
							double scaleFactor = Math.pow(2, Constants.DEFAULT_ZOOM - mMap.getCameraPosition().zoom) * Constants.SCALE_FACTOR_RECTIFIER;

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
									// Draw current circle.
									mMap.addCircle(new CircleOptions()
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
						}

						setDirectionsButtonIcon(true);
					}
					client.notifyPolylineResult(result);
				}
			});
		}

		@Override
		public void removeRoute(final Polyline directionsPolyline) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					directionsPolyline.remove();
					setDirectionsButtonIcon(false);
				}
			});
		}

		@Override
		public void drawMarker(final MarkerOptions markerOptions, final RouteUpdateResultCallbackClient client) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Marker result = null;
					if (mMap != null) {
						result = mMap.addMarker(markerOptions);
					}
					client.notifyMarkerResult(result);

					LatLng position = markerOptions.getPosition();
					openLocationInfoBar(new BasicLocation(position.latitude, position.longitude));
				}
			});
		}

		@Override
		public void removeMarker(final Marker marker) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					marker.remove();

					closeLocationInfoBar();
				}
			});
		}
	};

	private AddressResponseListener addressResponseListener = new AddressResponseListener() {
		@Override
		public void notifyAddressResponse(final String address) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView locationAddress = (TextView) findViewById(R.id.location_address);
					locationAddress.setText(address);
				}
			});
		}
	};

}
