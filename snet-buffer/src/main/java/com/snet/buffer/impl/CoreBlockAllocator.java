package com.snet.buffer.impl;

import com.snet.ResourceManager;
import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.buffer.SNetResourceManager;
import com.snet.util.coll.FixedQueue;

public class CoreBlockAllocator implements SNetResourceBlockAllocator, ResourceManager {
	public static final int BLOCK_CAPACITY = 1 << 24;
	public static final int CELL_CAPACITY = 1 << 16;
	public static final int MAX_ALLOCATE_CAPACITY = 1 << 22;
	public static final int MAX_FREE_BLOCK = BLOCK_CAPACITY / CELL_CAPACITY;

	@SuppressWarnings("unchecked")
	private static BlockList<BitmapBlock>[] newBlockLists() {
		BlockList<BitmapBlock> prev = new BlockList<>(0, 0);
		BlockList<BitmapBlock>[] result = new BlockList[10];
		for (int i = 0; i < 10; ++i) {
			result[i] = new BlockList<>(prev, (i + 1) * 10);
			prev = result[i];
		}
		return result;
	}

	protected final SNetResourceManager resourceManager;
	protected final FixedQueue<BitmapBlock> freeBlocks;
	protected final BlockList<BitmapBlock>[] blockLists;

	public CoreBlockAllocator(SNetResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		this.freeBlocks = new FixedQueue<>(MAX_FREE_BLOCK);
		this.blockLists = newBlockLists();
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		capacity = fixedCapacity(capacity);
		if (capacity > MAX_ALLOCATE_CAPACITY) {
			SNetResource resource = resourceManager.allocate(capacity);
			return new DefResourceBlock(resource, 0, capacity);
		} else {
			return allocateImpl(capacity);
		}
	}

	protected synchronized SNetResourceBlock allocateImpl(int capacity) {
		SNetResourceBlock block = BlockList.allocate(blockLists, capacity);
		if (block == null) {
			BitmapBlock bitmapBlock = allocateBitmapBlock();
			block = bitmapBlock.allocate(capacity);
			blockLists[blockLists.length - 1].addBlock(bitmapBlock);
		}
		return block;
	}

	private BitmapBlock allocateBitmapBlock() {
		BitmapBlock bitmapBlock = freeBlocks.poll();
		if (bitmapBlock == null) {
			SNetResource resource = resourceManager.allocate(BLOCK_CAPACITY);
			bitmapBlock = new BitmapBlock(this, resource, CELL_CAPACITY);
		}
		return bitmapBlock;
	}

	@Override
	public synchronized void recycle(SNetResourceBlock block) {
		BitmapBlock bitmapBlock = (BitmapBlock) block.getParent();
		bitmapBlock.recycle(block);
		bitmapBlock.list.addBlock(bitmapBlock);
	}

	@Override
	public int recycleResources() {

		return 0;
	}

	protected int fixedCapacity(int capacity) {
		int result = (capacity >>> 16) << 16;
		return result < capacity ? result + CELL_CAPACITY : result;
	}


}
