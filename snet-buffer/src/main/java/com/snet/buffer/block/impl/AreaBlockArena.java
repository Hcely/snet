package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockArenaUtil;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBlock;

import java.util.TreeSet;

class AreaBlockArena extends SNetAbsBlockArena {
	public static final int MIN_SHIFT = 8;
	public static final int MAX_SHIFT = 18;
	public static final int MIN_CAPACITY = 1 << MIN_SHIFT;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;
	protected final BlockCache[] caches;
	protected final TreeSet<AreaBlock> blocks;

	public AreaBlockArena(SNetBlockArena parent) {
		super(parent);
		final int len = MAX_SHIFT - MIN_SHIFT;
		this.caches = new BlockCache[len];
		this.blocks = new TreeSet<>();
		for (int i = 0; i < len; ++i)
			caches[i] = new BlockCache(128 * (len - i), 10000);
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < MAX_CAPACITY + 1;
	}

	@Override
	protected SNetBlock allocate0(int capacity) {
		final int idx = BlockArenaUtil.getIdx(capacity);
		SNetBlock block = caches[idx].poll();
		if (block != null)
			return block;
		return null;
	}

	@Override
	public void recycle(SNetBlock block) {
		int idx = BlockArenaUtil.getIdx(block.getCapacity());
		if (!caches[idx].add(block)) {

		}
	}

	@Override
	public void releaseBlock() {

	}

}
