package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockCache;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.util.MathUtil;

import java.util.LinkedList;
import java.util.List;

public class LocalBlockArena extends AbstractBlockArena {
	private static final int MIN_SHIFT = 7;
	public static final int MIN_CAPACITY = 1 << MIN_SHIFT;
	private static final int MAX_SHIFT = 13;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;

	public static final int getIdx(int capacity) {
		if (capacity < MIN_CAPACITY + 1)
			return 0;
		return MathUtil.ceilLog2(capacity) - MIN_SHIFT;
	}

	protected final Thread thread;
	protected BlockCache[] caches;
	protected boolean alive;

	public LocalBlockArena(BlockArenaManager manager, SNetBlockArena parent) {
		super(manager, parent);
		this.thread = Thread.currentThread();
		this.caches = new BlockCache[MAX_SHIFT - MIN_SHIFT + 1];
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
	protected SNetBlock allocate0(int capacity) {
		SNetBlock block = caches[getIdx(capacity)].poll();
		return block == null ? allocateImpl(capacity) : block;
	}

	protected SNetBlock allocateImpl(int capacity) {
		SNetBlock block = parent.allocate(capacity);
		return new ProxyBlock(this, block);
	}

	@Override
	public void recycle(SNetBlock block) {
		if (manager.released || block.getArena() != this || !caches[getIdx(block.getCapacity())].add(block))
			block.getArena().getParent().recycle(block);
	}

	public boolean isAlive() {
		return alive;
	}

	@Override
	public void trimArena() {
		if (!thread.isAlive() || manager.released) {
			alive = false;
			trimCache(0);
		} else if (lastUsingTime < System.currentTimeMillis() - manager.getLocalIdleTime()) {
			trimCache(-2);
		}
	}

	protected void trimCache(int factor) {
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
