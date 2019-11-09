package com.snet.buffer.impl;

import com.snet.buffer.SNetAllocatableResourceBlock;
import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;

abstract class BlockListNode<T extends BlockListNode<T>> extends DefResourceBlock
		implements SNetAllocatableResourceBlock {
	protected T prev;
	protected T next;
	protected BlockList<T> list;
	protected int remainCapacity;
	protected final int hundredthCapacity;

	BlockListNode(SNetResourceBlock parent, SNetResource resource, long resourceOff, int capacity) {
		super(parent, resource, resourceOff, capacity);
		this.remainCapacity = capacity;
		this.hundredthCapacity = capacity / 100;
	}

	public int getRemainPercent() {
		int percent = remainCapacity;
		if (percent == 0) {
			return 0;
		}
		percent /= hundredthCapacity;
		return percent == 0 ? 1 : percent;
	}

	@Override
	public int getRemainCapacity() {
		return remainCapacity;
	}

	void setPrev(T prev) {
		this.prev = prev;
	}

	void setNext(T next) {
		this.next = next;
	}

	void setList(BlockList<T> list) {
		this.list = list;
	}

	T getPrev() {
		return prev;
	}

	T getNext() {
		return next;
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
		}
	}

	BlockList<T> getList() {
		return list;
	}
}
