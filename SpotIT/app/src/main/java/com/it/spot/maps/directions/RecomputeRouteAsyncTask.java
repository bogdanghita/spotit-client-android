package com.it.spot.maps.directions;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * Created by Claudiu on 04-Apr-16.
 */
public class RecomputeRouteAsyncTask extends AsyncTask<List<LatLng>, Void, Void> {

	private float mZoom;
	private RedrawCallback mRedrawCallback;

	public RecomputeRouteAsyncTask(RedrawCallback redrawCallback, float zoom) {
		if(redrawCallback == null)
			throw new InvalidParameterException();

		mRedrawCallback = redrawCallback;
		mZoom = zoom;
	}

	@Override
	protected Void doInBackground(List<LatLng>... params) {
		List<CircleOptions> result = DirectionsAsyncTask.getWalkingDirections(params[0], mZoom);
		mRedrawCallback.notifyRedraw(result);
		return null;
	}
}
