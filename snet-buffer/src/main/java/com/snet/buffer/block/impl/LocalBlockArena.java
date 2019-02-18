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

	public LocalBlockArena(ArenaManager manager, SNetBlockArena parent) {
		super(manager, parent);
		this.thread = Thread.currentThread();
		this.caches = new BlockCache[MAX_SHIFT - BlockArenaUtil.MIN_SHIFT];
		for (int i = 0, len = MAX_SHIFT - BlockArenaUtil.MIN_SHIFT; i < len; ++i)
			this.caches[i] = new BlockCache(64 >>> i, manager.getLocalIdleTime());
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
		if (block != null)
			return block;
		block = new ProxyBlock(this, parent.allocate(capacity));
		return block;
	}

	@Override
	public void recycle(SNetBlock block) {
		if (released || !caches[BlockArenaUtil.getIdx(block.getCapacity())].add(block)) {
			block.release();
			manager.recycleBlock(((ProxyBlock) block).getBlock());
		}
	}

	@Override
	public void releaseBlock() {
		if (thread.isAlive())
			releaseBlock0(-2);
		else
			release();
	}

	@Override
	public void release() {
		released = true;
		releaseBlock0(0);
	}

	protected void releaseBlock0(int factor) {
		List<SNetBlock> list = new LinkedList<>();
		for (BlockCache cache : caches)
			cache.recycleCache(list, factor);
		if (list.isEmpty())
			return;
		List<SNetBlock> releaseList = new LinkedList<>();
		for (SNetBlock block : list) {
			block.release();
			releaseList.add(((ProxyBlock) block).getBlock());
		}
		manager.recycleBlocks(releaseList);
	}
}
