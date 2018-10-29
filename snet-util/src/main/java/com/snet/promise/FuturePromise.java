package com.snet.promise;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

@SuppressWarnings("rawtypes")
public abstract class FuturePromise<V> extends AbstractPromise<FuturePromise<V>> implements Future<V>, Runnable {

	public static final FuturePromise<?> create(Runnable task) {
		return create(null, task);
	}

	public static final FuturePromise<?> create(Executor executor, Runnable task) {
		return new RunnerFuturePromise(executor, task);
	}

	public static final <V> FuturePromise<V> create(Callable<V> task) {
		return create(null, task);
	}

	public static final <V> FuturePromise<V> create(Executor executor, Callable<V> task) {
		return new CallerFuturePromise<>(null, task);
	}

	protected static final AtomicReferenceFieldUpdater<FuturePromise, Thread> THREAD_UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(FuturePromise.class, Thread.class, "runner");
	protected static final int FINISH = 2;
	protected static final int EXCEPTONAL = 3;
	protected static final int CANCELED = 4;
	protected static final int INTERRUPTING = 5;
	protected static final int INTERRUPTED = 6;

	protected final Executor executor;
	protected volatile Thread runner;
	protected Throwable cause;
	protected V result;

	public FuturePromise() {
		this(null);
	}

	public FuturePromise(Executor executor) {
		this.executor = executor;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (state == INIT && casState(INIT, mayInterruptIfRunning ? INTERRUPTING : CANCELED)) {
			if (mayInterruptIfRunning) {
				Thread t = runner;
				if (t != null)
					t.interrupt();
				state = INTERRUPTED;
			}
			executeListeners(executor);
			return true;
		} else
			return false;
	}

	@Override
	public boolean isCancelled() {
		return state >= CANCELED;
	}

	@Override
	public boolean isDone() {
		return state > INIT;
	}

	public Throwable getCause() {
		return cause;
	}

	public V getResult() {
		return result;
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		int state = sync().state;
		if (state == FINISH)
			return result;
		else if (state == EXCEPTONAL)
			throw new ExecutionException(cause);
		else
			throw new CancellationException();
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		int state = sync(unit.toMillis(timeout)).state;
		if (state < FINISH)
			throw new TimeoutException();
		else if (state == FINISH)
			return result;
		else if (state == EXCEPTONAL)
			throw new ExecutionException(cause);
		else
			throw new CancellationException();
	}

	@Override
	public void run() {
		if (state != INIT || !THREAD_UPDATER.compareAndSet(this, null, Thread.currentThread()))
			return;
		try {
			if (state == INIT)
				try {
					V result = doExecute();
					finish(result);
				} catch (Throwable e) {
					exeception(e);
				}
		} finally {
			runner = null;
			clearInterrupt(state);
		}

	}

	protected void exeception(Throwable e) {
		if (state == INIT && casState(INIT, COMPLETING)) {
			this.cause = e;
			this.state = EXCEPTONAL;
			executeListeners(null);
		}
	}

	protected void finish(V result) {
		if (state == INIT && casState(INIT, COMPLETING)) {
			this.result = result;
			this.state = FINISH;
			executeListeners(null);
		}
	}

	protected void clearInterrupt(int s) {
		if (s == COMPLETING) {
			while (state == COMPLETING)
				Thread.yield();
			Thread.interrupted();
		}
	}

	protected abstract V doExecute() throws Throwable;

	private static final class RunnerFuturePromise extends FuturePromise {
		protected final Runnable task;

		public RunnerFuturePromise(Executor executor, Runnable task) {
			super(executor);
			this.task = task;
		}

		@Override
		protected Object doExecute() throws Throwable {
			task.run();
			return null;
		}
	}

	private static final class CallerFuturePromise<V> extends FuturePromise<V> {
		protected final Callable<V> task;

		public CallerFuturePromise(Executor executor, Callable<V> task) {
			super(executor);
			this.task = task;
		}

		@Override
		protected V doExecute() throws Throwable {
			return task.call();
		}

	}

}
