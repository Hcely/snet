package com.snet.util.thread;

import com.snet.Shutdownable;

import java.util.concurrent.Future;

public interface WorkService<T> extends WorkExecutor<T>, Shutdownable {

	String getName();

	default Future<?> submit(T task) {
		return submit(task, null);
	}

	<E> Future<E> submit(T task, E result);

	default Future<?> submit(Runnable task) {
		return submit(task, null);
	}

	<E> Future<E> submit(Runnable task, E result);
}
