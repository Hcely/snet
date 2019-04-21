package com.snet.util.ring;

import com.snet.SNetBuilder;
import com.snet.Shutdownable;
import com.snet.util.thread.DefThreadCtrl;
import com.snet.util.MathUtil;
import com.snet.util.thread.ThreadCtrl;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

@SuppressWarnings("unchecked")
public class RingBuffer<T> implements Shutdownable {
	public static final long EMPTY_ID = -1;
	public static final long DESTROY_ID = -2;
	public static final int MIN_CAPACITY = 32;

	protected static class StateCtrl extends DefThreadCtrl {
		protected final int state;
		protected final int capacity;
		protected final AtomicLong limit, pos;

		public StateCtrl(int state, int capacity) {
			this.state = state;
			this.capacity = capacity;
			this.limit = new AtomicLong(capacity);
			this.pos = new AtomicLong(0);
		}
	}

	protected final int mask;
	protected final AtomicReferenceArray<T> buffer;
	protected final AtomicIntegerArray stateBuffer;
	protected final StateCtrl firstState;
	protected final StateCtrl[] states;
	protected boolean destroy;
	protected boolean loop;

	public RingBuffer(int consumerStateSize, int capacity, SNetBuilder<T> builder) {
		capacity = MathUtil.ceil2(capacity < MIN_CAPACITY ? MIN_CAPACITY : capacity);
		consumerStateSize = consumerStateSize < 1 ? 1 : consumerStateSize;

		this.mask = capacity - 1;
		this.buffer = new AtomicReferenceArray<>(capacity);
		this.stateBuffer = new AtomicIntegerArray(capacity);
		this.firstState = new StateCtrl(0, capacity);
		this.states = new StateCtrl[consumerStateSize + 3];
		this.destroy = false;
		this.loop = true;

		states[1] = states[consumerStateSize + 2] = firstState;
		for (int i = 0; i < consumerStateSize; ++i)
			states[i + 2] = new StateCtrl(i + 1, 0);
		states[0] = states[consumerStateSize + 1];

		for (int i = 0; i < capacity; ++i) {
			buffer.set(i, builder.build());
			stateBuffer.set(i, 0);
		}
	}

	@Override
	public synchronized void destroy() {
		destroy = true;
	}

	@Override
	public synchronized void destroyNow() {
		destroy = true;
		loop = false;
	}

	public boolean isDestroy() {
		return destroy;
	}

	public long acquire(int state) {
		return acquire(state, -1);
	}

	public long acquire(int state, int waitCount) {
		if (state == 0 && destroy)
			return DESTROY_ID;
		return acquire(state, states[state], states[state + 1], waitCount < 0 ? -1 : waitCount);
	}

	protected long acquire(int state, StateCtrl prevCtrl, StateCtrl ctrl, int waitCount) {
		final AtomicIntegerArray stateBuffer = this.stateBuffer;
		final AtomicLong limit = ctrl.limit, pos = ctrl.pos, firstPos = firstState.pos;
		long id, l = limit.get();
		int count = 0;
		while (loop) {
			if ((id = pos.get()) < l || id < (l = limit.get()) || id < (l = tryMoveLimit(stateBuffer, prevCtrl.pos.get() + ctrl.capacity, limit, state))) {
				if (pos.compareAndSet(id, id + 1))
					return id;
			} else if (!destroy || id < firstPos.get()) {
				if (waitCount == 0)
					return EMPTY_ID;
				if ((count = ctrl.waitCount(count)) > ThreadCtrl.WAIT_COUNT && waitCount > 0 && --waitCount == 0)
					return EMPTY_ID;
			} else
				break;
		}
		return DESTROY_ID;
	}

	protected long tryMoveLimit(AtomicIntegerArray stateBuffer, final long prevP, AtomicLong limit, int state) {
		long oldL = limit.get(), newL = oldL;
		int mask = this.mask, i = 0;
		while (newL < prevP && ++i < 1000) {
			if (stateBuffer.get((int) (newL & mask)) == state)
				++newL;
			else
				break;
		}
		if (oldL < newL)
			return limit.compareAndSet(oldL, newL) ? newL : limit.get();
		return oldL;
	}

	public void publish(int state, long id) {
		StateCtrl nextCtrl = states[state + 2];
		stateBuffer.set((int) (id & mask), nextCtrl.state);
		nextCtrl.notifyAllCount();
	}

	public T get(long id) {
		return buffer.get((int) (id & mask));
	}

	public int getCapacity() {
		return buffer.length();
	}

	public int consumerSize() {
		return states.length - 3;
	}

	public int size() {
		return (int) (states[1].pos.get() - states[0].pos.get());
	}

}
