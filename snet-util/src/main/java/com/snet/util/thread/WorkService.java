package com.snet.util.thread;

import java.util.concurrent.Future;

public interface WorkService<T> extends WorkExecutor<T> {
	Future<?> submit(T work);

	<E> Future<E> submit(T work, E result);
}
