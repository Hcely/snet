package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.buffer.util.SNetBlockSet;
import com.snet.util.coll.FixedQueue;

public class AssistantResourceBlockAllocator implements SNetResourceBlockAllocator {
	protected MainResourceBlockAllocator parentAllocator;
	protected SNetBlockSet<DefAllocatableResourceBlock> header;
	protected SNetBlockSet<DefAllocatableResourceBlock>[] blockSets;
	protected FixedQueue<SNetResourceBlock>[] caches;


	@Override
	public void recycle(SNetResourceBlock block) {

	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		return null;
	}
}
