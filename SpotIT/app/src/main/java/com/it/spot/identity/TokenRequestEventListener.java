package com.it.spot.identity;

/**
 * Created by Bogdan on 19/03/2016.
 */
public interface TokenRequestEventListener {

	void requestFailed();

	void requestSucceeded(String token);
}
