package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockCache;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;

public class ProvinceArena extends AbstractBlockArena {
	private static final int MIN_SHIFT = 13;
	public static final int MIN_CAPACITY = 1 << MIN_SHIFT;
	private static final int MAX_SHIFT = 20;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;
	protected BlockCache[] caches;

	public ProvinceArena(BlockArenaManager manager, SNetBlockArena parent) {
		super(manager, parent);
		caches = new BlockCache[MAX_CAPACITY - MIN_SHIFT + 1];
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < MAX_CAPACITY + 1;
	}

	@Override
	protected SNetBlock allocate0(int capacity) {

		return null;
	}

	@Override
	public void recycle(SNetBlock block) {

	}

	@Override
	public void trimArena() {

	}

}
