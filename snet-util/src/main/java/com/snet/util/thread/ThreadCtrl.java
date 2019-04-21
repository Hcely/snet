package com.snet.util.thread;

import com.snet.util.RuntimeUtil;

public interface ThreadCtrl {
	int PARK_TIMEOUT = 10000;
	int WAIT_TIMEOUT = 1;
	int SKIP_COUNT = 5;
	int YIELD_COUNT = 10;
	int PARK_COUNT = 1000000 / PARK_TIMEOUT;
	int WAIT_COUNT = SKIP_COUNT + YIELD_COUNT + PARK_COUNT;

	int waitCount(int count);

	default void notifyCount() {
		notifyCount(RuntimeUtil.CORE_PROCESSOR);
	}

	void notifyCount(int count);

	default void notifyAllCount() {
		notifyAllCount(RuntimeUtil.CORE_PROCESSOR);
	}

	void notifyAllCount(int count);
}
