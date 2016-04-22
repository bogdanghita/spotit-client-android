package com.it.spot.events;

import android.view.View;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by Bogdan on 22/04/2016.
 */
public interface MapItemsProvider {

	GoogleMap getMap();

	View getView(int id);
}
