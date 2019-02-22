package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockCache;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;

public class ProvinceArena extends AbstractBlockArena {
	public static final int MAX_SHIFT = 20;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;

	public ProvinceArena(BlockArenaManager manager, SNetBlockArena parent) {
		super(manager, parent);
	}

	@Override
	protected boolean supports(int capacity) {
		return false;
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
