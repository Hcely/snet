package com.snet.buffer;

public interface SNetResourceManager extends SNetLongAllocator<SNetResource> {
	long getSumCapacity();

	void recycle(SNetResource resource);
}
