package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.resource.SNetResource;
import com.snet.buffer.resource.SNetBufferResourceFactory;
import com.snet.util.MathUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

class CenterBlockArena extends SNetAbsBlockArena {
	protected final SNetBufferResourceFactory resourceFactory;
	protected volatile int blockSize;
	protected final ConcurrentLinkedQueue<CenterBufferBlock> blocks;
	protected final int blockCapacity;

	public CenterBlockArena(SNetBlockArena parent, SNetBufferResourceFactory resourceFactory, int blockCapacity) {
		super(parent);
		this.resourceFactory = resourceFactory;
		this.blockSize = 0;
		this.blocks = new ConcurrentLinkedQueue<>();
		this.blockCapacity = MathUtil.ceil2(blockCapacity);
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity <= blockCapacity;
	}

	@Override
	public SNetBlock allocate0(int capacity) {
		for (SNetBlock result; ; ) {
			if ((result = allocateImpl(capacity)) != null)
				return result;
			if ((result = allocateOrCreate(capacity)) != null)
				return result;
			Thread.yield();
		}
	}

	private SNetBlock allocateImpl(int capacity) {
		int size = 0, blockSize = this.blockSize;
		for (Iterator<CenterBufferBlock> it = null; size < blockSize; ) {
			if ((it == null || !it.hasNext()) && !(it = blocks.iterator()).hasNext())
				break;
			CenterBufferBlock block = it.next();
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
					SNetResource resource = resourceFactory.create(blockCapacity);
					CenterBufferBlock block = new CenterBufferBlock(resource, this);
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
		CenterBufferBlock cBlock = (CenterBufferBlock) block.getParent();
		try {
			cBlock.lock();
			cBlock.recycle(block);
		} finally {
			cBlock.unlock();
		}
	}

	public void releaseBlock() {
		final List<CenterBufferBlock> idleBlocks = new LinkedList<>();
		final long idleDeadline = System.currentTimeMillis() - 10000;
		for (CenterBufferBlock block : blocks) {
			if (block.enableReleased() && block.getLastUsingTime() < idleDeadline)
				idleBlocks.add(block);
		}

		if (idleBlocks.size() > 0) {
			for (CenterBufferBlock block : idleBlocks) {
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
				for (Iterator<CenterBufferBlock> it = blocks.iterator(); it.hasNext(); ) {
					CenterBufferBlock block = it.next();
					if (block.isReleased()) {
						--blockSize;
						it.remove();
					}
				}
			} finally {
				lock.unlock();
			}
		}
	}
}
