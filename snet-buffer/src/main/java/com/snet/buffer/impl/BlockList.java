package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlock;

class BlockList<T extends BlockListNode<T>> {
	@SuppressWarnings("unchecked")
	public static <T extends BlockListNode<T>> BlockList<T>[] newBlockList() {
		BlockList<T> percent0 = new BlockList<>(0, 0);
		BlockList<T> percent1_10 = new BlockList<>(percent0, 10);
		BlockList<T> percent11_20 = new BlockList<>(percent1_10, 20);
		BlockList<T> percent21_40 = new BlockList<>(percent11_20, 40);
		BlockList<T> percent41_60 = new BlockList<>(percent21_40, 60);
		BlockList<T> percent61_80 = new BlockList<>(percent41_60, 80);
		BlockList<T> percent81_90 = new BlockList<>(percent61_80, 90);
		BlockList<T> percent91_100 = new BlockList<>(percent81_90, 100);
		return new BlockList[]{percent41_60, percent1_10, percent11_20, percent21_40, percent61_80, percent81_90,
				percent91_100};
	}

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

	public BlockList(BlockList<T> prev, int maxThreshold) {
		this(prev.maxThreshold + 1, maxThreshold);
		this.prev = prev;
		prev.next = this;
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
