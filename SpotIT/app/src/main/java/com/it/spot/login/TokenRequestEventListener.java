package com.it.spot.login;

import com.google.android.gms.auth.UserRecoverableAuthException;

/**
 * Created by Bogdan on 19/03/2016.
 */
public interface TokenRequestEventListener {

	void requestFailed();

	void requestSucceeded(String token);
}
