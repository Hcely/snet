package com.snet.buffer.resource;

public class BytesResourceFactory implements SNetBufferResourceFactory {
	@Override
	public SNetBufferResource create(int capacity) {
		return new BytesResource(new byte[capacity]);
	}
}
