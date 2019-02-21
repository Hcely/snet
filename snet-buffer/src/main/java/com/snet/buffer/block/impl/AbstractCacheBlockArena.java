package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockArenaUtil;
import com.snet.buffer.block.BlockCache;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractCacheBlockArena extends AbstractBlockArena {
	public AbstractCacheBlockArena(BlockArenaManager manager, SNetBlockArena parent) {
		super(manager, parent);
	}

	protected SNetBlock allocate0(int capacity) {
		SNetBlock block = pullCache(capacity);
		if (block != null)
			return block;
		return allocate1(capacity);
	}

	@Override
	public void recycle(SNetBlock block) {
		if (manager.released || !putCache(block))
			recycle0(block);
	}

	protected abstract SNetBlock pullCache(int capacity);

	protected abstract boolean putCache(SNetBlock block);

	protected abstract SNetBlock allocate1(int capacity);

	protected abstract void recycle0(SNetBlock block);

}
