package com.it.spot.threading;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Bogdan on 04/04/2016.
 */
public class Event {

	Lock lock = new ReentrantLock();
	Condition cond = lock.newCondition();
	boolean flag;

	public void doWait() throws InterruptedException {
		lock.lock();
		try {
			while (!flag) {
				cond.await();
			}
		}
		finally {
			lock.unlock();
		}
	}

	public void doWait(float milliseconds) throws InterruptedException {
		lock.lock();
		try {
			while (!flag) {
				cond.await((int) (milliseconds), TimeUnit.MILLISECONDS);
			}
		}
		finally {
			lock.unlock();
		}
	}

	public boolean isSet() {
		lock.lock();
		try {
			return flag;
		}
		finally {
			lock.unlock();
		}
	}

	public void set() {
		lock.lock();
		try {
			flag = true;
			cond.signalAll();
		}
		finally {
			lock.unlock();
		}
	}

	public void clear() {
		lock.lock();
		try {
			flag = false;
			cond.signalAll();
		}
		finally {
			lock.unlock();
		}
	}
}
