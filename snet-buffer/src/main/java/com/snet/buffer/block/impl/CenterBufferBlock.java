package com.snet.buffer.block.impl;

import com.snet.Releasable;
import com.snet.buffer.block.DefBufferBlock;
import com.snet.buffer.block.SNetAllocatableBufferBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.resource.SNetResource;

import java.util.BitSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CenterBufferBlock extends DefBufferBlock implements SNetAllocatableBufferBlock, Releasable {
	public static final int CELL_LEN_SHIFT = 10;
	public static final int CELL_LEN = 1 << CELL_LEN_SHIFT;

	protected final BitSet bitmap;
	protected final int bitSize;
	protected long lastUsingTime;
	protected int remaining;
	protected int remainCell;
	protected final Lock lock;

	public CenterBufferBlock(SNetResource resource, SNetBlockArena arena) {
		super(0, resource.getCapacity(), resource, arena, null);
		this.bitSize = capacity >>> CELL_LEN_SHIFT;
		this.remaining = capacity;
		this.remainCell = bitSize;
		this.bitmap = new BitSet(bitSize);
		this.lastUsingTime = System.currentTimeMillis();
		this.lock = new ReentrantLock();
	}

	@Override
	public SNetBlock allocate(int capacity) {
		if (released)
			return null;
		int len = capacity >>> CELL_LEN_SHIFT;
		if ((len << CELL_LEN_SHIFT) < capacity)
			++len;
		if (len > remainCell)
			return null;
		final BitSet bitmap = this.bitmap;
		for (int i = 0, size = bitSize, max = size - len + 1; i < max; ++i) {
			for (int off = i, count = 0; i < size && !bitmap.get(i); ++i) {
				if (++count == len) {
					int subCapacity = len << CELL_LEN_SHIFT;
					bitmap.set(off, off + len);
					remaining += subCapacity;
					remainCell -= len;
					lastUsingTime = System.currentTimeMillis();
					return new DefBufferBlock(resourceOffset + (off << CELL_LEN_SHIFT), subCapacity, arena, this);
				}
			}
		}
		return null;
	}

	@Override
	public void recycle(SNetBlock block) {
		if (block.getParent() == this) {
			int off = (block.getResourceOffset() - resourceOffset) >>> CELL_LEN_SHIFT;
			int len = block.getCapacity() >>> CELL_LEN_SHIFT;
			bitmap.clear(off, off + len);
			remainCell += len;
			remaining += block.getCapacity();
			lastUsingTime = System.currentTimeMillis();
		}
	}

	@Override
	public void reset() {
		bitmap.clear();
		this.remaining = capacity;
		this.remainCell = bitSize;
		this.lastUsingTime = System.currentTimeMillis();
	}

	@Override
	public int getRemaining() {
		return remaining;
	}

	@Override
	public boolean enableReleased() {
		return remaining == capacity && !released;
	}

	public long getLastUsingTime() {
		return lastUsingTime;
	}

	@Override
	public void release() {
		if (released)
			return;
		resource.release();
		released = true;
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
