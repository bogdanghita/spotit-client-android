package com.it.spot.login;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.it.spot.common.BaseActivity;
import com.it.spot.common.Constants;

public class IdentityActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

	protected GoogleApiClient mGoogleApiClient;
	protected GoogleSignInOptions gso;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Configure sign-in to request the user's ID, email address, and basic profile.
		// ID and basic profile are included in DEFAULT_SIGN_IN
		gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestEmail()
				.build();

		// Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		Log.d(Constants.APP + Constants.SIGN_IN, "onConnectionFailed()");
	}
}
