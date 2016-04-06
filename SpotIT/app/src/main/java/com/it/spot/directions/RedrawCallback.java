package com.it.spot.directions;

import com.google.android.gms.maps.model.CircleOptions;

import java.util.List;

/**
 * Created by Claudiu on 04-Apr-16.
 */
public interface RedrawCallback {
	void notifyRedraw(List<CircleOptions> circleOptionsList);
}
