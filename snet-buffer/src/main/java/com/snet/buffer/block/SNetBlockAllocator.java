package com.snet.buffer.block;

import com.snet.buffer.SNetAllocator;

public interface SNetBlockAllocator extends SNetAllocator<SNetBlock> {
	@Override
	SNetBlock allocate(int capacity);
}
