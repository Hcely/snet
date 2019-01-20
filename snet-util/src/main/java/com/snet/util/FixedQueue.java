package com.snet.util;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class FixedQueue<E> {
	protected final AtomicReferenceArray<E> array;
	protected final AtomicLong read, write, limit;
	protected final int capacity;
	protected final int mask;

	public FixedQueue(int capacity) {
		capacity = CollUtil.ceil2(capacity);
		this.array = new AtomicReferenceArray<>(capacity);
		this.read = new AtomicLong(0);
		this.write = new AtomicLong(0);
		this.limit = new AtomicLong(capacity);
		this.capacity = capacity;
		this.mask = capacity - 1;
	}

	public boolean add(E e) {
		if (e == null)
			throw new NullPointerException("element is null");
		AtomicReferenceArray<E> array = this.array;
		AtomicLong write = this.write, limit = this.limit;
		int mask = this.mask, idx;
		long lPos = 0, wPos;
		while (true) {
			if ((wPos = write.get()) < lPos || wPos < (lPos = limit.get())) {
				if (array.get(idx = (int) (mask & wPos)) == null && write.compareAndSet(wPos, wPos + 1)) {
					array.set(idx, e);
					return true;
				}
				Thread.yield();
			} else
				return false;
		}
	}

	public E first() {
		AtomicReferenceArray<E> array = this.array;
		AtomicLong read = this.read, write = this.write;
		int mask = this.mask, idx;
		long rPos, wPos = 0;
		while (true) {
			if ((rPos = read.get()) < wPos || rPos < (wPos = write.get())) {
				E result = array.get(idx = (int) (mask & rPos));
				if (result != null)
					return result;
				Thread.yield();
			} else
				return null;
		}
	}

	public E poll() {
		AtomicReferenceArray<E> array = this.array;
		AtomicLong read = this.read, write = this.write;
		int mask = this.mask, idx;
		long rPos, wPos = 0;
		while (true) {
			if ((rPos = read.get()) < wPos || rPos < (wPos = write.get())) {
				E result = array.get(idx = (int) (mask & rPos));
				if (result != null && read.compareAndSet(rPos, rPos + 1)) {
					array.set(idx, null);
					limit.incrementAndGet();
					return result;
				}
				Thread.yield();
			} else
				return null;
		}
	}

	public int getCapacity() {
		return capacity;
	}

	public int size() {
		return (int) (write.get() - read.get());
	}

	public boolean isEmpty() {
		return write.get() == read.get();
	}


	public Enumeration<E> enumeration() {
		return new En<>(array, read, write, mask);
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
