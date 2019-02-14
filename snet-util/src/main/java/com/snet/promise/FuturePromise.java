package com.snet.promise;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

@SuppressWarnings("rawtypes")
public abstract class FuturePromise<V> extends AbstractPromise<FuturePromise<V>> implements Future<V>, Runnable {

	public static final FuturePromise<?> create(Runnable task) {
		return create(null, task);
	}

	public static final <V> FuturePromise<V> create(Runnable task, V result) {
		return new RunnerFuturePromise<>(task, result);
	}

	public static final <V> FuturePromise<V> create(Callable<V> task) {
		return new CallerFuturePromise<>(task);
	}

	protected static final AtomicReferenceFieldUpdater<FuturePromise, Thread> THREAD_UPDATER = AtomicReferenceFieldUpdater.newUpdater(FuturePromise.class, Thread.class, "runner");
	protected static final int FINISH = 2;
	protected static final int EXCEPTONAL = 3;
	protected static final int CANCELED = 4;
	protected static final int INTERRUPTING = 5;
	protected static final int INTERRUPTED = 6;

	protected volatile Thread runner;
	protected Throwable cause;
	protected V result;


	public FuturePromise() {
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
			executeListeners();
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
					exception(e);
				}
		} finally {
			runner = null;
			clearInterrupt(state);
		}

	}

	protected void exception(Throwable e) {
		if (state == INIT && casState(INIT, COMPLETING)) {
			this.cause = e;
			this.state = EXCEPTONAL;
			executeListeners();
		}
	}

	protected void finish(V result) {
		if (state == INIT && casState(INIT, COMPLETING)) {
			this.result = result;
			this.state = FINISH;
			executeListeners();
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

	private static final class RunnerFuturePromise<V> extends FuturePromise<V> {
		protected final Runnable task;

		public RunnerFuturePromise(Runnable task, V result) {
			this.task = task;
			this.result = result;
		}

		@Override
		protected V doExecute() {
			task.run();
			return result;
		}
	}

	private static final class CallerFuturePromise<V> extends FuturePromise<V> {
		protected final Callable<V> task;

		public CallerFuturePromise(Callable<V> task) {
			this.task = task;
		}

		@Override
		protected V doExecute() throws Throwable {
			return task.call();
		}

	}

}
