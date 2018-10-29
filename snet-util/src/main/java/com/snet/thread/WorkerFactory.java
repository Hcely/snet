package com.snet.thread;

import com.snet.Factory;

public interface WorkerFactory extends Factory<Worker, TaskQueue<?>> {
	@Override
	Worker create(TaskQueue<?> object);
}
