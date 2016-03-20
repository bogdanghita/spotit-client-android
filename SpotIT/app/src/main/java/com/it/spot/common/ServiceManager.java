package com.it.spot.common;

import com.it.spot.identity.IdentityManager;

/**
 * Created by Bogdan on 19/03/2016.
 */
public class ServiceManager {

	private static ServiceManager instance = null;

	private IdentityManager identityManager;

	private ServiceManager() {

		identityManager = new IdentityManager();
	}

	public synchronized static ServiceManager getInstance() {

		if (instance == null) {
			instance = new ServiceManager();
		}
		return instance;
	}

	public IdentityManager getIdentityManager() {
		return identityManager;
	}

	public void clear() {

		identityManager.clear();
	}
}
