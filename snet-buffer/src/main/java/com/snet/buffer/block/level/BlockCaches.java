package com.snet.buffer.block.level;

import com.snet.buffer.block.SNetBlock;
import com.snet.util.FixedQueue;

abstract class BlockCaches {
	protected final int blockCapacity;
	protected final FixedQueue<SNetBlock> cache;
	protected final long idleTime;
	protected long lastUsingTime;

	public BlockCaches(int blockCapacity, int capacity, long idleTime) {
		this.blockCapacity = blockCapacity;
		this.cache = new FixedQueue<>(capacity);
		this.idleTime = idleTime;
		this.lastUsingTime = System.currentTimeMillis();
	}

	public void add(SNetBlock block) {
		if (!cache.add(block))
			recycleCachedBlock(block);
	}

	public SNetBlock poll() {
		SNetBlock bufferBlock = cache.poll();
		lastUsingTime = System.currentTimeMillis();
		return bufferBlock;
	}

	public void recycleCache() {
		if (lastUsingTime + idleTime > System.currentTimeMillis())
			return;
		int size = cache.size();
		if (size == 0)
			return;
		size = Math.min(size >>> 1, 4);
		for (int i = 0; i < size; ++i) {
			SNetBlock block = cache.poll();
			if (block == null)
				break;
			recycleCachedBlock(block);
		}
	}

	protected abstract void recycleCachedBlock(SNetBlock block);

}
