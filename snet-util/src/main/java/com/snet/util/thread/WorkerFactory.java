package com.snet.util.thread;

import com.snet.Factory;

public interface WorkerFactory<T> extends Factory<Worker<T>, WorkQueue> {
	@Override
	Worker<T> create(WorkQueue queue);
}
