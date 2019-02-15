package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockArenaUtil;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;

import java.util.LinkedList;
import java.util.List;

public class LocalBlockArena extends SNetAbsBlockArena {
	public static final int MAX_SHIFT = 12;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;
	protected final Thread thread;
	protected final BlockCache[] caches;

	public LocalBlockArena(SNetBlockArena parent) {
		super(parent);
		this.thread = Thread.currentThread();
		this.caches = new BlockCache[MAX_SHIFT - BlockArenaUtil.MIN_SHIFT];
		for (int i = 0, len = MAX_SHIFT - BlockArenaUtil.MIN_SHIFT; i < len; ++i)
			this.caches[i] = new BlockCache(64 >>> i, 5000);
	}

	public Thread getThread() {
		return thread;
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < MAX_CAPACITY + 1;
	}

	@Override
	protected SNetBlock allocate0(int capacity) {
		final int idx = BlockArenaUtil.getIdx(capacity);
		BlockCache cache = caches[idx];
		SNetBlock block = cache.poll();
		return block == null ? parent.allocate(capacity) : block;
	}

	@Override
	public void recycle(SNetBlock block) {
		final int idx = BlockArenaUtil.getIdx(block.getCapacity());
		if (!caches[idx].add(block))
			parent.recycle(block);
	}

	@Override
	public void releaseBlock() {
		List<SNetBlock> list = new LinkedList<>();
		for (BlockCache cache : caches)
			cache.recycleCache(list);
		for (SNetBlock block : list)
			parent.recycle(block);
	}
}
