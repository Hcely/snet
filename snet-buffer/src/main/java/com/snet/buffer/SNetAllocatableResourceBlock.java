package com.snet.buffer;

public interface SNetAllocatableResourceBlock extends SNetResourceBlock, SNetResourceBlockAllocator {
	int getRemainCapacity();
}
