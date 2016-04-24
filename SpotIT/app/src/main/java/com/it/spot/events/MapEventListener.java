package com.it.spot.events;

import android.util.Log;

import com.it.spot.common.Constants;

/**
 * Created by Bogdan on 19/04/2016.
 */
public abstract class MapEventListener implements EventListener {

	@Override
	public void notify(BaseEvent event) {

		Log.d(Constants.APP + Constants.EVENT, "Notify event: " + event.getClass());

		/**
		 * NOTE: This should be done with visitor pattern. No time for that now...
		 */
		if (event instanceof SetMarkerEvent) {
			notifySetMarker((SetMarkerEvent) event);
		}
		else if (event instanceof RemoveMarkerEvent) {
			notifyRemoveMarker((RemoveMarkerEvent) event);
		}
		else if (event instanceof DrawRouteEvent) {
			notifyDisplayRoute((DrawRouteEvent) event);
		}
		else if (event instanceof RemoveRouteEvent) {
			notifyHideRoute((RemoveRouteEvent) event);
		}
		else if (event instanceof SpotsMapEvent) {
			notifySpotsMap((SpotsMapEvent) event);
		}
		else if (event instanceof LocationChangeEvent) {
			notifyLocationChange((LocationChangeEvent) event);
		}
		else if (event instanceof CameraChangeEvent) {
			notifyCameraChange((CameraChangeEvent) event);
		}
	}

	public abstract void notifySetMarker(SetMarkerEvent event);

	public abstract void notifyRemoveMarker(RemoveMarkerEvent event);

	public abstract void notifyDisplayRoute(DrawRouteEvent event);

	public abstract void notifyHideRoute(RemoveRouteEvent event);

	public abstract void notifySpotsMap(SpotsMapEvent event);

	public abstract void notifyLocationChange(LocationChangeEvent event);

	public abstract void notifyCameraChange(CameraChangeEvent event);
}
