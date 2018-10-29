package com.snet.promise;

public interface Promise<T extends Promise<?>> {
	boolean isFinish();

	T sync() throws InterruptedException;

	T sync(long timeout) throws InterruptedException;

	T addListener(PromiseListener<T> listener);
}
