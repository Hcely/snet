package com.snet.buffer.impl;

public class BlockList<T extends LinkedBlock<T>> {
	protected final int minThreshold;
	protected final int maxThreshold;
	protected T header;

	public BlockList(int minThreshold, int maxThreshold) {
		this.minThreshold = minThreshold;
		this.maxThreshold = maxThreshold;
		this.header = null;
	}

	public int getMinThreshold() {
		return minThreshold;
	}

	public int getMaxThreshold() {
		return maxThreshold;
	}

	public T getHeader() {
		return header;
	}

	public void addBlock() {

	}
}
