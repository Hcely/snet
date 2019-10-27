package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.buffer.util.SNetBlockSet;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MainResourceBlockAllocator implements SNetResourceBlockAllocator {
	protected final CoreResourceBlockAllocator parentAllocator;
	protected final ConcurrentLinkedQueue<TreeResourceBlock> freeBlocks;
	protected final SNetBlockSet<TreeResourceBlock>[] blockSets;
	protected final SNetBlockSet<TreeResourceBlock> header;

	@SuppressWarnings("unchecked")
	public MainResourceBlockAllocator(CoreResourceBlockAllocator parentAllocator) {
		this.parentAllocator = parentAllocator;
		this.freeBlocks = new ConcurrentLinkedQueue<>();
		this.blockSets = new SNetBlockSet[10];
		for (int i = 0; i < 10; ++i) {
			blockSets[i] = new SNetBlockSet<>(i);
		}
		blockSets[0].setNext(blockSets[1]);
		blockSets[5].setNext(blockSets[1]);
		blockSets[1].setNext(blockSets[2]);
		blockSets[2].setNext(blockSets[3]);
		blockSets[3].setNext(blockSets[4]);
		blockSets[4].setNext(blockSets[6]);
		blockSets[6].setNext(blockSets[7]);
		blockSets[7].setNext(blockSets[8]);
		blockSets[8].setNext(blockSets[9]);
		this.header = blockSets[5];
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		return null;
	}

	@Override
	public void recycle(SNetResourceBlock block) {

	}


}
