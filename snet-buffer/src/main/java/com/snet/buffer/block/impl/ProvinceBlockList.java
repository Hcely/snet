package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockAllocator;

class ProvinceBlockList implements SNetBlockAllocator {
	protected ProvinceBlockList prev, next;
	protected ProvinceBlock head, tail;
	protected int size;
	protected int threshold;

	@Override
	public SNetBlock allocate(int capacity) {
		for (ProvinceBlock n = head; n != null; n = n.next) {
			SNetBlock result = n.allocate(capacity);
			if (result != null) {
				if (n.remaining < threshold && next != null) {
					remove(n);
					next.add(n);
				}
				return result;
			}
		}
		return null;
	}

	public boolean add(ProvinceBlock block) {
		if (block.list != null)
			return false;
		if (tail == null) {
			head = block;
			tail = block;
			block.prev = null;
			block.next = null;
		} else {
			tail.next = block;
			block.prev = tail;
			block.next = null;
			tail = block;
		}
		block.list = this;
		++size;
		return true;
	}

	boolean remove(ProvinceBlock block) {
		if (block.list != this)
			return false;
		final ProvinceBlock prev = block.prev, next = block.next;
		block.list = null;
		block.prev = null;
		block.next = null;
		if (prev == null) {
			head = next;
		} else {
			prev.next = next;
		}
		if (next == null) {
			tail = prev;
		} else {
			next.prev = prev;
		}
		--size;
		return true;
	}

}