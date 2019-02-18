package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;

public abstract class SNetAbsBlockArena implements SNetBlockArena {
	protected final ArenaManager manager;
	protected final SNetBlockArena parent;
	protected boolean released;

	public SNetAbsBlockArena(ArenaManager manager, SNetBlockArena parent) {
		this.manager = manager;
		this.parent = parent;
		this.released = false;
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

	@Override
	public boolean isReleased() {
		return released;
	}

	protected abstract boolean supports(int capacity);

	protected abstract SNetBlock allocate0(int capacity);
}
