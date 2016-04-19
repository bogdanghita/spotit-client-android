package com.it.spot.services;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.it.spot.common.Constants;
import com.it.spot.maps.main.MapUpdateCallbackClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class ServerAPI {

    MapUpdateCallbackClient updateClient;

    HttpService httpService;

    List<List<List<PolygonUI>>> rawHystoryData = new ArrayList<>();

    public static final int NUMBER_OF_DAYS = 7;
    public static int CURRENT_DAYS = 1;
    public static boolean ready = false;

    public ServerAPI(MapUpdateCallbackClient updateClient) {

        this.updateClient = updateClient;

        RestAdapter retrofit = new RestAdapter.Builder()
                .setEndpoint(Constants.SERVER_ADDRESS)
                .setRequestInterceptor(new RetrofitInterceptor())
                .build();

        httpService = retrofit.create(HttpService.class);
    }

    public void getMapStatus(LatLng latLng, double radius) {

        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        Map<String, String> request = new HashMap<>();
        request.put("latitude", String.valueOf(latitude));
        request.put("longitude", String.valueOf(longitude));
        request.put("radius", String.valueOf(radius));

        httpService.getMap(request, new Callback<List<PolygonDB>>() {

            @Override
            public void success(List<PolygonDB> polygons, Response response) {

                List<PolygonUI> uiPolygons = new ArrayList<>();

                for (PolygonDB polygon : polygons) {

//					Log.d(Constants.APP + Constants.SERVER_API, String.valueOf(polygon.getColor()));
                    uiPolygons.add(polygon.toPolygonUI());
                }

                // Calling callback method to return result
                updateClient.updateMapStatus(uiPolygons);
            }

            @Override
            public void failure(RetrofitError error) {

                Log.d(Constants.APP + Constants.SERVER_API, error.toString());

                if (error.getResponse() == null) {
                    return;
                }

                if (error.getResponse().getStatus() == Constants.HTTP_UNAUTHORIZED) {
                    updateClient.notifyRequestFailure();
                }
            }
        });
    }

    public void postStatus(LatLng latLng, int status) {

        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        Message.StatusMessage statusMessage = new Message.StatusMessage(latitude, longitude, status);

        httpService.postStatus(statusMessage, new Callback<Message.StatusMessageID>() {

            @Override
            public void success(Message.StatusMessageID statusMessageID, Response response) {

                Log.d(Constants.APP + Constants.SERVER_API, String.valueOf(statusMessageID.latitude) +
                        " " + String.valueOf(statusMessageID.longitude) + " " + String.valueOf(statusMessageID.status));
            }

            @Override
            public void failure(RetrofitError error) {

                Log.d(Constants.APP + Constants.SERVER_API, error.toString());

                if (error.getResponse() == null) {
                    return;
                }

                if (error.getResponse().getStatus() == Constants.HTTP_UNAUTHORIZED) {
                    updateClient.notifyRequestFailure();
                }
            }
        });
    }

    public void getHistory(LatLng latLng, int daysDelay, int hour, int interval) {

        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        Map<String, String> request = new HashMap<>();
        request.put("latitude", String.valueOf(latitude));
        request.put("longitude", String.valueOf(longitude));
        request.put("day", String.valueOf(-daysDelay));
        request.put("hour", String.valueOf(hour));
        request.put("interval", String.valueOf(interval));

        httpService.getMap(request, new Callback<List<PolygonDB>>() {

            @Override
            public void success(List<PolygonDB> polygons, Response response) {

                List<PolygonUI> uiPolygons = new ArrayList<>();

                for (PolygonDB polygon : polygons) {

//					Log.d(Constants.APP + Constants.SERVER_API, String.valueOf(polygon.getColor()));
                    uiPolygons.add(polygon.toPolygonUI());
                }

                // Calling callback method to return result
                updateClient.updateMapStatus(uiPolygons);
            }

            @Override
            public void failure(RetrofitError error) {

                Log.d(Constants.APP + Constants.SERVER_API, error.toString());

                if (error.getResponse() == null) {
                    return;
                }

                if (error.getResponse().getStatus() == Constants.HTTP_UNAUTHORIZED) {
                    updateClient.notifyRequestFailure();
                }
            }
        });
    }
}
