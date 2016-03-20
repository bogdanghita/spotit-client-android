package com.it.spot.maps;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.it.spot.R;
import com.it.spot.common.Constants;
import com.it.spot.common.SavedSpot;
import com.it.spot.common.ServiceManager;
import com.it.spot.directions.DirectionsAsyncTask;
import com.it.spot.directions.DirectionsListener;
import com.it.spot.directions.RouteOptions;
import com.it.spot.identity.IdentityActivity;
import com.it.spot.identity.IdentityManager;
import com.it.spot.identity.ImageLoaderAsyncTask;
import com.it.spot.identity.LoginActivity;
import com.it.spot.identity.TokenRequestAsyncTask;
import com.it.spot.identity.TokenRequestEventListener;
import com.it.spot.identity.UserInfo;
import com.it.spot.services.PolygonUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends IdentityActivity implements OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
		LocationListener, TokenRequestEventListener, MapUpdateCallbackClient, DirectionsListener {

	private ActionBarDrawerToggle mDrawerToggle;

	private GoogleMap mMap;
	private GoogleApiClient mMapsGoogleApiClient;

	private Location mLastLocation;
	private String mLastUpdateTime;
	private LocationRequest mLocationRequest;
	private LatLngBounds cameraBounds;

	private Location mSavedSpot = null;
	private Marker mSavedMarker = null;
	private PolylineOptions mDirectionsPolylineOptions = null;
	private Polyline mDirectionsPolyline = null;

	private boolean firstTimeLocation = true;

	// Bool to track whether the app is already resolving an error
	private boolean mResolvingError = false;

	private MapUpdateService mapUpdateService;

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

		buildGoogleApiClient();

		createLocationRequest();

		createUserProfile();

		loadSavedSpot();

		toggleLeaveSaveSpot();

		mapUpdateService = new MapUpdateService(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(Constants.STATE_RESOLVING_ERROR, mResolvingError);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (!mServiceManager.getIdentityManager().hasToken()) {
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

		if (checkAndRequestPermissionACCESS_FINE_LOCATION()) {
			enableLocation();
		}

		drawSavedSpot();
	}

	void enableLocation() {
		mMap.setMyLocationEnabled(true);
	}

	@Override
	public void onConnected(Bundle bundle) {

		Log.d(Constants.APP + Constants.CONNECTION, "onConnected");

		mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mMapsGoogleApiClient);
		if (mLastLocation != null) {
			onLocationChanged(mLastLocation);
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

		mLastLocation = location;
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

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
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

		if (identityManager.hasUserInfo()) {

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
		Log.d(Constants.APP + Constants.LOCATION, mLastUpdateTime + ": " + mLastLocation.getLatitude() +
				", " + mLastLocation.getLongitude());
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

		if (!mServiceManager.getIdentityManager().hasUserInfo()) {
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

		Log.d(Constants.APP + Constants.TOKEN, "Token obtained successfully: " + token);
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
		dialogFragment.show(getSupportFragmentManager(), "errordialog");
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
// SPOTS MAP
// -------------------------------------------------------------------------------------------------

	@Override
	public void updateMapStatus(final List<PolygonUI> polygons) {

		Log.d(Constants.APP + Constants.MAP_UPDATE, "Received update map status. Polygons: " + polygons.size());

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				// Clearing polygons
				mMap.clear();

				drawSavedSpot();
				drawDirectionsPolyline();

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
// SIDEBAR OPTIONS
// -------------------------------------------------------------------------------------------------

	void toggleLeaveSaveSpot() {

		RelativeLayout buttonSaveSpot = (RelativeLayout)findViewById(R.id.item_save_spot);
		RelativeLayout buttonLeaveSpot = (RelativeLayout)findViewById(R.id.item_leave_spot);

		if(mSavedSpot == null) {
			buttonLeaveSpot.setVisibility(View.GONE);
			buttonSaveSpot.setVisibility(View.VISIBLE);
		}
		else {
			buttonLeaveSpot.setVisibility(View.VISIBLE);
			buttonSaveSpot.setVisibility(View.GONE);
		}
	}

	void loadSavedSpot() {

		SavedSpot savedSpot = readSavedSpotFile(Constants.SAVED_SPOT_FILE);

		if (savedSpot != null && savedSpot.hasSavedSpot) {
			mSavedSpot = savedSpot.location;
		}
	}

	void drawSavedSpot() {

		if (mSavedMarker != null) {
			mSavedMarker.remove();
		}

		if (mSavedSpot == null) {
			return;
		}

		LatLng point = new LatLng(mSavedSpot.getLatitude(), mSavedSpot.getLongitude());
		mSavedMarker = mMap.addMarker(new MarkerOptions().position(point).
				title("Your car is here").
				snippet("Click on marker for directions"));
		mSavedMarker.showInfoWindow();

//		mMap.moveCamera(CameraUpdateFactory.newLatLng(point));

		mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				if(marker.getTitle().equals("Your car is here")) {
					drawRouteToSavedSpotButton();
				}
				return false;
			}
		});
	}

	public void buttonSaveSpot(View view) {

		SavedSpot spot = new SavedSpot(true, mLastLocation);

		writeSavedSpotFile(spot, Constants.SAVED_SPOT_FILE);

		mSavedSpot = spot.location;
		drawSavedSpot();

		toggleLeaveSaveSpot();

		toggleNavigationDrawer();
	}

	public void buttonLeaveSpot(View view) {

		SavedSpot spot = new SavedSpot(false, mLastLocation);

		writeSavedSpotFile(spot, Constants.SAVED_SPOT_FILE);

		mSavedSpot = null;
		if(mSavedMarker != null) {
			mSavedMarker.remove();
			mSavedMarker = null;
		}

		mDirectionsPolylineOptions = null;
		if(mDirectionsPolyline != null) {
			mDirectionsPolyline.remove();
			mDirectionsPolyline = null;
		}

		toggleLeaveSaveSpot();

		toggleNavigationDrawer();
	}

	private void writeSavedSpotFile(SavedSpot spot, String filename) {

		try {
			FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);

			Gson gson = new Gson();
			String jsonString = gson.toJson(spot);

			fos.write(jsonString.getBytes());

			fos.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.SAVED_SPOT, "Error writing saved spot to file: " + filename);
		}
	}

	private SavedSpot readSavedSpotFile(String filename) {

		SavedSpot result = null;

		try {
			FileInputStream fis = openFileInput(filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

			Gson gson = new Gson();
			result = gson.fromJson(reader, SavedSpot.class);

			reader.close();
			fis.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.SAVED_SPOT, "Error reading saved spot from file: " + filename);
		}

		return result;
	}

	public void buttonHistory(View view) {

	}

	public void buttonHelp(View view) {


	}

// -------------------------------------------------------------------------------------------------
// ON SCREEN BUTTONS
// -------------------------------------------------------------------------------------------------

	public void spotItButtonFree(View view) {

		mapUpdateService.sendMapStatus(mLastLocation, Constants.STATUS_GREEN_TEXT);
	}

	public void spotItButtonMedium(View view) {

		mapUpdateService.sendMapStatus(mLastLocation, Constants.STATUS_YELLOW_TEXT);
	}

	public void spotItButtonFull(View view) {

		mapUpdateService.sendMapStatus(mLastLocation, Constants.STATUS_RED_TEXT);
	}

	public void drawRouteToSavedSpotButton() {

		if(mSavedSpot == null) {
			return;
		}

		// Get directions from source to destination as PolylineOptions.
		DirectionsAsyncTask directions = new DirectionsAsyncTask(this);

		LatLng source = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
		LatLng destination = new LatLng(mSavedSpot.getLatitude(), mSavedSpot.getLongitude());

		directions.execute(new RouteOptions(source, destination, Constants.MODE_WALKING));
	}

	void drawDirectionsPolyline() {

		if(mDirectionsPolyline != null) {
			mDirectionsPolyline.remove();
		}

		if(mDirectionsPolylineOptions == null) {
			return;
		}

		mDirectionsPolyline = mMap.addPolyline(mDirectionsPolylineOptions);
	}

	@Override
	public void notifyDirectionsResponse(final PolylineOptions polylineOptions) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				mDirectionsPolylineOptions = polylineOptions;
				drawDirectionsPolyline();
			}
		});
	}
}
