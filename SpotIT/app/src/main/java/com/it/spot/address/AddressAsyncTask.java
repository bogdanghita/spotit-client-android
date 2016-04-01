package com.it.spot.address;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.it.spot.common.Constants;
import com.it.spot.services.HttpService;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Claudiu on 01-Apr-16.
 */
public class AddressAsyncTask extends AsyncTask<LatLng, Void, Void> {

    private AddressListener mAddressListener;
    private HttpService mHttpService;

    public AddressAsyncTask(AddressListener addressListener) {
        mAddressListener = addressListener;

        RestAdapter retrofit = new RestAdapter.Builder()
                .setEndpoint(Constants.GOOGLE_MAPS_API)
                .build();

        mHttpService = retrofit.create(HttpService.class);
    }

    @Override
    protected Void doInBackground(LatLng... params) {

        if (params == null || params.length ==0){
            return null;
        }

        String latlng = params[0].latitude + "," + params[0].longitude;

        mHttpService.getAddress(latlng, new Callback<Address>() {
            @Override
            public void success(Address address, Response response) {

                Log.d(Constants.ADDRESS, "success");

                String formattedAddress;
                List<Result> results = address.getResults();

                if (results == null || results.size() == 0)
                    return;

                formattedAddress = results.get(0).getFormattedAddress();

                mAddressListener.notifyAddressResponse(formattedAddress);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(Constants.ADDRESS, "failure "+error.toString());
            }
        });

        return null;
    }
}
