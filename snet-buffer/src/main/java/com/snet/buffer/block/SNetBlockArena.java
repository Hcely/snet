package com.snet.buffer.block;

import com.snet.Releasable;
import com.snet.buffer.SNetAllocator;

public interface SNetBlockArena extends SNetBlockAllocator, Releasable {
	SNetBlockArena getParent();

	void recycle(SNetBlock block);

	boolean isReleased();

	void releaseBlock();
}
