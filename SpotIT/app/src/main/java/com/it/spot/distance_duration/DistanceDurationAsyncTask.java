package com.it.spot.distance_duration;

import android.os.AsyncTask;
import android.util.Log;

import com.it.spot.common.Constants;
import com.it.spot.maps.BasicLocation;
import com.it.spot.services.HttpService;

import java.security.InvalidParameterException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Claudiu on 19-Apr-16.
 */
public class DistanceDurationAsyncTask extends AsyncTask<DistanceDurationOptions, Void, Void> {

	private DistanceDurationResponseListener mDistanceDurationResponseListener;
	private HttpService mHttpService;

	public DistanceDurationAsyncTask(DistanceDurationResponseListener distanceDurationResponseListener) {

		if (distanceDurationResponseListener == null)
			throw new InvalidParameterException();

		mDistanceDurationResponseListener = distanceDurationResponseListener;

		RestAdapter retrofit = new RestAdapter.Builder()
				.setEndpoint(Constants.GOOGLE_MAPS_API)
				.build();

		mHttpService = retrofit.create(HttpService.class);
	}

	@Override
	protected Void doInBackground(DistanceDurationOptions... params) {

		if (params == null || params.length != 1) {
			return null;
		}

		DistanceDurationOptions ddOptions = params[0];
		String origin = ddOptions.source.latitude + "," + ddOptions.source.longitude;
		String destination = ddOptions.destination.latitude + "," + ddOptions.destination.longitude;
		String mode = ddOptions.mode;

		mHttpService.getDistanceDuration(origin, destination, mode, new Callback<DistanceDuration>() {
			@Override
			public void success(DistanceDuration distanceDuration, Response response) {

				Log.d(Constants.DISTANCE_DURATION, "success");

				String distance;
				String duration;

				try {
					Row row = distanceDuration.getRows().get(0);
					Element element = row.getElements().get(0);

					distance = element.getDistance().getText();
					duration = element.getDuration().getText();

					Log.d(Constants.DISTANCE_DURATION, "[" + distance + "] - [" + duration + "]");
				} catch (Exception e) {
					Log.d(Constants.DISTANCE_DURATION, e.toString());
					return;
				}

				DistanceDurationData distanceDurationData = new DistanceDurationData(distance, duration);

				mDistanceDurationResponseListener.notifyAddressResponse(distanceDurationData);
			}

			@Override
			public void failure(RetrofitError error) {
				Log.d(Constants.DISTANCE_DURATION, "failure " + error.toString());
			}
		});

		return null;
	}
}