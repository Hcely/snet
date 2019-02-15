package com.snet.buffer.block.impl;

import com.snet.buffer.block.AbsBlockArena;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBlock;

public abstract class SNetAbsBlockArena extends AbsBlockArena implements SNetBlockArena {
	protected final SNetBlockArena parent;

	public SNetAbsBlockArena(SNetBlockArena parent) {
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
