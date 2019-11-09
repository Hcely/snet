package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;

class BlockList<T extends BlockListNode<T>> {
	protected BlockList<T> prev;
	protected BlockList<T> next;
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

	public BlockList<T> getPrev() {
		return prev;
	}

	public BlockList<T> setPrev(BlockList<T> prev) {
		this.prev = prev;
		return this;
	}

	public BlockList<T> getNext() {
		return next;
	}

	public BlockList<T> setNext(BlockList<T> next) {
		this.next = next;
		return this;
	}

	public void addBlock(T block) {
		addBlock(block.getRemainPercent(), block);
	}

	private void addBlock(int percent, T block) {
		if (percent > maxThreshold && next != null) {
			next.addBlock(percent, block);
			return;
		}
		if (percent < minThreshold && prev != null) {
			prev.addBlock(percent, block);
			return;
		}
		block.remove();
		block.setList(this);
		block.setNext(header);
		if (header != null) {
			header.setPrev(block);
		}
		header = block;
	}

	public SNetResourceBlock allocate(int capacity) {
		T node = header;
		for (; node != null; node = node.getNext()) {
			SNetResourceBlock block = node.allocate(capacity);
			if (block != null) {
				addBlock(node);
			}
		}
		return null;
	}
}
