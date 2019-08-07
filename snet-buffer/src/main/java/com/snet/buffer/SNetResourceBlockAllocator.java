package com.snet.buffer;

public interface SNetResourceBlockAllocator extends SNetAllocator<SNetResourceBlock> {
	void recycle(SNetResourceBlock block);
}
