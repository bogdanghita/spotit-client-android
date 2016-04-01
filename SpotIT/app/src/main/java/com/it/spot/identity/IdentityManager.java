package com.it.spot.identity;

/**
 * Created by Bogdan on 19/03/2016.
 */
public class IdentityManager {

	private UserInfo userInfo;
	private String token;

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

	public synchronized void setToken(String token) {
		this.token = token;
	}

	public synchronized String getToken() {
		if (token != null) {
			return token;
		}
		else {
			return null;
		}
	}

	public synchronized void clear() {
		userInfo = null;
		token = null;
	}
}
