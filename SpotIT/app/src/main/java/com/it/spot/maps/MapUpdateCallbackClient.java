package com.it.spot.maps;

import java.util.List;

/**
 * Created by Bogdan on 20/03/2016.
 */
public interface MapUpdateCallbackClient {

	void updateMapStatus(List<PolygonUI> polygons);

	void notifyRequestFailure();
}
