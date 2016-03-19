package com.it.spot.maps;

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
	public List<List<PolygonDB>> getDay(@Query("number") String number);

	@POST("/api/spot/status")
	public void postStatus(@Body Message.StatusMessage statusMessage, Callback<Message.StatusMessageID> response);
}
