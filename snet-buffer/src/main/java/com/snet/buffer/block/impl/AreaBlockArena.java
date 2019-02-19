package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockArenaUtil;
import com.snet.buffer.block.BlockCache;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.util.BPTreeMap;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

class AreaBlockArena extends AbstractCacheBlockArena {
	public static final int MAX_SHIFT = 20;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;
	public static final int BLOCK_LEN = 1 << 21;

	protected final ConcurrentLinkedQueue<AreaBlock> blocks;

	public AreaBlockArena(BlockArenaManager manager, SNetBlockArena parent) {
		super(manager, parent, BlockArenaUtil.getCaches(MAX_SHIFT - BlockArenaUtil.MIN_SHIFT, 16, 8192));
		this.blocks = new ConcurrentLinkedQueue<>();
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < MAX_CAPACITY + 1;
	}

	@Override
	protected SNetBlock allocate1(int capacity) {
		try {
			return null;
		} finally {
			manager.incBlockCount();
		}
	}

	@Override
	protected void recycle0(SNetBlock block) {
		try {
			block.release();
			final AreaBlock areaBlock = (AreaBlock) block.getParent();
			synchronized (areaBlock) {
				areaBlock.recycle(block);
			}
		} finally {
			manager.decBlockCount();
		}
	}

	@Override
	public void trimArena() {
		final long deadline = manager.released ? 0 : System.currentTimeMillis() - manager.getAreaIdleTime();
		final int factor = manager.released ? 0 : -2;
		trimCache(deadline, factor);
		trimBlock(deadline);
	}

	protected void trimCache(long deadline, int factor) {
		List<SNetBlock> list = new LinkedList<>();
		for (BlockCache cache : caches)
			cache.recycleCache(list, deadline, factor);
		for (SNetBlock block : list)
			recycle0(block);
	}

	protected void trimBlock(long deadline) {
		List<AreaBlock> list = new LinkedList<>();
		for (AreaBlock block : blocks) {
			if (block.enableReleased() && block.getLastUsingTime() < deadline)
				list.add(block);
		}
		if (list.isEmpty())
			return;
		releaseAreaBlocks(list);
		List<SNetBlock> blocks = new LinkedList<>();
		for (AreaBlock block : list) {
			if (block.isReleased())
				blocks.add(block.getBlock());
		}
		manager.recycleBlocks(blocks);
	}

	protected synchronized void releaseAreaBlocks(List<AreaBlock> releaseBlocks) {
		for (AreaBlock block : releaseBlocks) {
			if (block.enableReleased())
				block.release();
		}
	}
}
