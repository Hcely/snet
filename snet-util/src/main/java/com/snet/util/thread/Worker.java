package com.snet.util.thread;

import com.snet.Initializable;
import com.snet.promise.FuturePromise;

import java.util.concurrent.Future;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class Worker<T> implements Initializable, WorkService<T> {
	protected static int i = 0;

	protected static final synchronized String nextName() {
		String str = "Worker-" + i;
		++i;
		return str;
	}

	public static Worker currentWorker() {
		Thread thread = Thread.currentThread();
		return thread.getClass() == WorkerThread.class ? ((WorkerThread) thread).worker : null;
	}

	protected boolean destroy;
	protected boolean loop;
	protected boolean alive;
	protected final String name;
	protected final TaskQueue queue;
	protected final Consumer<T> consumer;
	protected final Thread thread;

	public Worker(TaskQueue queue, Consumer<T> consumer) {
		this(nextName(), queue, consumer);
	}

	public Worker(String name, TaskQueue queue, Consumer<T> consumer) {
		this.loop = false;
		this.destroy = false;
		this.alive = false;
		this.name = name == null ? nextName() : name;
		this.queue = queue;
		this.consumer = consumer;
		this.thread = new WorkerThread(this);
	}

	@Override
	public void initialize() {
		this.loop = true;
		this.destroy = false;
		this.alive = true;
		thread.start();
	}

	@Override
	public void execute(T task) {
		queue.add(task);
	}

	public Worker<T> setPriority(int newPriority) {
		thread.setPriority(newPriority);
		return this;
	}

	public Worker<T> setDaemon(boolean on) {
		thread.setDaemon(on);
		return this;
	}

	public Worker<T> setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler eh) {
		thread.setUncaughtExceptionHandler(eh);
		return this;
	}

	@Override
	public <E> Future<E> submit(T task, E result) {
		FuturePromise<E> promise = FuturePromise.create(new TaskRunner(consumer, task), result);
		queue.add(promise);
		return promise;
	}

	@Override
	public synchronized void destroyNow() {
		destroy = true;
		loop = true;
	}

	@Override
	public synchronized void destroy() {
		destroy = true;
	}

	@Override
	public Consumer<T> getConsumer() {
		return consumer;
	}

	@Override
	public String getName() {
		return name;
	}

	public long getWorkerId() {
		return thread.getId();
	}

	public boolean isALive() {
		return alive;
	}

	public static final class TaskRunner implements Runnable {
		protected final Consumer consumer;
		protected final Object task;

		public TaskRunner(Consumer consumer, Object task) {
			this.consumer = consumer;
			this.task = task;
		}

		@Override
		public void run() {
			consumer.accept(task);
		}
	}

	protected final static class WorkerThread extends Thread {
		protected final Worker worker;

		public WorkerThread(Worker worker) {
			super(worker.name);
			this.worker = worker;
		}

		@Override
		public void run() {
			final Worker worker = this.worker;
			final Consumer consumer = worker.consumer;
			final TaskQueue queue = worker.queue;
			Object task = null;
			int count = 0;
			while (worker.loop) {
				if ((task = queue.pop()) != null) {
					count = 0;
					execute(consumer, task);
				} else if (worker.destroy)
					break;
				else
					count = queue.waitCount(count);
			}
			worker.alive = false;
		}

		protected void execute(Consumer consumer, Object task) {
			try {
				if (task instanceof Runnable)
					((Runnable) task).run();
				else
					consumer.accept(task);
			} catch (Throwable e) {
				this.getUncaughtExceptionHandler().uncaughtException(this, e);
			}
		}
	}
}
