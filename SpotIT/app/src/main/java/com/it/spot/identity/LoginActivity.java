package com.it.spot.identity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.it.spot.R;
import com.it.spot.common.Constants;
import com.it.spot.intro.IntroActivity;
import com.it.spot.maps.main.MapsActivity;
import com.it.spot.services.FileService;
import com.it.spot.services.InternetConnectionCallbackListener;
import com.it.spot.services.InternetConnectionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class LoginActivity extends IdentityActivity implements InternetConnectionCallbackListener {

	private ProgressDialog mProgressDialog;

	InternetConnectionService mInternetConnectionService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		configureSignInButton();

		mInternetConnectionService = new InternetConnectionService(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Check internet connection
		if (!mInternetConnectionService.hasInternet()) {
			// Subscribe so that we get to know when we have internet
			mInternetConnectionService.subscribe(this);
			return;
		}
		// Unsubscribe if we have internet
		mInternetConnectionService.unsubscribe(this);

		// If internet connection is there start login
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				startSilentSignIn();
			}
		}, Constants.INITIAL_LOGIN_DELAY);
	}

	@Override
	public void onResume() {
		super.onResume();

		mInternetConnectionService.startConnectionCheckLoop();
	}

	@Override
	public void onPause() {
		super.onPause();

		mInternetConnectionService.stopConnectionCheckLoop();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void startSilentSignIn() {

		// Silent sign in
		OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mIdentityGoogleApiClient);

		if (pendingResult.isDone()) {
			// There's immediate result available.
			handleSignInResult(pendingResult.get());

			Log.d(Constants.SIGN_IN, "pendingResult DONE");
		}
		else {
			// There's no immediate result ready, displays some progress indicator and waits for the
			// async callback.
			pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
				@Override
				public void onResult(GoogleSignInResult result) {

					handleSignInResult(result);
				}
			});

			Log.d(Constants.SIGN_IN, "pendingResult NOT DONE");
		}
	}

	private void configureSignInButton() {

		SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setSize(SignInButton.SIZE_STANDARD);
		signInButton.setScopes(gso.getScopeArray());

		configureSignInButtonText(signInButton, "Sign in with Google");

		signInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonSignIn(v);
			}
		});

		signInButton.setVisibility(View.INVISIBLE);
	}

	private void showSignInButton() {

		SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setVisibility(View.VISIBLE);
	}

	private void configureSignInButtonText(SignInButton signInButton, String buttonText) {

		for (int i = 0; i < signInButton.getChildCount(); i++) {
			View v = signInButton.getChildAt(i);

			if (v instanceof TextView) {
				TextView tv = (TextView) v;
//				tv.setTextSize(15);
//				tv.setTypeface(null, Typeface.NORMAL);
				tv.setText(buttonText);
				return;
			}
		}
	}

	public void buttonSignIn(View v) {

		Log.d(Constants.APP + Constants.SIGN_IN, "buttonSignIn()");

		if (permission_GET_ACCOUNTS()) {
			startSignIn();
		}
	}

	public void startSignIn() {

//		showProgressIndicator();

		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mIdentityGoogleApiClient);
		startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

//		hideProgressIndicator();

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent()
		if (requestCode == Constants.RC_SIGN_IN) {

			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			handleSignInResult(result);
		}
	}

	private void handleSignInResult(GoogleSignInResult result) {

		Log.d(Constants.APP + Constants.SIGN_IN, "handleSignInResult(): " + result.isSuccess());

		if (result.isSuccess()) {

			GoogleSignInAccount acct = result.getSignInAccount();

			String email = acct.getEmail();
			String name = acct.getDisplayName();
			Uri photo = acct.getPhotoUrl();
			String id = acct.getId();
			String idToken = acct.getIdToken();
			String serverAuthCode = acct.getServerAuthCode();

			String stringPhoto = photo != null ? photo.toString() : "";
			UserInfo userInfo = new UserInfo(name, email, stringPhoto);
			mServiceManager.getIdentityManager().setUserInfo(userInfo);

			Log.d(Constants.APP + Constants.SIGN_IN, "handleSignInResult(): " + email + ", " + name + ", " + photo);
			Log.d(Constants.APP + Constants.SIGN_IN, "handleSignInResult(): " + "id: " + id);
			Log.d(Constants.APP + Constants.SIGN_IN, "handleSignInResult(): " + "idToken: " + idToken);
			Log.d(Constants.APP + Constants.SIGN_IN, "handleSignInResult(): " + "serverAuthCode: " + serverAuthCode);

			// Login successful. Starting main activity
			proceedToApp(userInfo);
		}
		else {

			showSignInButton();
		}
	}

	private void showProgressIndicator() {

		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Loading...");
			mProgressDialog.setIndeterminate(true);
		}

		mProgressDialog.show();
	}

	private void hideProgressIndicator() {

		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.hide();
		}
	}

	private void proceedToApp(UserInfo userInfo) {

		if (userInfo == null || userInfo.getEmail() == null) {
			return;
		}

		List<String> userList = new FileService(this).readUserListFile(Constants.USER_LIST_FILE);

		if (userList == null) {
			userList = new LinkedList<>();
		}

		handleFirstSignIn(userInfo.getEmail(), userList);
	}

	private void handleFirstSignIn(String email, List<String> userList) {

		if (userList.contains(email)) {
			startMainActivity();
			return;
		}

		userList.add(email);
		new FileService(this).writeUserListFile(userList, Constants.USER_LIST_FILE);

		startIntroActivity();
	}

	private void startMainActivity() {

		Intent intent = new Intent(this, MapsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		// Disable activity end transition
		overridePendingTransition(0, 0);
	}

	private void startIntroActivity() {

		Intent intent = new Intent(this, IntroActivity.class);
		startActivity(intent);
	}

// -------------------------------------------------------------------------------------------------
// PERMISSIONS
// -------------------------------------------------------------------------------------------------

	private boolean permission_GET_ACCOUNTS() {

		List<String> listPermissionsNeeded = new ArrayList<>();

		int permissionGetAccounts = ContextCompat.checkSelfPermission(this,
				Manifest.permission.GET_ACCOUNTS);

		if (permissionGetAccounts != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
		}

		if (!listPermissionsNeeded.isEmpty()) {
			ActivityCompat.requestPermissions(this,
					listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
					Constants.REQ_GET_ACCOUNTS);
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

		switch (requestCode) {
			case Constants.REQ_GET_ACCOUNTS: {

				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					startSignIn();
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
// INTERNET CONNECTION CHECK
// -------------------------------------------------------------------------------------------------

	@Override
	public void notifyResult(boolean connected) {

		if (connected) {

			// Unsubscribe
			mInternetConnectionService.unsubscribe(this);

			// Start login
			startSilentSignIn();
		}
	}
}
