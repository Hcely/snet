package com.snet.buffer.block;

public interface SNetBlockArena extends SNetBlockAllocator {
	SNetBlockArena getParent();

	void recycle(SNetBlock block);

	void trimArena();
}
