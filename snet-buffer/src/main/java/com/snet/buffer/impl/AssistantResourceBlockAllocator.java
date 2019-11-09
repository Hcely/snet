package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.coll.FixedQueue;

public class AssistantResourceBlockAllocator implements SNetResourceBlockAllocator {
	protected MainResourceBlockAllocator parentAllocator;
	protected FixedResourceBlock[] blockList;
	protected FixedQueue<SNetResourceBlock>[] caches;

	@Override
	public void recycle(SNetResourceBlock block) {

	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		return null;
	}
}
