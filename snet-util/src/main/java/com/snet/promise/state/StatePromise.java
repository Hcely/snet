package com.snet.promise.state;

import com.snet.promise.Promise;
import com.snet.promise.PromiseListener;

public interface StatePromise<T extends StatePromise<?>> extends Promise<T> {
	T syncState(int state) throws InterruptedException;

	T syncState(int state, long timeout) throws InterruptedException;

	T addListener(int state, PromiseListener<T> listener);

	int getState();

	boolean isState(int state);
}
