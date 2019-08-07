package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceFactory;
import com.snet.buffer.SNetResourceManager;
import com.snet.buffer.exception.SNetBufferException;

public class ByteBufferResourceFactory implements SNetResourceFactory {
	protected final boolean direct;

	public ByteBufferResourceFactory() {
		this(true);
	}

	public ByteBufferResourceFactory(boolean direct) {
		this.direct = direct;
	}

	@Override
	public ByteBufferResource create(SNetResourceManager manager, long capacity) {
		if (capacity > Integer.MAX_VALUE)
			throw new SNetBufferException("out of max capacity:" + Integer.MAX_VALUE);
		return new ByteBufferResource(manager, direct, (int) capacity);
	}
}
