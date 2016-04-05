package com.it.spot.maps;

import android.util.Log;

import com.it.spot.common.Constants;
import com.it.spot.threading.Event;

import java.util.Iterator;
import java.util.List;

/**
 * Waits for all events to be set, then notifies the listener
 */
public class StateMonitorThread extends Thread {

	private StateMonitorListener mListener;
	private List<Event> mEventList;

	public StateMonitorThread(StateMonitorListener listener, List<Event> eventList) {
		this.mListener = listener;
		this.mEventList = eventList;
	}

	@Override
	public void run() {

		Iterator<Event> i = mEventList.iterator();
		while (i.hasNext()) {
			Event e = i.next();

			try {
				e.doWait(10);
			}
			catch (InterruptedException e1) {
				Log.d(Constants.APP + Constants.STATE_MONITOR, e1.toString());
			}

			if (e.isSet()) {
				i.remove();
			}
		}

		mListener.notifyStateReady();
	}
}
