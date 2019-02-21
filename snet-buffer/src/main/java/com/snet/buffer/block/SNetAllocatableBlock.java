package com.snet.buffer.block;

public interface SNetAllocatableBlock extends SNetBlock, SNetBlockAllocator {
	void recycle(SNetBlock block);
}
