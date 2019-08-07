package com.snet.buffer;

public interface SNetResourceFactory {
	SNetResource create(SNetResourceManager manager, long capacity);
}
