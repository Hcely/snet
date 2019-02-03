package com.snet.util;

import java.util.concurrent.locks.LockSupport;

public class ThreadCtrl {
	public static final int PARK_TIMEOUT = 10000;
	public static final int WAIT_TIMEOUT = 1;
	public static final int SKIP_COUNT = 5;
	public static final int YIELD_COUNT = 10;
	public static final int PARK_COUNT = 1000000/PARK_TIMEOUT;

	protected volatile int waitCount;
	protected volatile int skipCount;

	public ThreadCtrl() {
		this.waitCount = 0;
		this.skipCount = 0;
	}


	public int waitCount(int count) {
		if (count < 0) {
			--skipCount;
			++count;
		} else if (count < SKIP_COUNT) {
			++count;
		} else if (count < SKIP_COUNT + YIELD_COUNT) {
			++count;
			Thread.yield();
		} else if (count < SKIP_COUNT + YIELD_COUNT + PARK_COUNT) {
			++count;
			LockSupport.parkNanos(PARK_TIMEOUT);
		} else if (skipCount > 0) {
			count = -1;
		} else {
			++count;
			synchronized (this) {
				++waitCount;
				try {
					if (skipCount > 0)
						count = -1;
					else
						this.wait(WAIT_TIMEOUT);
				} catch (Throwable ignored) {
				} finally {
					--waitCount;
				}
			}
		}
		return count;
	}

	public void notifyCount(int count) {
		skipCount = count;
		if (waitCount > 0) {
			synchronized (this) {
				skipCount = count;
				this.notify();
			}
		}
	}

	public void notifyAllCount(int count) {
		skipCount = count;
		if (waitCount > 0) {
			synchronized (this) {
				skipCount = count;
				this.notifyAll();
			}
		}
	}
}
