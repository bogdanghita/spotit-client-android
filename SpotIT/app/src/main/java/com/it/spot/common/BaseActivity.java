package com.it.spot.common;

import android.os.Bundle;

import com.it.spot.debug.DebugActivity;

public class BaseActivity extends DebugActivity {

	protected ServiceManager mServiceManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mServiceManager = ServiceManager.getInstance();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// NOTE: This is called when login activity is closed. We do not want this...
		// Clearing this so that it is not kept in memory as a static object until the OS
		// decides to stop the process and clearRoute the RAM
//		mServiceManager.clearRoute();
	}
}
