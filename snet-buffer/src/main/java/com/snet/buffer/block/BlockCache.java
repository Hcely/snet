package com.snet.buffer.block;

import com.snet.util.coll.FixedQueue;

import java.util.List;

public class BlockCache {
	protected final FixedQueue<SNetBlock> cache;

	public BlockCache(int capacity) {
		this.cache = new FixedQueue<>(capacity);
	}

	public boolean add(SNetBlock block) {
		return cache.add(block);
	}

	public SNetBlock poll() {
		return cache.poll();
	}

	public void recycleCache(List<SNetBlock> list, int factor) {
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
