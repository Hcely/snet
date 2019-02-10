package com.snet.util;

import java.util.concurrent.locks.LockSupport;

public class DefThreadCtrl implements ThreadCtrl {
	protected volatile int waitCount;
	protected volatile int skipCount;

	public DefThreadCtrl() {
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
