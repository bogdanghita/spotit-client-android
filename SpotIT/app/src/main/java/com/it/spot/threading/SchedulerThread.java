package com.it.spot.threading;

import android.os.Looper;
import android.util.Log;

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

		mInterval.set(interval);

		if (handler != null) {
			((SchedulerHandler) handler).setInterval(interval);
		}
	}

	@Override
	public void run() {

		Looper.prepare();

		mThread = new LooperThread();
		mThread.start();

		handler = new SchedulerHandler(mThread, mRunnable, mInterval.longValue());

		// Sleeping until the handler of mThread is created
		while (mThread.handler == null) {
		}

		handler.sendMessage(handler.obtainMessage(SchedulerHandler.MSG_SCHEDULE));

		Looper.loop();
	}

	@Override
	public void quit() {
		super.quit();

		mThread.quit();

		Log.d(TaskScheduler.TAG_TIMER, "mThread quit. Waiting for mThread to join.");

		try {
			mThread.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(TaskScheduler.TAG_TIMER, e.getClass().getName() + " | " + "exception on join()");
		}

		Log.d(TaskScheduler.TAG_TIMER, "mThread finished.");
	}
}