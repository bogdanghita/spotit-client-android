package com.it.spot.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.it.spot.common.ServiceManager;
import com.it.spot.maps.MapsActivity;

public class LoginActivity extends IdentityActivity {

	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		configureSignInButton();
	}

	@Override
	public void onStart() {
		super.onStart();

		// Silent sign in
		OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);

		if (pendingResult.isDone()) {
			// There's immediate result available.
			handleSignInResult(pendingResult.get());
		}
		else {
			// There's no immediate result ready, displays some progress indicator and waits for the
			// async callback.
			showProgressIndicator();

			pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
				@Override
				public void onResult(GoogleSignInResult result) {

					handleSignInResult(result);

					hideProgressIndicator();
				}
			});
		}
	}

	public void configureSignInButton() {

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

		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

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
			ServiceManager.getInstance().getIdentityManager().setUserInfo(userInfo);

			Log.d(Constants.APP + Constants.SIGN_IN, "handleSignInResult(): " + email + ", " + name + ", " + photo);
			Log.d(Constants.APP + Constants.SIGN_IN, "handleSignInResult(): " + "id: " + id);
			Log.d(Constants.APP + Constants.SIGN_IN, "handleSignInResult(): " + "idToken: " + idToken);
			Log.d(Constants.APP + Constants.SIGN_IN, "handleSignInResult(): " + "serverAuthCode: " + serverAuthCode);

			// Login successful. Starting main activity
			startMainActivity();
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

	private void startMainActivity() {

		Intent intent = new Intent(this, MapsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
}