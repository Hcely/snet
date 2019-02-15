package com.snet.util.thread;

import com.snet.Provider;
import com.snet.promise.FuturePromise;
import com.snet.util.RuntimeUtil;

import java.util.concurrent.Future;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class SimpleWorkerPool<T> implements WorkService<T> {
	protected static int i = 0;

	protected static final synchronized String nextName() {
		String str = "SimpleWorkerPool-" + i;
		++i;
		return str;
	}

	protected boolean destroy;
	protected final String name;
	protected final Consumer<T> consumer;
	protected final TaskQueue queue;
	protected final Worker<T>[] workers;

	public SimpleWorkerPool(Consumer<T> consumer) {
		this(RuntimeUtil.DOUBLE_CORE_PROCESSOR, consumer);
	}

	public SimpleWorkerPool(int workerSize, Consumer<T> consumer) {
		this(workerSize, consumer, DefTaskQueue::new);
	}

	public SimpleWorkerPool(int workerSize, Consumer<T> consumer, Provider<TaskQueue> queueProvider) {
		workerSize = workerSize < 1 ? 1 : workerSize;
		queueProvider = queueProvider == null ? DefTaskQueue::new : queueProvider;
		this.destroy = false;
		this.queue = queueProvider.get();
		this.name = nextName();
		this.consumer = consumer;
		this.workers = new Worker[workerSize];
		for (int i = 0; i < workerSize; ++i)
			this.workers[i] = new Worker<>(name + "-worker-" + i, queue, consumer);
		for (Worker<T> worker : workers)
			worker.initialize();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public <E> Future<E> submit(T task, E result) {
		if (destroy) return null;
		FuturePromise<E> promise = FuturePromise.create(new Worker.TaskRunner(consumer, task), result);
		queue.add(promise);
		return promise;
	}

	@Override
	public void execute(T task) {
		if (destroy) return;
		queue.add(task);
	}

	@Override
	public synchronized void destroyNow() {
		destroy = true;
		for (Worker<T> worker : workers)
			worker.destroyNow();
	}

	@Override
	public synchronized void destroy() {
		destroy = true;
		for (Worker<T> worker : workers)
			worker.destroy();
	}

	@Override
	public Consumer<T> getConsumer() {
		return consumer;
	}

	public int workerSize() {
		return workers.length;
	}

}
