package com.snet.buffer.impl;

import com.snet.ResourceManager;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.buffer.SNetResourceManager;

import java.util.concurrent.ConcurrentLinkedQueue;

public class CoreResourceBlockAllocator implements SNetResourceBlockAllocator, ResourceManager {
	protected long blockCapacity;
	protected SNetResourceManager resourceManager;
	protected ConcurrentLinkedQueue<DefAllocatableResourceBlock> freeBlocks;
	protected BlockList<DefAllocatableResourceBlock> header;
	protected BlockList<DefAllocatableResourceBlock>[] blockSets;

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
