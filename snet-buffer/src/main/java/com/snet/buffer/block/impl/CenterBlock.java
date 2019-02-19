package com.snet.buffer.block.impl;

import com.snet.Releasable;
import com.snet.buffer.block.DefBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.resource.SNetResource;

import java.util.BitSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CenterBlock extends DefBlock implements Releasable {
	public static final int CELL_LEN_SHIFT = 10;
	public static final int CELL_LEN = 1 << CELL_LEN_SHIFT;

	protected final BitSet bitmap;
	protected final int bitSize;
	protected final Lock lock;
	protected long lastUsingTime;
	protected int remaining;
	protected int remainCell;

	public CenterBlock(SNetResource resource, SNetBlockArena arena) {
		super(0, resource.getCapacity(), resource, arena, null);
		this.bitSize = capacity >>> CELL_LEN_SHIFT;
		this.remaining = capacity;
		this.remainCell = bitSize;
		this.bitmap = new BitSet(bitSize);
		this.lastUsingTime = System.currentTimeMillis();
		this.lock = new ReentrantLock();
	}

	public SNetBlock allocate(int capacity) {
		if (released)
			return null;
		int len = capacity >>> CELL_LEN_SHIFT;
		if ((len << CELL_LEN_SHIFT) < capacity)
			++len;
		if (len > remainCell)
			return null;
		final BitSet bitmap = this.bitmap;
		for (int i = 0, max = bitSize - len + 1; i < max; ++i) {
			if (isFree(bitmap, i, len)) {
				int subCapacity = len << CELL_LEN_SHIFT;
				bitmap.set(i, i + len, true);
				remaining += subCapacity;
				remainCell -= len;
				lastUsingTime = System.currentTimeMillis();
				return new DefBlock(resourceOffset + (i << CELL_LEN_SHIFT), subCapacity, arena, this);
			}
		}
		return null;
	}

	protected static boolean isFree(BitSet bitmap, int off, int len) {
		for (len += off; off < len; ++off)
			if (bitmap.get(off))
				return false;
		return true;
	}

	public void recycle(SNetBlock block) {
		int off = (block.getResourceOffset() - resourceOffset) >>> CELL_LEN_SHIFT;
		int len = block.getCapacity() >>> CELL_LEN_SHIFT;
		bitmap.clear(off, off + len);
		remainCell += len;
		remaining += block.getCapacity();
		lastUsingTime = System.currentTimeMillis();
	}

	public int getRemaining() {
		return remaining;
	}

	public boolean enableReleased() {
		return remaining == capacity;
	}

	@Override
	public void release() {
		if (!enableReleased())
			throw new RuntimeException("");
		super.release();
	}

	public long getLastUsingTime() {
		return lastUsingTime;
	}

	public int getRemainCell() {
		return remainCell;
	}

	public void lock() {
		lock.lock();
	}

	public boolean tryLock() {
		return lock.tryLock();
	}

	public void unlock() {
		lock.unlock();
	}
}
