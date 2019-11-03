package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;

public class DefResourceBlock implements SNetResourceBlock {
	protected final SNetResourceBlock parent;
	protected final SNetResource resource;
	protected final long resourceOff;
	protected final int capacity;
	protected boolean destroyed;

	public DefResourceBlock(SNetResource resource, long resourceOff, int capacity) {
		this(null, resource, resourceOff, capacity);
	}

	public DefResourceBlock(SNetResourceBlock parent, SNetResource resource, long resourceOff, int capacity) {
		this.parent = parent;
		this.resource = resource;
		this.resourceOff = resourceOff;
		this.capacity = capacity;
	}

	@Override
	public void destroy() {
		if (!destroyed) {
			this.resource.release();
			destroyed = true;
		}
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public SNetResourceBlock getParent() {
		return parent;
	}

	@Override
	public int getCapacity() {
		return capacity;
	}

	@Override
	public SNetResource getResource() {
		return resource;
	}

	@Override
	public long getResourceOff() {
		return resourceOff;
	}


}
