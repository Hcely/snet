package com.snet.buffer.block.level;

import com.snet.buffer.block.SNetBlock;
import com.snet.util.FixedQueue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BlockCaches {
	protected final FixedQueue<SNetBlock> cache;
	protected final long idleTime;
	protected long lastUsingTime;

	public BlockCaches(int capacity, long idleTime) {
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

	public List<SNetBlock> recycleCache() {
		if (lastUsingTime + idleTime > System.currentTimeMillis())
			return Collections.emptyList();
		int size = cache.size();
		if (size == 0)
			return Collections.emptyList();
		List<SNetBlock> list = new LinkedList<>();
		size = Math.min(size >>> 1, 4);
		for (int i = 0; i < size; ++i) {
			SNetBlock block = cache.poll();
			if (block == null)
				break;
			list.add(block);
		}
		return list;
	}

}
