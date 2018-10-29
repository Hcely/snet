package com.snet.promise;

public interface PromiseListener<T extends Promise<?>> {
	void onFinish(T promise);
}
