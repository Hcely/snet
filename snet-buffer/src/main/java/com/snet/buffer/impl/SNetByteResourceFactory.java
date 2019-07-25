package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceFactory;
import com.snet.buffer.SNetResourceManager;

public class SNetByteResourceFactory implements SNetResourceFactory {
	@Override
	public SNetByteResource create(SNetResourceManager manager, int capacity) {
		return new SNetByteResource(manager, capacity);
	}
}
