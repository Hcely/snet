package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.MathUtil;
import com.snet.util.coll.FixedQueue;

public class MainBlockAllocator implements SNetResourceBlockAllocator {
	public static final int ALLOCATE_BLOCK_CAPACITY = 1 << 20;
	public static final int CELL_CAPACITY = 1 << 13;

	@SuppressWarnings("unchecked")
	private static BlockList<TreeResourceBlock>[] newBlockList() {
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

	public MainBlockAllocator(CoreBlockAllocator parentAllocator) {
		this.parentAllocator = parentAllocator;
		this.freeBlocks = new FixedQueue<>(32);
		this.blockLists = newBlockList();
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
		for (BlockList<TreeResourceBlock> list : blockLists) {
			SNetResourceBlock block = list.allocate(capacity);
			if (block != null) {
				return block;
			}
		}
		TreeResourceBlock treeBlock = allocateTreeBlock();
		SNetResourceBlock block = treeBlock.allocate(capacity);
		blockLists[blockLists.length - 1].addBlock(treeBlock);
		return block;
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
		TreeResourceBlock treeBlock = (TreeResourceBlock) block.getParent();
		treeBlock.recycle(block);
		BlockList<TreeResourceBlock> blockList = treeBlock.list;
		blockList.addBlock(treeBlock);
	}
}
