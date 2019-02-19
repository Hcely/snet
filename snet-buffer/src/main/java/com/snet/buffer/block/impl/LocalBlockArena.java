package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockArenaUtil;
import com.snet.buffer.block.BlockCache;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;

import java.util.LinkedList;
import java.util.List;

public class LocalBlockArena extends AbstractCacheBlockArena {
	public static final int MAX_SHIFT = 13;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;
	protected final Thread thread;
	protected boolean alive;

	public LocalBlockArena(BlockArenaManager manager, SNetBlockArena parent) {
		super(manager, parent, BlockArenaUtil.getCaches(MAX_SHIFT - BlockArenaUtil.MIN_SHIFT, 8, 64));
		this.thread = Thread.currentThread();
		this.alive = true;
	}

	public Thread getThread() {
		return thread;
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < MAX_CAPACITY + 1;
	}

	@Override
	protected SNetBlock allocate1(int capacity) {
		return new ProxyBlock(this, parent.allocate(capacity));
	}

	@Override
	public void recycle(SNetBlock block) {
		if (!alive || manager.released || !putCache(block))
			recycle0(block);
	}

	@Override
	protected void recycle0(SNetBlock block) {
		block.release();
		manager.recycleBlock(((ProxyBlock) block).getBlock());
	}

	public boolean isAlive() {
		return alive;
	}

	@Override
	public void trimArena() {
		if (thread.isAlive() && !manager.released)
			trimCache(System.currentTimeMillis() - manager.getLocalIdleTime(), -2);
		else {
			alive = false;
			trimCache(0, 0);
		}
	}

	protected void trimCache(long deadline, int factor) {
		List<SNetBlock> list = new LinkedList<>();
		for (BlockCache cache : caches)
			cache.recycleCache(list, deadline, factor);
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
