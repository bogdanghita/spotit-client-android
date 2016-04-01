package com.it.spot.services;

import com.it.spot.common.ServiceManager;

import retrofit.RequestInterceptor;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class RetrofitInterceptor implements RequestInterceptor  {

	@Override
	public void intercept(RequestInterceptor.RequestFacade req) {

		String token = ServiceManager.getInstance().getIdentityManager().getToken();
		if(token != null) {
			req.addHeader("Authorization", "Bearer " + token);
		}
	}
}
