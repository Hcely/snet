package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;

public abstract class AbstractBlockArena implements SNetBlockArena {
	protected final BlockArenaManager manager;
	protected final SNetBlockArena parent;

	public AbstractBlockArena(BlockArenaManager manager, SNetBlockArena parent) {
		this.manager = manager;
		this.parent = parent;
	}

	@Override
	public SNetBlockArena getParent() {
		return parent;
	}

	@Override
	public SNetBlock allocate(int capacity) {
		if (supports(capacity))
			return allocate0(capacity);
		return parent.allocate(capacity);
	}

	protected abstract boolean supports(int capacity);

	protected abstract SNetBlock allocate0(int capacity);
}
