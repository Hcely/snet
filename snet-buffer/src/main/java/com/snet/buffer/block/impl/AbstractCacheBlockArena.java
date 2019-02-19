package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockArenaUtil;
import com.snet.buffer.block.BlockCache;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractCacheBlockArena extends AbstractBlockArena {
	protected final BlockCache[] caches;

	public AbstractCacheBlockArena(BlockArenaManager manager, SNetBlockArena parent, BlockCache[] caches) {
		super(manager, parent);
		this.caches = caches;
	}

	protected SNetBlock allocate0(int capacity) {
		SNetBlock block = pollCache(capacity);
		if (block != null)
			return block;
		return allocate1(capacity);
	}

	protected SNetBlock pollCache(int capacity) {
		return caches[BlockArenaUtil.getIdx(capacity)].poll();
	}

	protected abstract SNetBlock allocate1(int capacity);

	@Override
	public void recycle(SNetBlock block) {
		if (manager.released || !putCache(block))
			recycle0(block);
	}

	protected boolean putCache(SNetBlock block) {
		return caches[BlockArenaUtil.getIdx(block.getCapacity())].add(block);
	}

	protected abstract void recycle0(SNetBlock block);

}
