package com.snet.buffer.block.level;

import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBlock;

public class LocalBlockArena extends AbsLevelBlockArena {
	protected final Thread thread;
	protected final LocalBlockCached[] caches;

	public LocalBlockArena(SNetBlockArena parent, Thread thread) {
		super(parent);
		this.thread = thread;
		this.caches = new LocalBlockCached[5];
	}


	@Override
	public SNetBlock allocate(int capacity) {

		return null;
	}

	@Override
	protected boolean supports(int capacity) {
		return false;
	}

	@Override
	protected SNetBlock allocate0(int capacity) {
		return null;
	}

	@Override
	public void recycle(SNetBlock block) {
	}

	public void recycle(Thread thread, SNetBlock block) {

	}

	@Override
	public SNetBlockArena getParent() {
		return parent;
	}


	protected final class LocalBlockCached extends BlockCaches {
		public LocalBlockCached(int blockCapacity, int capacity, long idleTime) {
			super(blockCapacity, capacity, idleTime);
		}

		@Override
		protected void recycleCachedBlock(SNetBlock block) {
			parent.recycle(block);
		}
	}


}
