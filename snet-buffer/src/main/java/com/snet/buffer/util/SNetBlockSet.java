package com.snet.buffer.util;

import com.snet.buffer.SNetAllocatableResourceBlock;

import java.util.LinkedList;

public class SNetBlockSet<T extends SNetAllocatableResourceBlock> {
	protected SNetBlockSet<T> next;
	protected final int threshold;
	protected final LinkedList<T> blocks;

	public SNetBlockSet(int threshold) {
		this.threshold = threshold;
		this.blocks = new LinkedList<>();
	}

	public SNetBlockSet<T> getNext() {
		return next;
	}

	public SNetBlockSet<T> setNext(SNetBlockSet<T> next) {
		this.next = next;
		return this;
	}

	public int getThreshold() {
		return threshold;
	}

	public LinkedList<T> getBlocks() {
		return blocks;
	}
}
