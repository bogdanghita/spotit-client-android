package com.it.spot.maps.main;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.it.spot.R;
import com.it.spot.common.ServiceManager;
import com.it.spot.events.MapItemsProvider;
import com.it.spot.maps.address.AddressAsyncTask;
import com.it.spot.maps.address.AddressResponseListener;
import com.it.spot.maps.location.BasicLocation;

/**
 * Created by Bogdan on 25/04/2016.
 */
public class UiItemsController {

	private Context mContext;

	private MapItemsManager mMapItemsManager;

	private MapItemsProvider mMapItemsProvider;
	private UiController mUiController;

	public UiItemsController(Context context, MapItemsProvider mapItemsProvider, UiController uiController) {

		mContext = context;

		mMapItemsProvider = mapItemsProvider;
		mUiController = uiController;

		mMapItemsManager = ServiceManager.getInstance().getMapItemsManager();
	}

	public void closeLocationInfoBar() {

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {

				View bottom_layout = mMapItemsProvider.getView(R.id.location_info_bar);
				View directions_fab = mMapItemsProvider.getView(R.id.directions_fab);

				bottom_layout.setTranslationY(bottom_layout.getHeight());
				directions_fab.setVisibility(View.INVISIBLE);

				clearLocationInfoBarItems();
			}
		});
	}

	private void clearLocationInfoBarItems() {

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {

				TextView locationTitle = (TextView) mMapItemsProvider.getView(R.id.location_title);
				locationTitle.setText("");

				TextView locationAddress = (TextView) mMapItemsProvider.getView(R.id.location_address);
				locationAddress.setText("");

				TextView destinationTime = (TextView) mMapItemsProvider.getView(R.id.destination_time);
				destinationTime.setText("");
			}
		});
	}

	public void setDirectionsButtonIcon(boolean iconClosed) {

		final int icon_id;

		MarkerData markerData = mMapItemsManager.getMarkerData();

		if (iconClosed) {
			icon_id = R.drawable.ic_close_white_24dp;
		}
		else if (markerData == null) {
			return;
		}
		else if (markerData.markerType == MapItemsService.MarkerType.DESTINATION) {
			icon_id = R.drawable.ic_directions_car_white_24dp;
		}
		else if (markerData.markerType == MapItemsService.MarkerType.SAVED_SPOT) {
			icon_id = R.drawable.ic_directions_walk_white_24dp;
		}
		else {
			return;
		}

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {
				FloatingActionButton fab = (FloatingActionButton) mMapItemsProvider.getView(R.id.directions_fab);
				fab.setImageDrawable(mContext.getResources().getDrawable(icon_id));
			}
		});
	}

	public void openLocationInfoBar(final BasicLocation location, final AddressResponseListener listener) {

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {

				// Clear items first (useful when location info bar was not closed)
				clearLocationInfoBarItems();

				View bottom_layout = mMapItemsProvider.getView(R.id.location_info_bar);
				View directions_fab = mMapItemsProvider.getView(R.id.directions_fab);

				bottom_layout.setTranslationY(0);
				directions_fab.setVisibility(View.VISIBLE);

				AddressAsyncTask addressAsyncTask = new AddressAsyncTask(listener);
				addressAsyncTask.execute(location);

				// Set appropriate title
				setLocationInfoBarTitle();
			}
		});
	}

	private void setLocationInfoBarTitle() {

		final String text;

		MarkerData markerData = mMapItemsManager.getMarkerData();
		if (markerData == null) {
			return;
		}

		if (markerData.markerType == MapItemsService.MarkerType.DESTINATION) {
			text = mContext.getResources().getString(R.string.location_info_bar_title_destination);
		}
		else if (markerData.markerType == MapItemsService.MarkerType.SAVED_SPOT) {
			text = mContext.getString(R.string.location_info_bar_title_saved_spot);
		}
		else {
			text = "";
		}

		mUiController.doRunOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView tv = (TextView) mMapItemsProvider.getView(R.id.location_title);
				tv.setText(text);
			}
		});
	}
}
