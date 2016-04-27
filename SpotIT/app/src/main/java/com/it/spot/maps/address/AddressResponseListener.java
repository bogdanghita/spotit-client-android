package com.it.spot.maps.address;

/**
 * Created by Claudiu on 01-Apr-16.
 */
public interface AddressResponseListener {
	
	void notifyAddressResponse(String address);
	void notifyAddressFailure();
}
