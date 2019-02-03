package com.snet.thread;

import java.util.function.Consumer;

public interface TaskQueue<T> {

	Consumer<T> getHandler();

	void addFirst(T task);

	void addLast(T task);

	int size();

	default T pop() {
		return pop(0);
	}

	T pop(long timeout);
}
