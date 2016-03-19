package com.it.spot.maps;

import com.it.spot.common.ServiceManager;

import retrofit.RequestInterceptor;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class RetrofitInterceptor implements RequestInterceptor  {

	@Override
	public void intercept(RequestInterceptor.RequestFacade req) {

		if(ServiceManager.getInstance().getIdentityManager().hasToken()) {
			String token = ServiceManager.getInstance().getIdentityManager().getToken();
			req.addHeader("Authorization", "Bearer " + token);
		}
	}
}
