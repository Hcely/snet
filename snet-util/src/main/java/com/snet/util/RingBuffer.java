package com.snet.util;

import com.snet.Builder;
import com.snet.Shutdownable;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unchecked")
public class RingBuffer<T> implements Shutdownable {
	public static final long EMPTY_ID = -1;
	public static final long DESTROY_ID = -2;
	public static final int MIN_CAPACITY = 32;

	protected static class StateCtrl extends ThreadCtrl {
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
	protected final Object[] buffer;
	protected final AtomicIntegerArray stateBuffer;
	protected final StateCtrl firstState;
	protected final StateCtrl[] states;
	protected boolean destroy;
	protected boolean loop;

	public RingBuffer(int customStateSize, int capacity, Builder<T> builder) {
		capacity = MapPlus.ceil2(capacity < MIN_CAPACITY ? MIN_CAPACITY : capacity);
		customStateSize = customStateSize < 1 ? 1 : customStateSize;

		this.mask = capacity - 1;
		this.buffer = new Object[capacity];
		this.stateBuffer = new AtomicIntegerArray(capacity);
		this.firstState = new StateCtrl(0, capacity);
		this.states = new StateCtrl[customStateSize + 3];
		this.destroy = false;
		this.loop = true;

		states[1] = states[customStateSize + 2] = firstState;
		for (int i = 0; i < customStateSize; ++i)
			states[i + 2] = new StateCtrl(i + 1, 0);
		states[0] = states[customStateSize + 1];

		for (int i = 0; i < capacity; ++i) {
			buffer[i] = builder.build();
			stateBuffer.set(i, 0);
		}
	}

	@Override
	public void destroy() {
		destroy = true;
	}

	@Override
	public void destroyNow() {
		destroy = true;
		loop = false;
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
		final AtomicLong limit = ctrl.limit;
		final AtomicLong pos = ctrl.pos;
		long id, l = limit.get();
		int count = 0;
		while (loop) {
			if ((id = pos.get()) < l || id < (l = limit.get())) {
				if (pos.compareAndSet(id, id + 1))
					return id;
			} else if (l < (l = tryMoveLimit(stateBuffer, prevCtrl.pos.get() + ctrl.capacity, limit, state))) {
				if ((id = pos.get()) < l && pos.compareAndSet(id, id + 1))
					return id;
			} else if (!destroy || id < firstState.pos.get()) {
				if (waitCount == 0)
					return EMPTY_ID;
				if ((count = ctrl.waitCount(count)) > ThreadCtrl.SKIP_COUNT + ThreadCtrl.YIELD_COUNT + ThreadCtrl.PARK_COUNT && waitCount > 0 && --waitCount == 0)
					return EMPTY_ID;
			} else
				break;
		}
		return DESTROY_ID;
	}

	protected long tryMoveLimit(AtomicIntegerArray stateBuffer, long prevP, AtomicLong limit, int state) {
		long oldL = limit.get(), newL = oldL;
		int mask = this.mask;
		while (newL < prevP) {
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
		nextCtrl.notifyAllCount(4);
	}

	public T get(long id) {
		return (T) buffer[(int) (id & mask)];
	}

	public int getCapacity() {
		return buffer.length;
	}

	public int customStateSize() {
		return states.length - 3;
	}
}
