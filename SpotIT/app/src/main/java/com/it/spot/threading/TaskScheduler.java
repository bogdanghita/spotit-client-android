package com.it.spot.threading;

import android.util.Log;


public class TaskScheduler {

	public final static String TAG_TIMER = "TIMER";

	private SchedulerThread mThread = null;

	public void setInterval(long interval) {

		if (mThread != null) {
			mThread.setInterval(interval);
		}
	}

	/**
	 * @return - true if the scheduler is running and false otherwise
	 */
	public boolean isRunning() {

		return mThread != null;
	}

	/**
	 * Starts the execution of the task, on a separate thread and at the given hour.
	 * The hour can be modified using the setInterval(long) method.
	 *
	 * @param task     - the task that will be executed at the given hour.
	 * @param interval - the hour at which the task is executed.
	 * @throws IllegalArgumentException    - if hour is less than or equal to zero.
	 * @throws IllegalThreadStateException - if the scheduler was already started.
	 */
	public void start(Runnable task, long interval) {

		if (mThread != null) {
			throw new IllegalThreadStateException();
		}

		if (interval <= 0) {
			throw new IllegalArgumentException();
		}

		mThread = new SchedulerThread(task, interval);

		mThread.start();
	}

	/**
	 * TODO: See if you want this to join the thread or not. Maybe a parameter would help.
	 * TODO: Think if we want to also stop the task that is currently executed by calling mThread.interrupt()
	 * Stops the execution of the task and the thread that it was running on.
	 *
	 * @throws IllegalThreadStateException - if the scheduler is not started or was already stopped.
	 */
	public void stop() {

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
