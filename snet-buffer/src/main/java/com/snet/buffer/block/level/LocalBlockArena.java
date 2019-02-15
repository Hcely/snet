package com.snet.buffer.block.level;

import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBlock;

public class LocalBlockArena extends AbsLevelBlockArena {
	public static final int MIN_SHIFT = 8;
	public static final int MAX_SHIFT = 12;
	public static final int MIN_CAPACITY = 1 << MIN_SHIFT;
	public static final int MAX_CAPACITY = 1 << MAX_SHIFT;
	protected final Thread thread;
	protected final BlockCaches[] caches;

	public LocalBlockArena(SNetBlockArena parent, Thread thread) {
		super(parent);
		this.thread = thread;
		this.caches = new BlockCaches[MAX_SHIFT - MIN_SHIFT];
		for (int i = 0, len = MAX_SHIFT - MIN_SHIFT; i < len; ++i)
			this.caches[i] = new BlockCaches(64 >>> i, 5000);
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < MAX_CAPACITY + 1;
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

}
