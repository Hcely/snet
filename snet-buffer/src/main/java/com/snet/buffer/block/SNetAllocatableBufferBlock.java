package com.snet.buffer.block;

import com.snet.buffer.SNetAllocator;

public interface SNetAllocatableBufferBlock extends SNetBlock, SNetAllocator<SNetBlock> {

	int getRemaining();

	boolean enableReleased();

	void reset();

	void recycle(SNetBlock block);
}
