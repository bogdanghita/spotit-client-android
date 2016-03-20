package com.it.spot.common;

import android.location.Location;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class SavedSpot {

	public boolean hasSavedSpot;
	public Location location;

	public SavedSpot(boolean hasSavedSpot, Location location) {
		this.hasSavedSpot = hasSavedSpot;
		this.location = location;
	}
}
