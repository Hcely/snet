package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.buffer.util.SNetBlockSet;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MainResourceBlockAllocator implements SNetResourceBlockAllocator {
	protected final CoreResourceBlockAllocator parentAllocator;
	protected final ConcurrentLinkedQueue<TreeResourceBlock> freeBlocks;
	protected final SNetBlockSet<TreeResourceBlock>[] blockSets;
	protected final SNetBlockSet<TreeResourceBlock>[] allocateBlockSets;

	@SuppressWarnings("unchecked")
	public MainResourceBlockAllocator(CoreResourceBlockAllocator parentAllocator) {
		this.parentAllocator = parentAllocator;
		this.freeBlocks = new ConcurrentLinkedQueue<>();
		this.blockSets = new SNetBlockSet[5];
		this.allocateBlockSets = new SNetBlockSet[5];
		blockSets[0] = new SNetBlockSet<>(0, 2);
		blockSets[1] = new SNetBlockSet<>(3, 5);
		blockSets[2] = new SNetBlockSet<>(6, 11);
		blockSets[3] = new SNetBlockSet<>(12, 14);
		blockSets[4] = new SNetBlockSet<>(14, 16);
		

	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		return null;
	}

	@Override
	public void recycle(SNetResourceBlock block) {

	}


}
