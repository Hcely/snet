package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockArenaUtil;
import com.snet.buffer.block.DefBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.util.BPTreeMap;
import com.snet.util.MathUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

class AreaBlockArena extends SNetAbsBlockArena {
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
	protected final BlockCache[] caches;
	protected final ConcurrentLinkedQueue<AreaBlock> blocks;
	protected final BPTreeMap<AreaBlock.Cell, Void> sortBlocks;

	public AreaBlockArena(ArenaManager manager, SNetBlockArena parent) {
		super(manager, parent);
		final int len = MAX_SHIFT - BlockArenaUtil.MIN_SHIFT;
		this.caches = new BlockCache[len];
		this.blocks = new ConcurrentLinkedQueue<>();
		this.sortBlocks = new BPTreeMap<>(2, REMAIN_COMPARATOR, BPTreeMap.IDENTITY_EQUALS);
		for (int i = 0; i < len; ++i)
			caches[i] = new BlockCache(Math.min(16, 8192 >>> i));
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < MAX_CAPACITY + 1;
	}

	@Override
	public SNetBlock allocate(int capacity) {
		if (supports(capacity))
			return allocate0(MathUtil.ceil2(capacity));
		return parent.allocate(capacity);
	}

	@Override
	protected SNetBlock allocate0(int capacity) {
		if (released)
			return null;
		int idx = BlockArenaUtil.getIdx(capacity);
		SNetBlock block = caches[idx].poll();
		if (block != null)
			return block;
		if (++idx < MAX_SHIFT - BlockArenaUtil.MIN_SHIFT && (block = caches[idx].poll()) != null) {
			SNetBlock newBlock = new DefBlock(block.getResourceOffset(), capacity, block.getResource(), block.getArena(), block.getParent());
			recycle(newBlock);
			block = new DefBlock(block.getResourceOffset() + capacity, capacity, block.getResource().duplicate(), block.getArena(), block.getParent());
			return block;
		}
		return allocate1(capacity);
	}

	protected synchronized SNetBlock allocate1(int capacity) {
		final BPTreeMap.LeafNode<AreaBlock.Cell, Void> node = sortBlocks.ceilEntity(capacity);
		AreaBlock.Cell cell = node == null ? create() : node.getKey();
		return cell.allocate(capacity);
	}

	protected AreaBlock.Cell create() {
		SNetBlock block = parent.allocate(BLOCK_LEN);
		AreaBlock areaBlock = new AreaBlock(this, block);
		blocks.add(areaBlock);
		return areaBlock.firstCell();
	}

	protected BPTreeMap.LeafNode<AreaBlock.Cell, Void> addSort(AreaBlock.Cell block) {
		return sortBlocks.getEntity(block, true);
	}

	@Override
	public void recycle(SNetBlock block) {
		int idx = BlockArenaUtil.getIdx(block.getCapacity());
		if (!caches[idx].add(block))
			recycle0(block);
	}

	protected synchronized void recycle0(SNetBlock block) {
		AreaBlock areaBlock = (AreaBlock) block.getParent();
		areaBlock.recycle(block);
	}

	@Override
	public void releaseBlock() {
		long deadline = System.currentTimeMillis() - manager.getAreaIdleTime();
		releaseCache(deadline, -2);
		releaseAreaBlock(deadline, true);
	}

	protected void releaseCache(long deadline, int factor) {
		List<SNetBlock> list = new LinkedList<>();
		for (BlockCache cache : caches)
			cache.recycleCache(list, deadline, factor);
		for (SNetBlock block : list)
			recycle0(block);
	}

	protected void releaseAreaBlock(long deadline, boolean async) {
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
		if (blocks.isEmpty())
			return;
		if (async)
			manager.recycleBlocks(blocks);
		else {
			for (SNetBlock e : blocks)
				e.recycle();
		}
	}

	protected synchronized void releaseAreaBlocks(List<AreaBlock> releaseBlocks) {
		for (AreaBlock block : releaseBlocks) {
			if (block.enableReleased())
				block.release();
		}
	}

	@Override
	public void release() {
		if (released)
			return;
		released = true;
		releaseCache(0, 0);
		releaseAreaBlock(0, false);
	}
}
