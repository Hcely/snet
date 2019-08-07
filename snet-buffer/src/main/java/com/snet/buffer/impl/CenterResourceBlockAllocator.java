package com.snet.buffer.impl;

import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceManager;

public class CenterResourceBlockAllocator implements SNetResourceBlockAllocator {
	protected SNetResourceManager manager;


	@Override
	public void recycle(SNetResourceBlock block) {

	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		return null;
	}

}
