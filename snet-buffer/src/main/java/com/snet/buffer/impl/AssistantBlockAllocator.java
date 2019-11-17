package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.coll.FixedQueue;

public class AssistantBlockAllocator implements SNetResourceBlockAllocator {
	public static final int MIN_CAPACITY = 1 << 6;
	public static final int BLOCK_CAPACITY = 1 << 12;

	@SuppressWarnings("unchecked")
	private static BlockList<FixedResourceBlock>[] newBlockLists() {
		BlockList<FixedResourceBlock> list0_0 = new BlockList<>(0, 0);
		BlockList<FixedResourceBlock> list1_30 = new BlockList<>(list0_0, 30);
		BlockList<FixedResourceBlock> list31_70 = new BlockList<>(list1_30, 70);
		BlockList<FixedResourceBlock> list71_99 = new BlockList<>(list31_70, 99);
		BlockList<FixedResourceBlock> list100 = new BlockList<>(list71_99, 100);
		return new BlockList[]{list31_70, list1_30, list71_99, list100};
	}

	protected final MainBlockAllocator parentAllocator;
	protected final BlockList<FixedResourceBlock>[][] blockLists;
	protected final FixedQueue<SNetResourceBlock>[] caches;

	@SuppressWarnings("unchecked")
	public AssistantBlockAllocator(MainBlockAllocator parentAllocator) {
		this.parentAllocator = parentAllocator;
		this.blockLists = new BlockList[6][];
		this.caches = new FixedQueue[6];
		for (int i = 0; i < 6; ++i) {
			blockLists[i] = newBlockLists();
			caches[i] = new FixedQueue<>(64);
		}
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {

		return null;
	}

	@Override
	public void recycle(SNetResourceBlock block) {

	}


}
