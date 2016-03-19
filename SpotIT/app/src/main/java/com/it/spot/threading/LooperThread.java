package com.it.spot.threading;

import android.os.Handler;
import android.os.Looper;


public class LooperThread extends Thread {

	public Handler handler;

	@Override
	public void run() {

		// TODO: think if this is desired and if it affects something; read about mThread priority in Java
		android.os.Process.setThreadPriority(MAX_PRIORITY);
		this.setPriority(MAX_PRIORITY);

		Looper.prepare();

		handler = new Handler();

		Looper.loop();
	}

	/**
	 * Removes all callbacks and messages of the handler and then stops the mThread from looping.
	 */
	public void quit() {

		handler.removeCallbacksAndMessages(null);

		handler.getLooper().quit();
	}
}
