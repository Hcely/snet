package com.snet.buffer;

public interface SNetAllocator<T> {
	T allocate(int capacity);
}
