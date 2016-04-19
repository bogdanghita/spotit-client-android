package com.it.spot.maps.main;

import com.it.spot.maps.location.BasicLocation;

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
