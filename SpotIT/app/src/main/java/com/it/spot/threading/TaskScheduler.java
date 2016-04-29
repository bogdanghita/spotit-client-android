package com.it.spot.threading;

import android.util.Log;


public class TaskScheduler {

	public final static String TAG_TIMER = "TIMER";

	private SchedulerThread mThread = null;

	private final Object syncObj = new Object();

	public void setInterval(long interval) {

		synchronized (syncObj) {

			if (mThread != null) {
				mThread.setInterval(interval);
			}
		}
	}

	/**
	 * @return - true if the scheduler is running and false otherwise
	 */
	public boolean isRunning() {

		synchronized (syncObj) {

			return mThread != null;
		}
	}

	/**
	 * Starts the execution of the task, on a separate thread and at the given interval.
	 * The interval can be modified using the setInterval(long) method.
	 *
	 * @param task     - the task that will be executed at the given interval.
	 * @param interval - the interval in milliseconds at which the task is executed.
	 * @throws IllegalArgumentException    - if interval is less than or equal to zero.
	 * @throws IllegalThreadStateException - if the scheduler was already started.
	 */
	public void start(Runnable task, long interval) {

		synchronized (syncObj) {

			if (mThread != null) {
				throw new IllegalThreadStateException();
			}

			if (interval <= 0) {
				throw new IllegalArgumentException();
			}

			mThread = new SchedulerThread(task, interval);

			mThread.start();
		}
	}

	/**
	 * TODO: See if you want this to join the thread or not. Maybe a parameter would help.
	 * TODO: Think if we want to also stop the task that is currently executed by calling mThread.interrupt()
	 * Stops the execution of the task and the thread that it was running on.
	 *
	 * @throws IllegalThreadStateException - if the scheduler is not started or was already stopped.
	 */
	public void stop() {

		synchronized (syncObj) {

			if (mThread == null) {
				throw new IllegalThreadStateException();
			}

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

			mThread = null;
		}
	}
}
