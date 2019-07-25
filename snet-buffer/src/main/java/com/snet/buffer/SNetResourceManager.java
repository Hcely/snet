package com.snet.buffer;

public interface SNetResourceManager extends SNetAllocator<SNetResource> {
	long getSumCapacity();

	void recycle(SNetResource resource);
}
