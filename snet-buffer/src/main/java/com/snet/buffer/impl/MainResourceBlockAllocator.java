package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.MathUtil;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MainResourceBlockAllocator implements SNetResourceBlockAllocator {
	protected final CoreResourceBlockAllocator parentAllocator;
	protected final ConcurrentLinkedQueue<TreeResourceBlock> freeBlocks;
	protected final int allocateBlockCapacity;
	protected final int cellCapacity;
	protected final BlockList<TreeResourceBlock>[] blockLists;

	public MainResourceBlockAllocator(CoreResourceBlockAllocator parentAllocator, int allocateBlockCapacity,
			int cellCapacity) {
		this.parentAllocator = parentAllocator;
		this.freeBlocks = new ConcurrentLinkedQueue<>();
		this.allocateBlockCapacity = allocateBlockCapacity;
		this.cellCapacity = cellCapacity;
		this.blockLists = BlockList.newBlockList();
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		capacity = capacity < cellCapacity ? cellCapacity : MathUtil.ceil2(capacity);
		if (capacity < allocateBlockCapacity) {
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
			SNetResourceBlock rawBlock = parentAllocator.allocate(allocateBlockCapacity);
			treeBlock = new TreeResourceBlock(this, rawBlock, cellCapacity);
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
