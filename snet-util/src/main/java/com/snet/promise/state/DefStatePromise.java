package com.snet.promise.state;

import com.snet.promise.PromiseListener;
import com.snet.util.coll.IntHashMap;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class DefStatePromise implements StatePromise<DefStatePromise> {
	protected static final AtomicIntegerFieldUpdater<DefStatePromise> STATE_UPDATER = AtomicIntegerFieldUpdater
			.newUpdater(DefStatePromise.class, "state");
	protected final int finishState;
	protected volatile int state;
	protected volatile IntHashMap<List<PromiseListener<DefStatePromise>>> stateListeners;

	public DefStatePromise(int finishState) {
		this.finishState = finishState;
	}

	@Override
	public DefStatePromise sync() throws InterruptedException {
		return sync(-1);
	}

	@Override
	public DefStatePromise sync(long timeout) throws InterruptedException {
		return syncState(finishState, timeout);
	}

	@Override
	public DefStatePromise syncState(int state) throws InterruptedException {
		return syncState(state, -1);
	}

	@Override
	public DefStatePromise syncState(int state, long timeout) throws InterruptedException {
		if (isState(state))
			return this;
		final long expireTime = timeout < 0 ? Long.MAX_VALUE : (System.currentTimeMillis() + timeout);
		long t;
		while (true) {
			if (isState(state))
				break;
			synchronized (this) {
				if ((t = expireTime - System.currentTimeMillis()) < 1 || isState(state))
					break;
				this.wait(t < 100 ? t : 100);
			}
		}
		return this;
	}

	public boolean nextState(int state) {
		int tmp;
		while ((tmp = this.state) < state) {
			if (STATE_UPDATER.compareAndSet(this, tmp, state)) {
				executeListeners(state);
				return true;
			}
		}
		return false;
	}

	@Override
	public DefStatePromise addListener(PromiseListener<DefStatePromise> listener) {
		return addListener(finishState, listener);
	}

	@Override
	public DefStatePromise addListener(int state, PromiseListener<DefStatePromise> listener) {
		if (!addListener0(state, listener))
			listener.onFinish(this);
		return this;
	}

	private boolean addListener0(int state, PromiseListener<DefStatePromise> listener) {
		if (isState(state))
			return false;
		synchronized (this) {
			if (isState(state))
				return false;
			if (stateListeners == null)
				stateListeners = new IntHashMap<>();
			List<PromiseListener<DefStatePromise>> list = stateListeners.get(state);
			if (list == null)
				stateListeners.put(state, list = new LinkedList<>());
			list.add(listener);
			return true;
		}
	}

	private void executeListeners(int state) {
		IntHashMap<List<PromiseListener<DefStatePromise>>> map = this.stateListeners;
		if (map == null)
			return;
		List<PromiseListener<DefStatePromise>> list = map.get(state);
		if (list == null)
			return;
		for (PromiseListener<DefStatePromise> l : list)
			try {
				l.onFinish(this);
			} catch (Throwable ignored) {
			}
	}

	@Override
	public int getState() {
		return state;
	}

	@Override
	public boolean isState(int state) {
		return this.state >= state;
	}

	@Override
	public boolean isFinish() {
		return state >= finishState;
	}

}
