package com.it.spot.debug;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import com.it.spot.common.Constants;


public class DebugActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onCreate");
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onRestoreInstanceState");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onSaveInstanceState");
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onDestroy");
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onBackPressed");
	}

	@Override
	public boolean onMenuOpened(final int featureId, final Menu menu) {
		boolean result = super.onMenuOpened(featureId, menu);

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onMenuOpened");

		return result;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onConfigurationChanged");
	}
}
