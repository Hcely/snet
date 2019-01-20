package com.snet.buffer.block;

import com.snet.buffer.SNetAllocator;

public interface SNetAllocatableBufferBlock extends SNetBufferBlock, SNetAllocator<SNetBufferBlock> {

	int getRemaining();

	boolean enableReleased();

	void reset();

	void recycle(SNetBufferBlock block);
}
