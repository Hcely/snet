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
	protected static final BPTreeMap.KeyComparator<AreaBlock.Cell, Object> REMAIN_COMPARATOR = (o1, o2) -> {
		final int remain1 = o1.remaining;
		if (o2 instanceof Integer)
			return remain1 < (Integer) o2 ? -1 : 1;
		final int remain2 = ((AreaBlock.Cell) o2).remaining;
		return remain1 < remain2 ? -1 : (remain1 == remain2 ? 0 : 1);
	};

	public static final int MAX_SHIFT = 20;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;
	public static final int BLOCK_LEN = 1 << 21;

	protected final ConcurrentLinkedQueue<AreaBlock> blocks;
	protected final BPTreeMap<AreaBlock.Cell, Void> sortBlocks;

	public AreaBlockArena(BlockArenaManager manager, SNetBlockArena parent) {
		super(manager, parent, BlockArenaUtil.getCaches(MAX_SHIFT - BlockArenaUtil.MIN_SHIFT, 16, 8192));
		this.blocks = new ConcurrentLinkedQueue<>();
		this.sortBlocks = new BPTreeMap<>(2, REMAIN_COMPARATOR, BPTreeMap.IDENTITY_EQUALS);
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < MAX_CAPACITY + 1;
	}

	@Override
	protected SNetBlock allocate1(int capacity) {
		try {
			synchronized (this) {
				final BPTreeMap.LeafNode<AreaBlock.Cell, Void> node = sortBlocks.ceilEntity(capacity);
				AreaBlock.Cell cell;
				if (node != null)
					cell = node.getKey();
				else {
					SNetBlock block = parent.allocate(BLOCK_LEN);
					AreaBlock areaBlock = new AreaBlock(this, block);
					blocks.add(areaBlock);
					cell = areaBlock.firstCell();
				}
				return cell.allocate(capacity);
			}
		} finally {
			manager.incBlockCount();
		}
	}

	protected BPTreeMap.LeafNode<AreaBlock.Cell, Void> addSort(AreaBlock.Cell block) {
		return sortBlocks.getEntity(block, true);
	}

	@Override
	protected void recycle0(SNetBlock block) {
		try {
			synchronized (this) {
				AreaBlock areaBlock = (AreaBlock) block.getParent();
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
