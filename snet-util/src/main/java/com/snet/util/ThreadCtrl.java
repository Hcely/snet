package com.snet.util;

public interface ThreadCtrl {
	int PARK_TIMEOUT = 10000;
	int WAIT_TIMEOUT = 1;
	int SKIP_COUNT = 5;
	int YIELD_COUNT = 10;
	int PARK_COUNT = 1000000 / PARK_TIMEOUT;

	int waitCount(int count);

	void notifyCount(int count);

	void notifyAllCount(int count);
}
