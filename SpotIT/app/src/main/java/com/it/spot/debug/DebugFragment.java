package com.it.spot.debug;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.it.spot.common.Constants;


public class DebugFragment extends Fragment {

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View ret = super.onCreateView(inflater, container, savedInstanceState);

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onCreateView");

		return ret;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onActivityCreated");
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
	public void onDestroyView() {
		super.onDestroyView();

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();

		Log.d(Constants.APP + Constants.LIFECYCLE, this.getClass().getName() + " - " + "onDetach");
	}
}
