package com.snet.promise;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AbstractPromise<T extends AbstractPromise<?>> implements Promise<T> {
	private static final AtomicIntegerFieldUpdater<AbstractPromise> STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractPromise.class, "state");
	protected volatile ListenerNode head, tail;
	protected static final int INIT = 0;
	protected static final int COMPLETING = 1;
	protected volatile int state = INIT;

	@Override
	public T sync() throws InterruptedException {
		return sync(-1);
	}

	@Override
	public T sync(long timeout) throws InterruptedException {
		if (isFinish())
			return (T) this;
		final long expireTime = timeout < 0 ? Long.MAX_VALUE : (System.currentTimeMillis() + timeout);
		long t;
		while (true) {
			if (isFinish())
				break;
			synchronized (this) {
				if ((t = expireTime - System.currentTimeMillis()) < 1 || isFinish())
					break;
				this.wait(t < 100 ? t : 100);
			}

		}
		return (T) this;
	}

	@Override
	public boolean isFinish() {
		return state > COMPLETING;
	}

	protected boolean casState(int oldState, int newState) {
		return STATE_UPDATER.compareAndSet(this, oldState, newState);
	}

	@Override
	public T addListener(PromiseListener<T> listener) {
		if (!addListener0(listener))
			listener.onFinish((T) this);
		return (T) this;
	}

	private final boolean addListener0(PromiseListener<T> listener) {
		if (isFinish())
			return false;
		synchronized (this) {
			if (isFinish())
				return false;
			ListenerNode node = new ListenerNode(listener);
			if (head == null) {
				head = tail = node;
			} else {
				tail.setNext(node);
				tail = node;
			}
			return true;
		}
	}

	protected final void executeListeners() {
		synchronized (this) {
			this.notifyAll();
		}
		for (ListenerNode n = head; n != null; n = n.getNext()) {
			try {
				n.getListener().onFinish(this);
			} catch (Throwable ignored) {
			}
		}
	}

	protected static class ListenerNode {
		protected ListenerNode next;
		protected final PromiseListener listener;

		public ListenerNode(PromiseListener listener) {
			this.listener = listener;
		}

		public PromiseListener getListener() {
			return listener;
		}

		public ListenerNode getNext() {
			return next;
		}

		public void setNext(ListenerNode next) {
			this.next = next;
		}
	}


}
