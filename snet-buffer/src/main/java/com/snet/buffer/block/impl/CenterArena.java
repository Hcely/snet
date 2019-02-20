package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.resource.SNetResource;
import com.snet.util.MathUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CenterArena extends AbstractBlockArena {
	public static final int MAX_SHIFT = 24;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;//16mb
	protected final ConcurrentHashMap<SNetBlock, Void> blocks;

	public CenterArena(BlockArenaManager manager, int blockCapacity) {
		super(manager, null);
		this.blockSize = new AtomicInteger(0);
		this.blocks = new ConcurrentHashMap<>();
		this.blockCapacity = MathUtil.ceil2(blockCapacity);
		this.threshold = this.blockCapacity + 1;
		this.lock = new ReentrantLock();
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < MAX_CAPACITY + 1;
	}

	@Override
	public SNetBlock allocate0(int capacity) {
		SNetResource resource = manager.createResource(blockCapacity);
		
	}

	private SNetBlock allocateImpl(int capacity) {
		int size = 0, blockSize = this.blockSize.get() + 2;
		for (Iterator<CenterBlock> it = null; size < blockSize; ) {
			if ((it == null || !it.hasNext()) && !(it = blocks.iterator()).hasNext())
				break;
			CenterBlock block = it.next();
			if (block.tryLock()) {
				try {
					if (!block.isReleased()) {
						SNetBlock result = block.allocate(capacity);
						if (result != null)
							return result;
					}
				} finally {
					++size;
					block.unlock();
				}
			}
		}
		return null;
	}

	private SNetBlock allocateOrCreate(int capacity) {
		if (lock.tryLock()) {
			try {
				SNetBlock result = allocateImpl(capacity);
				if (result != null)
					return result;
				SNetResource resource = manager.createResource(blockCapacity);
				CenterBlock block = new CenterBlock(resource, this);
				blocks.add(block);
				blockSize.incrementAndGet();
				result = block.allocate(capacity);
				return result;
			} finally {
				lock.unlock();
			}
		}
		return null;
	}

	@Override
	public void recycle(SNetBlock block) {
		CenterBlock cBlock = (CenterBlock) block.getParent();
		try {
			cBlock.lock();
			block.release();
			cBlock.recycle(block);
		} finally {
			cBlock.unlock();
			manager.decBlockCount();
		}
	}

	@Override
	public void trimArena() {
	}
}
