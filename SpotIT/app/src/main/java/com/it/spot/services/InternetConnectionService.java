package com.it.spot.services;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;

import com.it.spot.R;
import com.it.spot.common.Constants;
import com.it.spot.threading.Event;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Bogdan on 27/04/2016.
 */
public class InternetConnectionService {

	public final static int INTERVAL_NORMAL = 3000;
	public final static int INTERVAL_FAST = 1000;

	private List<InternetConnectionCallbackListener> listeners;

	private Activity runningActivity;

	private ScheduledExecutorService timer;

	private AlertDialog mAlertDialog;

	private final Object dialogOpenSync = new Object();
	private boolean dialogOpen;

	private String mInternetCheckAddress;

	private final Object syncObject = new Object();

	public InternetConnectionService(Activity activity) {

		runningActivity = activity;

		listeners = new ArrayList<>();

		dialogOpen = false;

		mInternetCheckAddress = activity.getResources().getString(R.string.internet_check_address);

		createDialog();
	}

	public void subscribe(InternetConnectionCallbackListener l) {
		listeners.add(l);
	}

	public void unsubscribe(InternetConnectionCallbackListener l) {
		listeners.remove(l);
	}

	private void notifyListeners(boolean result) {

		for (InternetConnectionCallbackListener l : listeners) {
			l.notifyResult(result);
		}
	}

	public void startConnectionCheckLoop() {

		synchronized (syncObject) {

			if (timer != null) {
				timer.shutdown();
			}

			timer = Executors.newSingleThreadScheduledExecutor();
			timer.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {

					boolean result = hasInternet();

					notifyListeners(result);

					if (!result) {
						openDialog();
//					taskScheduler.setInterval(INTERVAL_FAST);
						return;
					}

					synchronized (dialogOpenSync) {
						if (dialogOpen) {
							closeDialog();
//						taskScheduler.setInterval(INTERVAL_NORMAL);
						}
					}
				}
			}, 0, INTERVAL_NORMAL, TimeUnit.MILLISECONDS);
		}
	}

	public void stopConnectionCheckLoop() {

		synchronized (syncObject) {

			if (timer != null) {
				timer.shutdown();
				timer = null;
			}
		}
	}

	public boolean hasInternet() {

		final boolean[] result = new boolean[1];

		final Event eventHandler = new Event();

		// Check connection on separate thread because it does network actions
		new Thread(new Runnable() {
			@Override
			public void run() {

				result[0] = isNetworkConnected() && isInternetAvailable();

				eventHandler.set();
			}
		}).start();

		try {
			eventHandler.doWait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.EVENT, "Interrupted while waiting for internet connection check.");
		}

		return result[0];
	}

	private boolean isNetworkConnected() {

		ConnectivityManager connectivityManager;

		if (runningActivity == null) {
			return false;
		}

		connectivityManager = (ConnectivityManager) runningActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

		return connectivityManager.getActiveNetworkInfo() != null;
	}

	private boolean isInternetAvailable() {

		InetAddress inetAddress;

		try {
			inetAddress = InetAddress.getByName(mInternetCheckAddress);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}

		if (inetAddress == null || inetAddress.getHostAddress().equals("")) {
			return false;
		}

		return true;
	}

	private void createDialog() {

		AlertDialog.Builder alertDialogBuilder;

		alertDialogBuilder = new AlertDialog.Builder(runningActivity);

		String noInternetMessage = runningActivity.getResources().getString(R.string.no_internet_message);
		alertDialogBuilder.setMessage(noInternetMessage);

		alertDialogBuilder.setCancelable(false);

		alertDialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)

					// Close application on back press
					if (runningActivity != null) {
						runningActivity.finish();
					}

				return false;
			}
		});

		alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				synchronized (dialogOpenSync) {
					dialogOpen = false;
				}
			}
		});

		alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				synchronized (dialogOpenSync) {
					dialogOpen = false;
				}
			}
		});

		mAlertDialog = alertDialogBuilder.create();
	}

	private void openDialog() {

		runningActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				synchronized (dialogOpenSync) {
					mAlertDialog.show();
					dialogOpen = true;
				}
			}
		});
	}

	private void closeDialog() {

		runningActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				mAlertDialog.dismiss();
			}
		});
	}
}
