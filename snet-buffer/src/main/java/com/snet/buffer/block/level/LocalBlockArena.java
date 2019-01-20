package com.snet.buffer.block.level;

import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBufferBlock;

@SuppressWarnings("unchecked")
public class LocalBlockArena extends AbsLevelBlockArena {

	protected final Thread thread;

	protected final LocalBlockCached[] cacheds;

	public LocalBlockArena(SNetBlockArena parent, Thread thread) {
		super(parent);
		this.thread = thread;
		this.cacheds = new LocalBlockCached[5];
	}


	@Override
	public SNetBufferBlock allocate(int capacity) {

		return null;
	}

	@Override
	protected boolean supports(int capacity) {
		return false;
	}

	@Override
	protected SNetBufferBlock allocate0(int capacity) {
		return null;
	}

	@Override
	public void recycle(SNetBufferBlock block) {
	}

	public void recycle(Thread thread, SNetBufferBlock block) {

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
		protected void recycleCachedBlock(SNetBufferBlock block) {

		}
	}


}
