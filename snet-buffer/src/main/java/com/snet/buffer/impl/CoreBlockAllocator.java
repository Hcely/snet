package com.snet.buffer.impl;

import com.snet.ResourceManager;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.buffer.SNetResourceManager;
import com.snet.util.coll.FixedQueue;

public class CoreBlockAllocator implements SNetResourceBlockAllocator, ResourceManager {
	public static final long BLOCK_CAPACITY = 1 << 24;
	public static final long MAX_ALLOCATE_CAPACITY = 1 << 22;
	public static final int MAX_FREE_BLOCK = 1 << 8;

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
		if (capacity > MAX_ALLOCATE_CAPACITY) {

		}

		return null;
	}

	@Override
	public void recycle(SNetResourceBlock block) {

	}


	@Override
	public int recycleResources() {
		return 0;
	}
}
