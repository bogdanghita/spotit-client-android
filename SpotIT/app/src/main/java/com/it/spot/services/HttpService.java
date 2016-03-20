package com.it.spot.services;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by Bogdan on 20/03/2016.
 */
public interface HttpService {

	@GET("/api/spot")
	public void getMap(@QueryMap Map<String, String> getMapMessage, Callback<List<PolygonDB>> response);

	@GET("/api/spot/day")
	public void getHistory(@QueryMap Map<String, String> getHistoryMessage, Callback<List<PolygonDB>> response);

	@POST("/api/spot/status")
	public void postStatus(@Body Message.StatusMessage statusMessage, Callback<Message.StatusMessageID> response);
}
