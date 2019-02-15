package com.snet.util;

import com.snet.Shutdownable;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ProducerConsumerBuffer<E> implements Shutdownable {
	protected final AtomicReferenceArray<E> array;
	protected final PosCtrl readCtrl, writeCtrl;
	protected final int capacity;
	protected final int mask;
	protected boolean loop;
	protected boolean running;
	protected boolean destroy;

	public ProducerConsumerBuffer(int capacity) {
		capacity = MathUtil.ceil2(capacity);
		this.array = new AtomicReferenceArray<>(capacity);
		this.readCtrl = new PosCtrl(0);
		this.writeCtrl = new PosCtrl(capacity);
		this.capacity = capacity;
		this.mask = capacity - 1;
		this.loop = true;
		this.running = true;
		this.destroy = false;
	}

	public boolean add(E e) {
		return add(e, -1);
	}

	public boolean add(E e, int waitCount) {
		if (e == null)
			throw new NullPointerException("element is null");
		final AtomicReferenceArray<E> array = this.array;
		final PosCtrl readCtrl = this.readCtrl, writeCtrl = this.writeCtrl;
		final AtomicLong read = readCtrl.pos, writeLimit = writeCtrl.limit, write = writeCtrl.pos;
		int count = 0;
		long lPos = writeLimit.get(), pos;
		while (running) {
			if ((pos = write.get()) < lPos || pos < (lPos = writeLimit.get()) || pos < (lPos = tryMoveLimit(array, read.get() + capacity, writeLimit, true))) {
				if (write.compareAndSet(pos, pos + 1)) {
					array.set((int) (pos & mask), e);
					readCtrl.notifyAllCount();
					return true;
				}
			} else if (destroy || waitCount == 0)
				break;
			else if ((count = writeCtrl.waitCount(count)) > ThreadCtrl.WAIT_COUNT && waitCount > 0 && --waitCount == 0)
				break;
		}
		return false;
	}

	public E poll() {
		return poll(-1);
	}

	public E poll(int waitCount) {
		final AtomicReferenceArray<E> array = this.array;
		final PosCtrl readCtrl = this.readCtrl, writeCtrl = this.writeCtrl;
		final AtomicLong readLimit = readCtrl.limit, read = readCtrl.pos, write = writeCtrl.pos;
		int count = 0;
		long lPos = readLimit.get(), pos;
		while (loop) {
			if ((pos = read.get()) < lPos || pos < (lPos = readLimit.get()) || pos < (lPos = tryMoveLimit(array, write.get(), readLimit, false))) {
				if (read.compareAndSet(pos, pos + 1)) {
					E result = array.getAndSet((int) (pos & mask), null);
					writeCtrl.notifyAllCount();
					return result;
				}
			} else if (destroy || waitCount == 0)
				break;
			else if ((count = readCtrl.waitCount(count)) > ThreadCtrl.WAIT_COUNT && waitCount > 0 && --waitCount == 0)
				break;
		}
		return null;
	}

	protected long tryMoveLimit(AtomicReferenceArray<E> array, long prevP, AtomicLong limit, boolean isNull) {
		long oldL = limit.get(), newL = oldL;
		final int mask = this.mask;
		while (newL < prevP) {
			if ((array.get((int) (newL & mask)) == null) == isNull)
				++newL;
			else
				break;
		}
		if (oldL < newL)
			return limit.compareAndSet(oldL, newL) ? newL : limit.get();
		return oldL;
	}

	@Override
	public synchronized void destroyNow() {
		this.running = false;
		this.destroy = true;
		this.loop = false;
	}

	@Override
	public synchronized void destroy() {
		this.running = false;
		this.destroy = true;
	}

	public int getCapacity() {
		return capacity;
	}

	public int size() {
		return (int) (writeCtrl.pos.get() - readCtrl.pos.get());
	}

	public boolean isEmpty() {
		return writeCtrl.pos.get() == readCtrl.pos.get();
	}

	public Enumeration<E> enumeration() {
		return new En<>(array, readCtrl.pos, writeCtrl.pos, mask);
	}

	protected static class PosCtrl extends DefThreadCtrl {
		protected final AtomicLong limit, pos;
		protected final int capacity;

		public PosCtrl(int capacity) {
			this.capacity = capacity;
			this.limit = new AtomicLong(capacity);
			this.pos = new AtomicLong(0);
		}
	}

	protected static final class En<E> implements Enumeration<E> {
		protected final AtomicReferenceArray<E> array;
		protected final AtomicLong read;
		protected final int mask;
		protected final long wPos;
		protected E obj;
		protected long rPos;

		public En(AtomicReferenceArray<E> array, AtomicLong read, AtomicLong write, int mask) {
			this.array = array;
			this.read = read;
			this.mask = mask;
			this.wPos = write.get();
			this.rPos = 0;
		}

		@Override
		public boolean hasMoreElements() {
			while (obj == null) {
				if (rPos < read.get())
					rPos = read.get();
				if (rPos < wPos) {
					obj = array.get((int) (rPos & mask));
					++rPos;
				} else
					return false;
			}
			return true;
		}

		@Override
		public E nextElement() {
			E tmp = obj;
			obj = null;
			return tmp;
		}
	}
}
