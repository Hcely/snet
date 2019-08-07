package com.snet.buffer;

public interface SNetLongAllocator<T> {
	T allocate(long capacity);
}
