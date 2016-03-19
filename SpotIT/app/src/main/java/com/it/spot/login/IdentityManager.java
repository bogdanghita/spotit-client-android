package com.it.spot.login;

/**
 * Created by Bogdan on 19/03/2016.
 */
public class IdentityManager {

	private UserInfo userInfo;

	public synchronized void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public synchronized UserInfo getUserInfo() {

		if (userInfo != null) {
			return userInfo.clone();
		}
		else {
			return null;
		}
	}

	public synchronized boolean hasUserInfo() {
		return userInfo != null;
	}

	public synchronized void clear() {
		userInfo = null;
	}
}
