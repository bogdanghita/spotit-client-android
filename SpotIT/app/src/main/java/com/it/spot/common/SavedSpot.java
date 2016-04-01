package com.it.spot.common;

import android.location.Location;

import com.it.spot.maps.BasicLocation;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class SavedSpot {

	public boolean hasSavedSpot;
	public BasicLocation location;

	public SavedSpot(boolean hasSavedSpot, BasicLocation location) {
		this.hasSavedSpot = hasSavedSpot;
		this.location = location;
	}
}
