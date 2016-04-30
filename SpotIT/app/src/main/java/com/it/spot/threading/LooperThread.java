package com.it.spot.threading;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.it.spot.common.Constants;


public class LooperThread extends Thread {

	public Handler handler;

	protected final Object syncObj = new Object();

	@Override
	public void run() {

		// TODO: think if this is desired and if it affects something; read about mThread priority in Java
//		android.os.Process.setThreadPriority(MAX_PRIORITY);
//		this.setPriority(MAX_PRIORITY);

		synchronized (syncObj) {

			Looper.prepare();

			handler = new Handler();
		}

		Looper.loop();
	}

	/**
	 * Removes all callbacks and messages of the handler and then stops the mThread from looping.
	 */
	public void quit() {

		synchronized (syncObj) {

			handler.removeCallbacksAndMessages(null);

			handler.getLooper().quit();
		}
	}
}
