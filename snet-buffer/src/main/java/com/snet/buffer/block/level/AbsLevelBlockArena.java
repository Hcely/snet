package com.snet.buffer.block.level;

import com.snet.buffer.block.AbsBlockArena;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBufferBlock;

public abstract class AbsLevelBlockArena extends AbsBlockArena implements SNetLevelBlockArena {
	protected final SNetBlockArena parent;

	public AbsLevelBlockArena(SNetBlockArena parent) {
		this.parent = parent;
	}

	@Override
	public SNetBlockArena getParent() {
		return parent;
	}

	@Override
	public SNetBufferBlock allocate(int capacity) {
		if (supports(capacity))
			return allocate0(capacity);
		return parent.allocate(capacity);
	}

	protected abstract boolean supports(int capacity);

	protected abstract SNetBufferBlock allocate0(int capacity);
}
