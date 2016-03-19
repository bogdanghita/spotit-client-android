package com.example.claudiu.gmtest;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by Claudiu on 19-Mar-16.
 */
public interface HttpService {

//    @GET("/xml")
//    public void getDirections(@QueryMap Map<String, String> getDirectionsMessage, Callback<String> response);

    @GET("/maps/api/directions/xml")
    public String getDirections(@QueryMap Map<String, String> getDirectionsMessage);

    @GET("/maps/api/directions/xml")
    public String getDirections(@Query("origin") String origin, @Query("destination") String destination);
}
