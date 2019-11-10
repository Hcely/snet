package com.snet.buffer.impl;

import com.snet.ResourceManager;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.buffer.SNetResourceManager;
import com.snet.util.coll.FixedQueue;

public class CoreBlockAllocator implements SNetResourceBlockAllocator, ResourceManager {
	public static final long BLOCK_CAPACITY = 1 << 23;
	public static final int MAX_FREE_BLOCK = 64;

	@SuppressWarnings("unchecked")
	private static BlockList<DefAllocatableResourceBlock>[] newBlockLists() {
		BlockList<DefAllocatableResourceBlock> prev = new BlockList<>(0, 0);
		BlockList<DefAllocatableResourceBlock>[] result = new BlockList[10];
		for (int i = 0; i < 10; ++i) {
			result[i] = new BlockList<>(prev, (i + 1) * 10);
			prev = result[i];
		}
		return result;
	}

	protected final SNetResourceManager resourceManager;
	protected final FixedQueue<DefAllocatableResourceBlock> freeBlocks;
	protected final BlockList<DefAllocatableResourceBlock>[] blockLists;

	public CoreBlockAllocator(SNetResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		this.freeBlocks = new FixedQueue<>(MAX_FREE_BLOCK);
		this.blockLists = newBlockLists();
	}


	@Override
	public SNetResourceBlock allocate(int capacity) {


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
