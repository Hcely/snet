package com.snet.buffer.resource;

import com.snet.buffer.SNetReference;

import java.nio.ByteBuffer;

public interface SNetResource extends SNetReference, SNetResourceView {
	int CACHE_CAPACITY = 256;
	ThreadLocal<ByteBuffer> caches = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(CACHE_CAPACITY));

	static ByteBuffer getCacheBuffer() {
		ByteBuffer buf = caches.get();
		buf.clear();
		return buf;
	}

	int getCapacity();

	Object getRawObject();

	SNetResource duplicate();

	boolean isReleased();
}
