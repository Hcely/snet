package com.snet.buffer.block.impl;

import com.snet.buffer.block.BlockArenaUtil;
import com.snet.buffer.block.DefBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.util.BPTreeMap;
import com.snet.util.MathUtil;

import java.util.concurrent.ConcurrentLinkedQueue;

class AreaBlockArena extends SNetAbsBlockArena {
	protected static final BPTreeMap.KeyComparator<AreaBlock.Cell, Object> REMAIN_COMPARATOR = (o1, o2) -> {
		final int remain1 = o1.remaining;
		if (o2 instanceof Integer)
			return remain1 < (Integer) o2 ? -1 : 1;
		final int remain2 = ((AreaBlock.Cell) o2).remaining;
		return remain1 < remain2 ? -1 : (remain1 == remain2 ? 0 : 1);
	};

	public static final int MAX_SHIFT = 18;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;
	public static final int BLOCK_LEN = 1 << 20;
	protected final BlockCache[] caches;
	protected final ConcurrentLinkedQueue<AreaBlock> blocks;
	protected final BPTreeMap<AreaBlock.Cell, Void> sortBlocks;

	public AreaBlockArena(SNetBlockArena parent) {
		super(parent);
		final int len = MAX_SHIFT - BlockArenaUtil.MIN_SHIFT;
		this.caches = new BlockCache[len];
		this.blocks = new ConcurrentLinkedQueue<>();
		this.sortBlocks = new BPTreeMap<>(2, REMAIN_COMPARATOR, BPTreeMap.IDENTITY_EQUALS);
		for (int i = 0; i < len; ++i)
			caches[i] = new BlockCache(128 * (len - i), 10000);
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
		return cell.combineBlock.allocate(cell, capacity);
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

	}

}
