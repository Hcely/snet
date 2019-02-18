package com.snet.util.thread;

import com.snet.Provider;
import com.snet.util.MathUtil;
import com.snet.util.RuntimeUtil;

import java.util.concurrent.Future;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class FixedWorkerPool<T> implements WorkService<T> {
	protected static int i = 0;

	protected static final synchronized String nextName() {
		String str = "FixedWorkerPool-" + i;
		++i;
		return str;
	}

	protected final int mask;
	protected final String name;
	protected final Consumer<T> consumer;
	protected final Worker<T>[] workers;

	public FixedWorkerPool(Consumer<T> consumer) {
		this(RuntimeUtil.DOUBLE_CORE_PROCESSOR, consumer);
	}

	public FixedWorkerPool(int workerSize, Consumer<T> consumer) {
		this(workerSize, consumer, DefTaskQueue::new);
	}

	public FixedWorkerPool(int workerSize, Consumer<T> consumer, Provider<TaskQueue> queueProvider) {
		workerSize = MathUtil.ceil2(workerSize);
		queueProvider = queueProvider == null ? DefTaskQueue::new : queueProvider;
		this.mask = workerSize - 1;
		this.name = nextName();
		this.consumer = consumer;
		this.workers = new Worker[workerSize];
		for (int i = 0; i < workerSize; ++i)
			this.workers[i] = new Worker<>(name + "-worker-" + i, queueProvider.get(), consumer);
		for (Worker<T> worker : workers)
			worker.initialize();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public <E> Future<E> submit(T task, E result) {
		return workers[task.hashCode() & mask].submit(task, result);
	}

	@Override
	public <E> Future<E> submit(Runnable task, E result) {
		return workers[task.hashCode() & mask].submit(task, result);
	}

	@Override
	public void execute(T task) {
		workers[task.hashCode() & mask].execute(task);
	}

	@Override
	public void execute(Runnable task) {
		workers[task.hashCode() & mask].execute(task);
	}

	@Override
	public void destroyNow() {
		for (Worker<T> worker : workers)
			worker.destroyNow();
	}

	@Override
	public void destroy() {
		for (Worker<T> worker : workers)
			worker.destroy();
	}

	@Override
	public Consumer<T> getConsumer() {
		return consumer;
	}

	public Worker<T> getWorkerByHash(int hash) {
		return workers[hash & mask];
	}

	public Worker<T> getWorker(int idx) {
		return workers[idx];
	}

	public int workerSize() {
		return workers.length;
	}

}
