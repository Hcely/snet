package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceFactory;
import com.snet.buffer.SNetResourceManager;
import com.snet.buffer.exception.SNetBufferException;

public class ByteResourceFactory implements SNetResourceFactory {
	@Override
	public ByteResource create(SNetResourceManager manager, long capacity) {
		if (capacity > Integer.MAX_VALUE)
			throw new SNetBufferException("out of max capacity:" + Integer.MAX_VALUE);
		return new ByteResource(manager, (int) capacity);
	}
}
