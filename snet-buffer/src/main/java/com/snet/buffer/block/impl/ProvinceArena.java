package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockCache;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;

public class ProvinceArena extends AbstractCacheBlockArena {
	public static final int MAX_SHIFT = 20;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;

	public ProvinceArena(BlockArenaManager manager, SNetBlockArena parent, BlockCache[] caches) {
		super(manager, parent, caches);
	}

	@Override
	protected boolean supports(int capacity) {
		return false;
	}

	@Override
	protected SNetBlock allocate1(int capacity) {
		return null;
	}

	@Override
	protected void recycle0(SNetBlock block) {

	}

	@Override
	public void trimArena() {

	}
}
