package com.snet.buffer.resource;

import java.nio.ByteBuffer;

public class BufferResourceFactory implements SNetBufferResourceFactory {
	protected boolean heap;

	public BufferResourceFactory(boolean heap) {
		this.heap = heap;
	}

	@Override
	public SNetResource create(int capacity) {
		ByteBuffer buffer = heap ? ByteBuffer.allocate(capacity) : ByteBuffer.allocateDirect(capacity);
		return new BufferResource(buffer);
	}
}
