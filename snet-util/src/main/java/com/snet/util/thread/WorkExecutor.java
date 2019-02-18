package com.snet.util.thread;

import java.util.function.Consumer;

public interface WorkExecutor<T> {
	Consumer<T> getConsumer();

	void execute(T task);

	void execute(Runnable task);
}
