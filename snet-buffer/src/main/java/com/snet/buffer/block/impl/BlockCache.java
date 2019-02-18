package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlock;
import com.snet.util.FixedQueue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BlockCache {
	protected final FixedQueue<SNetBlock> cache;
	protected final long idleTime;
	protected long lastUsingTime;

	public BlockCache(int capacity, long idleTime) {
		this.cache = new FixedQueue<>(capacity);
		this.idleTime = idleTime;
		this.lastUsingTime = System.currentTimeMillis();
	}

	public boolean add(SNetBlock block) {
		return cache.add(block);
	}

	public SNetBlock poll() {
		SNetBlock bufferBlock = cache.poll();
		lastUsingTime = System.currentTimeMillis();
		return bufferBlock;
	}

	public void recycleCache(List<SNetBlock> list, int factor) {
		if (lastUsingTime + idleTime > System.currentTimeMillis())
			return;
		int size = cache.size();
		if (size == 0)
			return;
		size = factor > 0 ? factor : (factor == 0 ? Integer.MAX_VALUE : Math.min(size / (-factor), 4));
		for (int i = 0; i < size; ++i) {
			SNetBlock block = cache.poll();
			if (block == null)
				break;
			list.add(block);
		}
	}
}
