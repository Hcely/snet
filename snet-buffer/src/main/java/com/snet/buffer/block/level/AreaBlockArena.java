package com.snet.buffer.block.level;

import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBufferBlock;

import java.util.Iterator;
import java.util.TreeSet;

class AreaBlockArena extends AbsLevelBlockArena {
	public static final int MIN_SHIFT = 8;
	public static final int MAX_SHIFT = 18;
	public static final int MIN_CAPACITY = 1 << MIN_SHIFT;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;
	protected final BlockCaches[] caches;
	protected final TreeSet<ProxyAllocatableBufferBlock> blocks;

	public AreaBlockArena(SNetBlockArena parent) {
		super(parent);
		this.caches = new BlockCaches[MAX_SHIFT - MIN_SHIFT];
		this.blocks = new TreeSet<>();
		for (int i = 0; i < MAX_SHIFT - MIN_SHIFT; ++i)
			caches[i] = new ArenaBlockCached(1 << (i + MIN_SHIFT), 64, 500 * (10 - i));
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < MAX_CAPACITY + 1;
	}

	@Override
	protected SNetBufferBlock allocate0(int capacity) {
		int idx = 32 - Integer.numberOfLeadingZeros(capacity - 1);
		idx = idx > MIN_SHIFT ? (idx - MIN_SHIFT) : 0;
		SNetBufferBlock block = caches[idx].poll();
		if (block != null)
			return block;
		return allocateImpl(capacity);
	}


	protected SNetBufferBlock allocateImpl(int capacity) {
		try {
			lock.lock();
			ProxyAllocatableBufferBlock block = getBlock(capacity);
			SNetBufferBlock result = block.allocate(capacity);
			blocks.add(block);
			return result;
		} finally {
			lock.unlock();
		}
	}

	protected ProxyAllocatableBufferBlock getBlock(int capacity) {
		for (Iterator<ProxyAllocatableBufferBlock> it = blocks.iterator(); it.hasNext(); ) {
			ProxyAllocatableBufferBlock block = it.next();
			if (capacity <= block.getRemaining()) {
				it.remove();
				return block;
			}
		}
		SNetBufferBlock block = parent.allocate(MAX_CAPACITY << 2);
		return new ProxyAllocatableBufferBlock(this, block);
	}

	@Override
	public void recycle(SNetBufferBlock block) {
		int capacity = block.getCapacity();
		int idx = 32 - Integer.numberOfLeadingZeros(capacity - 1);
		caches[idx].add(block);
	}

	protected void recycle0(SNetBufferBlock block) {
		ProxyAllocatableBufferBlock parent = (ProxyAllocatableBufferBlock) block.getParent();
		try {
			lock.lock();
			parent.recycle(block);
			if (parent.enableReleased()) {
				blocks.remove(parent);
				parent.reset();
				blocks.add(parent);
			}
		} finally {
			lock.unlock();
		}
	}

	protected final class ArenaBlockCached extends BlockCaches {
		public ArenaBlockCached(int blockCapacity, int capacity, long idleTime) {
			super(blockCapacity, capacity, idleTime);
		}

		@Override
		protected void recycleCachedBlock(SNetBufferBlock block) {
			recycle0(block);
		}
	}


}
