package com.it.spot.maps;

import android.Manifest;
import android.app.Dialog;
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
import android.view.View;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.it.spot.R;
import com.it.spot.common.Constants;
import com.it.spot.common.ServiceManager;
import com.it.spot.login.IdentityActivity;
import com.it.spot.login.LoginActivity;
import com.it.spot.login.TokenRequestAsyncTask;
import com.it.spot.login.TokenRequestEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends IdentityActivity implements OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
		LocationListener, TokenRequestEventListener {

	private ActionBarDrawerToggle mDrawerToggle;

	private GoogleMap mMap;
	private GoogleApiClient mMapsGoogleApiClient;

	private Location mLastLocation;
	private String mLastUpdateTime;
	private LocationRequest mLocationRequest;
	private LatLngBounds cameraBounds;

	private boolean firstTimeLocation = true;

	// Bool to track whether the app is already resolving an error
	private boolean mResolvingError = false;

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

	}

	@Override
	public void onPause() {
		super.onPause();

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

				// TODO
//				// Setting camera position and requesting a map status update
//				serviceEngine.setCameraPosition(cameraBounds);
//				serviceEngine.requestMapStatusUpdate();
			}
		});

		if (checkAndRequestPermissionACCESS_FINE_LOCATION()) {
			enableLocation();
		}
	}

	void enableLocation() {
		mMap.setMyLocationEnabled(true);
	}

	@Override
	public void onConnected(Bundle bundle) {

		Log.d(Constants.APP + Constants.CONNECTION, "onConnected");

		mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mMapsGoogleApiClient);
		if(mLastLocation != null) {
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
		mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
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
}