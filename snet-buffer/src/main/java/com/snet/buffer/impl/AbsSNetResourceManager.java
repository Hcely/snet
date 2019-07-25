package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceManager;

import java.util.Map;

public abstract class AbsSNetResourceManager implements SNetResourceManager {
	protected Map<SNetResource, Void> resources;
	protected long sumCapacity = 0;

	@Override
	public long getSumCapacity() {
		return sumCapacity;
	}


}
