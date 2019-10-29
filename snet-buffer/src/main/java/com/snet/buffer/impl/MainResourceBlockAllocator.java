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
		this.blockSets = new SNetBlockSet[9];
		this.allocateBlockSets = new SNetBlockSet[9];
		allocateBlockSets[1] = blockSets[0] = new SNetBlockSet<>(0, 0);
		allocateBlockSets[2] = blockSets[1] = new SNetBlockSet<>(1, 8);
		allocateBlockSets[3] = blockSets[2] = new SNetBlockSet<>(8, 16);
		allocateBlockSets[4] = blockSets[3] = new SNetBlockSet<>(16, 23);
		allocateBlockSets[0] = blockSets[4] = new SNetBlockSet<>(24, 40);
		allocateBlockSets[5] = blockSets[5] = new SNetBlockSet<>(41, 48);
		allocateBlockSets[6] = blockSets[6] = new SNetBlockSet<>(49, 56);
		allocateBlockSets[7] = blockSets[7] = new SNetBlockSet<>(57, 63);
		allocateBlockSets[8] = blockSets[8] = new SNetBlockSet<>(64, 64);
	}

	@Override
	public synchronized SNetResourceBlock allocate(int capacity) {
		return null;
	}

	@Override
	public synchronized void recycle(SNetResourceBlock block) {

	}
}
