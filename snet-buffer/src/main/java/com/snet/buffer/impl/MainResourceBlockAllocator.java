package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MainResourceBlockAllocator implements SNetResourceBlockAllocator {
	protected final CoreResourceBlockAllocator parentAllocator;
	protected final ConcurrentLinkedQueue<TreeResourceBlock> freeBlocks;
	protected final int allocateBlockCapacity;
	protected final int cellCapacity;
	protected final BlockList<TreeResourceBlock>[] blockSets;
	protected final BlockList<TreeResourceBlock>[] allocateBlockSets;

	@SuppressWarnings("unchecked")
	public MainResourceBlockAllocator(CoreResourceBlockAllocator parentAllocator, int allocateBlockCapacity,
			int cellCapacity) {
		this.parentAllocator = parentAllocator;
		this.freeBlocks = new ConcurrentLinkedQueue<>();
		this.allocateBlockCapacity = allocateBlockCapacity;
		this.cellCapacity = cellCapacity;
		this.blockSets = new BlockList[9];
		this.allocateBlockSets = new BlockList[9];
		allocateBlockSets[1] = blockSets[0] = new BlockList<>(0, 0);
		allocateBlockSets[2] = blockSets[1] = new BlockList<>(1, 8);
		allocateBlockSets[3] = blockSets[2] = new BlockList<>(8, 16);
		allocateBlockSets[4] = blockSets[3] = new BlockList<>(16, 23);
		allocateBlockSets[0] = blockSets[4] = new BlockList<>(24, 40);
		allocateBlockSets[5] = blockSets[5] = new BlockList<>(41, 48);
		allocateBlockSets[6] = blockSets[6] = new BlockList<>(49, 56);
		allocateBlockSets[7] = blockSets[7] = new BlockList<>(57, 63);
		allocateBlockSets[8] = blockSets[8] = new BlockList<>(64, 64);
	}

	@Override
	public synchronized SNetResourceBlock allocate(int capacity) {
		for (BlockList<TreeResourceBlock> list : allocateBlockSets) {
			SNetResourceBlock block = list.allocate(capacity);
			if (block != null) {
				return block;
			}
		}
		TreeResourceBlock treeBlock = allocateTreeBlock();
		SNetResourceBlock block = treeBlock.allocate(capacity);
		allocateBlockSets[8].addBlock(treeBlock);
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
