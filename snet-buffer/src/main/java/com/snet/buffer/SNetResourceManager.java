package com.snet.buffer;

import com.snet.ResourceManager;

public interface SNetResourceManager extends SNetLongAllocator<SNetResource>, ResourceManager {
	long getSumCapacity();

	void recycle(SNetResource resource);
}
