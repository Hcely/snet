package com.snet.buffer.block.level;

import com.snet.Releasable;
import com.snet.buffer.block.DefBufferBlock;
import com.snet.buffer.block.SNetAllocatableBufferBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBufferBlock;
import com.snet.buffer.resource.SNetBufferResource;

import java.util.BitSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CenterBufferBlock extends DefBufferBlock implements SNetAllocatableBufferBlock, Releasable {
	protected final BitSet bitmap;
	protected final int bitSize;
	protected final int cellLen;
	protected final int cellLenShift;
	protected long lastUsingTime;
	protected int remaining;
	protected int remainCell;
	protected final Lock lock;

	public CenterBufferBlock(int cellLen, SNetBufferResource resource, SNetBlockArena arena) {
		super(0, resource.getCapacity(), resource, arena, null);
		this.cellLenShift = 32 - Integer.numberOfLeadingZeros(cellLen - 1);
		this.cellLen = 1 << cellLenShift;
		this.bitSize = capacity >>> cellLenShift;
		this.remaining = capacity;
		this.remainCell = bitSize;
		this.bitmap = new BitSet(bitSize);
		this.lastUsingTime = System.currentTimeMillis();
		this.lock = new ReentrantLock();
	}

	@Override
	public SNetBufferBlock allocate(int capacity) {
		if (released)
			return null;
		int len = capacity >>> cellLenShift;
		if ((len << cellLenShift) < capacity)
			++len;
		if ((len << cellLenShift) > remaining)
			return null;
		final BitSet bitmap = this.bitmap;
		for (int i = 0, size = bitSize, max = size - len + 1; i < max; ++i) {
			for (int off = i, count = 0; i < size && !bitmap.get(i); ++i) {
				if (++count == len) {
					int subCapacity = len << cellLenShift;
					bitmap.set(off, off + len);
					remaining += subCapacity;
					remainCell -= len;
					lastUsingTime = System.currentTimeMillis();
					return new DefBufferBlock(resourceOffset + (off << cellLenShift), subCapacity, arena, this);
				}
			}
		}
		return null;
	}

	@Override
	public void recycle(SNetBufferBlock block) {
		if (block.getParent() == this) {
			int off = (block.getResourceOffset() - resourceOffset) >>> cellLenShift;
			int len = block.getCapacity() >>> cellLenShift;
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

	public Lock getLock() {
		return lock;
	}
}
