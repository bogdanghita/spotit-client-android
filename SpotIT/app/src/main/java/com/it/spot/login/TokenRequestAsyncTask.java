package com.it.spot.login;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.it.spot.common.Constants;

import java.io.IOException;

/**
 * Created by Bogdan on 19/03/2016.
 */
public class TokenRequestAsyncTask extends AsyncTask<Void, Void, Void> {

	Activity mActivity;
	String mEmail;
	String mScope;
	TokenRequestEventListener mTokenRequestEventListener;

	public TokenRequestAsyncTask(Activity activity, TokenRequestEventListener tokenRequestEventListener,
	                             String email, String scope) {

		this.mActivity = activity;
		this.mScope = scope;
		this.mEmail = email;
		this.mTokenRequestEventListener = tokenRequestEventListener;
	}

	@Override
	protected Void doInBackground(Void... params) {

		String result = getToken();

		if(result != null) {
			mTokenRequestEventListener.requestSucceeded(result);
		}
		else {
			mTokenRequestEventListener.requestFailed();
		}

		return null;
	}

	private String getToken() {

		try {
			return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
		}
		catch (UserRecoverableAuthException userAuthEx) {
			// This means that the app hasn't been authorized by the user for access
			// to the scope, so we're going to have to fire off the (provided) Intent
			// to arrange for that. But we only want to do this once. Multiple
			// attempts probably mean the user said no.
			mActivity.startActivityForResult(userAuthEx.getIntent(), Constants.USER_RECOVERABLE_AUTH_EXCEPTION);
		}
		catch (GoogleAuthException | IOException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.TOKEN, "Unable to get token.");
		}
		return null;
	}
}
