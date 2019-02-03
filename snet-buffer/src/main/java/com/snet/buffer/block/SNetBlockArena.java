package com.snet.buffer.block;

import com.snet.buffer.SNetAllocator;

public interface SNetBlockArena extends SNetAllocator<SNetBlock> {
	void recycle(SNetBlock block);
}
