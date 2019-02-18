package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.resource.SNetResource;
import com.snet.util.MathUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CenterBlockArena extends SNetAbsBlockArena {
	protected volatile int blockSize;
	protected final ConcurrentLinkedQueue<CenterBlock> blocks;
	protected final int blockCapacity;
	protected final int threshold;
	protected final Lock lock;

	public CenterBlockArena(ArenaManager manager, int blockCapacity) {
		super(manager, null);
		this.blockSize = 0;
		this.blocks = new ConcurrentLinkedQueue<>();
		this.blockCapacity = MathUtil.ceil2(blockCapacity);
		this.threshold = this.blockCapacity + 1;
		this.lock = new ReentrantLock();
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < threshold;
	}

	@Override
	public SNetBlock allocate0(int capacity) {
		for (SNetBlock result; !released; ) {
			if ((result = allocateImpl(capacity)) != null)
				return result;
			if ((result = allocateOrCreate(capacity)) != null)
				return result;
			Thread.yield();
		}
		return null;
	}

	private SNetBlock allocateImpl(int capacity) {
		int size = 0, blockSize = this.blockSize;
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
		if (lock.tryLock())
			try {
				SNetBlock result = allocateImpl(capacity);
				if (result == null) {
					SNetResource resource = manager.createResource(blockCapacity);
					CenterBlock block = new CenterBlock(resource, this);
					blocks.add(block);
					++blockSize;
				}
				return result;
			} finally {
				lock.unlock();
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
		}
	}

	public void releaseBlock() {
		final List<CenterBlock> idleBlocks = new LinkedList<>();
		final long idleDeadline = System.currentTimeMillis() - manager.getCenterIdleTime();
		for (CenterBlock block : blocks) {
			if (block.enableReleased() && block.getLastUsingTime() < idleDeadline)
				idleBlocks.add(block);
		}
		if (idleBlocks.isEmpty()) return;
		for (CenterBlock block : idleBlocks) {
			if (block.tryLock()) {
				try {
					if (block.getLastUsingTime() < idleDeadline && block.enableReleased())
						block.release();
				} finally {
					block.unlock();
				}
			}
		}
		try {
			lock.lock();
			for (Iterator<CenterBlock> it = blocks.iterator(); it.hasNext(); ) {
				CenterBlock block = it.next();
				if (block.isReleased()) {
					--blockSize;
					it.remove();
				}
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void release() {
		if (released)
			return;
		released = true;
		releaseBlock();
	}
}
