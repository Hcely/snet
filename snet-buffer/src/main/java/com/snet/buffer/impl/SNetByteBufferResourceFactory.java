package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceFactory;
import com.snet.buffer.SNetResourceManager;

public class SNetByteBufferResourceFactory implements SNetResourceFactory {
	protected final boolean direct;

	public SNetByteBufferResourceFactory() {
		this(true);
	}

	public SNetByteBufferResourceFactory(boolean direct) {
		this.direct = direct;
	}

	@Override
	public SNetByteBufferResource create(SNetResourceManager manager, int capacity) {
		return new SNetByteBufferResource(manager, direct, capacity);
	}
}
