package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.coll.FixedQueue;

public class AssistantBlockAllocator implements SNetResourceBlockAllocator {
	public static final int MIN_CAPACITY = 1 << 6;
	public static final int BLOCK_CAPACITY = 1 << 12;

	private static BlockList<FixedResourceBlock>[] newBlockLists() {
		return null;
	}

	protected MainBlockAllocator parentAllocator;
	protected FixedResourceBlock[] blockList;
	protected FixedQueue<SNetResourceBlock>[] caches;

	@Override
	public SNetResourceBlock allocate(int capacity) {

		return null;
	}

	@Override
	public void recycle(SNetResourceBlock block) {

	}


}
