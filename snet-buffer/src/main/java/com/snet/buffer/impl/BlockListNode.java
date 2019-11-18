package com.snet.buffer.impl;

import com.snet.buffer.SNetAllocatableResourceBlock;
import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;

abstract class BlockListNode<T extends BlockListNode<T>> extends DefResourceBlock
		implements SNetAllocatableResourceBlock {
	protected final SNetResourceBlock rawBlock;
	T prev;
	T next;
	BlockList<T> list;
	protected int remainCapacity;
	protected final int hundredthCapacity;
	protected final int cellCapacity;

	public BlockListNode(SNetResourceBlockAllocator allocator, SNetResource resource, long resourceOff, int capacity,
			int cellCapacity) {
		super(allocator, null, resource, resourceOff, capacity);
		this.rawBlock = null;
		this.remainCapacity = capacity;
		this.hundredthCapacity = capacity / 100;
		this.cellCapacity = cellCapacity;
	}


	BlockListNode(SNetResourceBlockAllocator allocator, SNetResourceBlock rawBlock, int cellCapacity) {
		super(allocator, rawBlock.getParent(), rawBlock.getResource(), rawBlock.getResourceOff(),
				rawBlock.getCapacity());
		this.rawBlock = rawBlock;
		this.remainCapacity = capacity;
		this.hundredthCapacity = capacity / 100;
		this.cellCapacity = cellCapacity;
	}

	public int getRemainPercent() {
		int percent = remainCapacity;
		if (percent < cellCapacity) {
			return 0;
		}
		percent /= hundredthCapacity;
		return percent == 0 ? 1 : percent;
	}

	public int getCellCapacity() {
		return cellCapacity;
	}

	public SNetResourceBlock getRawBlock() {
		return rawBlock;
	}

	@Override
	public int getRemainCapacity() {
		return remainCapacity;
	}


	void remove() {
		if (list != null) {
			if (prev != null) {
				prev.next = next;
			} else {
				list.header = next;
			}
			if (next != null) {
				next.prev = prev;
			}
			prev = null;
			next = null;
			list = null;
		}
	}

	BlockList<T> getList() {
		return list;
	}
}
