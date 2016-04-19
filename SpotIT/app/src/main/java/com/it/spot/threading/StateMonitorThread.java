package com.it.spot.threading;

import android.util.Log;

import com.it.spot.common.Constants;

import java.util.Iterator;
import java.util.List;

/**
 * Waits for all events to be set, then notifies the listener
 */
public class StateMonitorThread extends Thread {

	private final static int WAIT_INTERVAL = 50;

	private StateMonitorListener mListener;
	private List<Event> mEventList;

	public StateMonitorThread(StateMonitorListener listener, List<Event> eventList) {
		this.mListener = listener;
		this.mEventList = eventList;
	}

	@Override
	public void run() {

		while (!mEventList.isEmpty()) {

			Iterator<Event> i = mEventList.iterator();
			while (i.hasNext()) {
				Event e = i.next();

				try {
					e.doWait(WAIT_INTERVAL);
				}
				catch (InterruptedException e1) {
					Log.d(Constants.APP + Constants.STATE_MONITOR, e1.toString());
				}

				if (e.isSet()) {
					i.remove();
				}
			}
		}

		mListener.notifyStateReady();
	}
}
