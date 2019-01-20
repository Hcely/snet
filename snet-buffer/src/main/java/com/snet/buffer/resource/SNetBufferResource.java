package com.snet.buffer.resource;

import com.snet.buffer.SNetReference;

import java.nio.ByteBuffer;

public interface SNetBufferResource extends SNetReference, SNetResourceView {
    int CACHE_CAPACITY = 256;
    ThreadLocal<ByteBuffer> caches = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(CACHE_CAPACITY));

    static ByteBuffer getCacheBuffer() {
        ByteBuffer buf = caches.get();
        buf.clear();
        return buf;
    }

    int getCapacity();

    Object getRaw();

    SNetBufferResource duplicate();

    boolean isReleased();
}
