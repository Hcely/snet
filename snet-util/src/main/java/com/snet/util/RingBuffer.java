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

	protected static class NodeCtrl extends ThreadCtrl {
		protected final int state;
		protected final int capacity;
		protected final AtomicLong limit, pos;

		public NodeCtrl(int state, int capacity) {
			this.state = state;
			this.capacity = capacity;
			this.limit = new AtomicLong(capacity);
			this.pos = new AtomicLong(0);
		}
	}

	protected int mask;
	protected Object[] buffer;
	protected AtomicIntegerArray stateBuffer;
	protected NodeCtrl firstNode;
	protected NodeCtrl[] nodes;
	protected boolean destroy;
	protected boolean loop;

	public RingBuffer(int customSteps, int capacity, Builder<T> builder) {
		capacity = MapPlus.ceil2(capacity < MIN_CAPACITY ? MIN_CAPACITY : capacity);
		this.mask = capacity - 1;
		this.buffer = new Object[capacity];
		this.stateBuffer = new AtomicIntegerArray(capacity);
		this.firstNode = new NodeCtrl(0, capacity);
		this.nodes = new NodeCtrl[customSteps + 3];
		this.destroy = false;
		this.loop = true;

		nodes[1] = nodes[customSteps + 2] = firstNode;
		for (int i = 0; i < customSteps; ++i)
			nodes[i + 2] = new NodeCtrl(i + 1, 0);
		nodes[0] = nodes[customSteps + 1];

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

	public long require(int state) {
		return require(state, -1);
	}

	public long require(int state, int waitCount) {
		if (state == 0 && destroy)
			return DESTROY_ID;
		return require(state, nodes[state], nodes[state + 1], waitCount < 0 ? -1 : waitCount);
	}

	protected long require(int state, NodeCtrl prevCtrl, NodeCtrl ctrl, int waitCount) {
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
			} else if (!destroy || id < firstNode.pos.get()) {
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
			if (stateBuffer.get((int) (++newL & mask)) != state) {
				--newL;
				break;
			}
		}
		if (oldL < newL)
			return limit.compareAndSet(oldL, newL) ? newL : limit.get();
		return oldL;
	}

	public void publish(int state, long id) {
		NodeCtrl nextCtrl = nodes[state + 2];
		stateBuffer.set((int) (id & mask), nextCtrl.state);
		nextCtrl.notifyAllCount(4);
	}

	public T get(long id) {
		return (T) buffer[(int) (id & mask)];
	}

	public int getCapacity() {
		return buffer.length;
	}
}
