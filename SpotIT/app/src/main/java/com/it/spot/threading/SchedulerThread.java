package com.it.spot.threading;

import android.os.Looper;
import android.util.Log;

import com.it.spot.common.Constants;

import java.util.concurrent.atomic.AtomicLong;


public class SchedulerThread extends LooperThread {

	private LooperThread mThread;

	private Runnable mRunnable;
	private AtomicLong mInterval = new AtomicLong();

	public SchedulerThread(Runnable runnable, long interval) {

		this.mRunnable = runnable;
		this.mInterval.set(interval);
	}

	public void setInterval(long interval) {

		synchronized (syncObj) {

			mInterval.set(interval);

			if (handler != null) {
				((SchedulerHandler) handler).setInterval(interval);
			}
		}
	}

	@Override
	public void run() {

		synchronized (syncObj) {

			Looper.prepare();

			mThread = new LooperThread();
			mThread.start();

			handler = new SchedulerHandler(mThread, mRunnable, mInterval.longValue());

			// Waiting until the handler of mThread is created
			while (mThread.handler == null) ;

			handler.sendMessage(handler.obtainMessage(SchedulerHandler.MSG_SCHEDULE));
		}

		Looper.loop();
	}

	@Override
	public void quit() {

		synchronized (syncObj) {

			/**
			 * NOTE: synchronized locks are reentrant
			 * http://stackoverflow.com/questions/13197756/synchronized-method-calls-itself-recursively-is-this-broken
			 */
			super.quit();

			mThread.quit();

			Log.d(Constants.TAG_TIMER, "mThread quit. Waiting for mThread to join.");

			try {
				mThread.join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				Log.d(Constants.TAG_TIMER, e.getClass().getName() + " | " + "exception on join()");
			}

			Log.d(Constants.TAG_TIMER, "mThread finished.");
		}
	}
}