package com.snet.buffer.impl;

import com.snet.ResourceManager;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.MathUtil;
import com.snet.util.coll.FixedQueue;

public class MainBlockAllocator implements SNetResourceBlockAllocator, ResourceManager {
	public static final int ALLOCATE_BLOCK_CAPACITY = 1 << 20;
	public static final int CELL_CAPACITY = 1 << 13;

	@SuppressWarnings("unchecked")
	private static BlockList<TreeResourceBlock>[] newBlockLists() {
		BlockList<TreeResourceBlock> percent0 = new BlockList<>(0, 0);
		BlockList<TreeResourceBlock> percent1_10 = new BlockList<>(percent0, 10);
		BlockList<TreeResourceBlock> percent11_20 = new BlockList<>(percent1_10, 20);
		BlockList<TreeResourceBlock> percent21_40 = new BlockList<>(percent11_20, 40);
		BlockList<TreeResourceBlock> percent41_60 = new BlockList<>(percent21_40, 60);
		BlockList<TreeResourceBlock> percent61_80 = new BlockList<>(percent41_60, 80);
		BlockList<TreeResourceBlock> percent81_90 = new BlockList<>(percent61_80, 90);
		BlockList<TreeResourceBlock> percent91_100 = new BlockList<>(percent81_90, 100);
		return new BlockList[]{percent41_60, percent1_10, percent11_20, percent21_40, percent61_80, percent81_90,
				percent91_100};
	}

	protected final CoreBlockAllocator parentAllocator;
	protected final FixedQueue<TreeResourceBlock> freeBlocks;
	protected final BlockList<TreeResourceBlock>[] blockLists;
	protected long lastAllocateTime;
	protected long lastRecycleTime;

	public MainBlockAllocator(CoreBlockAllocator parentAllocator) {
		this.parentAllocator = parentAllocator;
		this.freeBlocks = new FixedQueue<>(16);
		this.blockLists = newBlockLists();
		this.lastAllocateTime = System.currentTimeMillis();
		this.lastRecycleTime = this.lastAllocateTime;
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		capacity = capacity < CELL_CAPACITY ? CELL_CAPACITY : MathUtil.ceil2(capacity);
		if (capacity < ALLOCATE_BLOCK_CAPACITY) {
			return allocate0(capacity);
		}
		return parentAllocator.allocate(capacity);
	}

	private synchronized SNetResourceBlock allocate0(int capacity) {
		try {
			SNetResourceBlock block = BlockList.allocate(blockLists, capacity);
			if (block == null) {
				TreeResourceBlock treeBlock = allocateTreeBlock();
				block = treeBlock.allocate(capacity);
				blockLists[blockLists.length - 1].addBlock(treeBlock);
			}
			return block;
		} finally {
			this.lastAllocateTime = System.currentTimeMillis();
		}
	}

	private TreeResourceBlock allocateTreeBlock() {
		TreeResourceBlock treeBlock = freeBlocks.poll();
		if (treeBlock == null) {
			SNetResourceBlock rawBlock = parentAllocator.allocate(ALLOCATE_BLOCK_CAPACITY);
			treeBlock = new TreeResourceBlock(this, rawBlock, CELL_CAPACITY);
		}
		return treeBlock;
	}

	@Override
	public synchronized void recycle(SNetResourceBlock block) {
		try {
			TreeResourceBlock treeBlock = (TreeResourceBlock) block.getParent();
			treeBlock.recycle(block);
			BlockList<TreeResourceBlock> blockList = treeBlock.list;
			blockList.addBlock(treeBlock);
		} finally {
			this.lastRecycleTime = System.currentTimeMillis();
		}
	}

	@Override
	public int recycleResources() {

		return 0;
	}
}
