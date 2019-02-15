package com.snet.buffer.block;

import com.snet.buffer.SNetAllocator;

public interface SNetBlockArena extends SNetAllocator<SNetBlock> {
	SNetBlockArena getParent();

	void recycle(SNetBlock block);

	void releaseBlock();
}
