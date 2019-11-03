package com.snet.buffer.impl;

import com.snet.buffer.SNetAllocatableResourceBlock;
import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;

public abstract class LinkedBlock<T extends LinkedBlock<T>> extends DefResourceBlock
		implements SNetAllocatableResourceBlock {
	protected T prev;
	protected T next;
	protected BlockList<T> list;

	public LinkedBlock(SNetResource resource, long resourceOff, int capacity) {
		super(resource, resourceOff, capacity);
	}

	public LinkedBlock(SNetResourceBlock parent, SNetResource resource, long resourceOff, int capacity) {
		super(parent, resource, resourceOff, capacity);
	}

	public void setPrev(T prev) {
		this.prev = prev;
	}

	public void setNext(T next) {
		this.next = next;
	}

	public void setList(BlockList<T> list) {
		this.list = list;
	}

	public T getPrev() {
		return prev;
	}

	public T getNext() {
		return next;
	}

	public BlockList<T> getList() {
		return list;
	}
}
