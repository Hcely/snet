package com.snet.buffer.impl;

import com.snet.ResourceManager;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.MathUtil;
import com.snet.util.coll.FixedQueue;

public class AssistantBlockAllocator implements SNetResourceBlockAllocator, ResourceManager {
	public static final int MIN_CAPACITY = 1 << 6;
	public static final int MAX_CAPACITY = 1 << 11;
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
	protected final FixedQueue<SNetResourceBlock> freeBlocks;
	protected final FixedQueue<SNetResourceBlock>[] caches;
	protected long lastAllocateTime;
	protected long lastRecycleTime;

	@SuppressWarnings("unchecked")
	public AssistantBlockAllocator(MainBlockAllocator parentAllocator) {
		this.parentAllocator = parentAllocator;
		this.blockLists = new BlockList[6][];
		this.caches = new FixedQueue[6];
		this.freeBlocks = new FixedQueue<>(32);
		this.lastAllocateTime = System.currentTimeMillis();
		this.lastRecycleTime = lastAllocateTime;
		for (int i = 0; i < 6; ++i) {
			blockLists[i] = newBlockLists();
			caches[i] = new FixedQueue<>(128);
		}
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		if (capacity > MAX_CAPACITY) {
			return parentAllocator.allocate(capacity);
		}
		try {
			return allocate0(capacity);
		} finally {
			this.lastAllocateTime = System.currentTimeMillis();
		}
	}

	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	private SNetResourceBlock allocate0(int capacity) {
		int idx = capacity < MIN_CAPACITY ? 0 : MathUtil.ceilLog2(capacity) - 6;
		SNetResourceBlock block = caches[idx].poll();
		if (block != null) {
			return block;
		}
		final BlockList<FixedResourceBlock>[] blockLists = this.blockLists[idx];
		capacity = 1 << (idx + 6);
		synchronized (blockLists) {
			block = BlockList.allocate(blockLists, capacity);
			if (block == null) {
				FixedResourceBlock fixedBlock = getFixedBlock(capacity);
				block = fixedBlock.allocate(capacity);
				blockLists[blockLists.length - 1].addBlock(fixedBlock);
			}
			return block;
		}
	}

	private FixedResourceBlock getFixedBlock(int capacity) {
		SNetResourceBlock rawBlock = freeBlocks.poll();
		if (rawBlock == null) {
			rawBlock = parentAllocator.allocate(BLOCK_CAPACITY);
		}
		return new FixedResourceBlock(this, rawBlock, capacity);
	}

	@Override
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public void recycle(SNetResourceBlock block) {
		try {
			final int idx = MathUtil.ceilLog2(block.getCapacity()) - 6;
			if (caches[idx].add(block)) {
				return;
			}
			BlockList<FixedResourceBlock>[] blockLists = this.blockLists[idx];
			synchronized (blockLists) {
				FixedResourceBlock fixedBlock = (FixedResourceBlock) block.getParent();
				fixedBlock.recycle(block);
				BlockList<FixedResourceBlock> blockList = fixedBlock.list;
				blockList.addBlock(fixedBlock);
			}
		} finally {
			this.lastRecycleTime = System.currentTimeMillis();
		}
	}

	@Override
	public int recycleResources() {

		return 0;
	}
}
