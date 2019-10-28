package com.snet.buffer.util;

import com.snet.buffer.SNetAllocatableResourceBlock;

import java.util.LinkedList;

public class SNetBlockSet<T extends SNetAllocatableResourceBlock> {
	protected final int minThreshold;
	protected final int maxThreshold;
	protected final LinkedList<T> blocks;

	public SNetBlockSet(int minThreshold, int maxThreshold) {
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
