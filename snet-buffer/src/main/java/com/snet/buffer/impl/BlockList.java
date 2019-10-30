package com.snet.buffer.impl;

import com.snet.buffer.SNetAllocatableResourceBlock;

import java.util.LinkedList;

public class BlockList<T extends SNetAllocatableResourceBlock> {
	protected final int minThreshold;
	protected final int maxThreshold;
	protected final LinkedList<T> blocks;

	public BlockList(int minThreshold, int maxThreshold) {
		this.minThreshold = minThreshold;
		this.maxThreshold = maxThreshold;
		this.blocks = new LinkedList<>();
	}

	public int getMinThreshold() {
		return minThreshold;
	}

	public int getMaxThreshold() {
		return maxThreshold;
	}

	public LinkedList<T> getBlocks() {
		return blocks;
	}
}
