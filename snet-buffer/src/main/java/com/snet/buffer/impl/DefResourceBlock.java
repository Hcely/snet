package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;

public class DefResourceBlock implements SNetResourceBlock {
	protected final SNetResourceBlockAllocator allocator;
	protected final SNetResourceBlock parent;
	protected final SNetResource resource;
	protected final long resourceOff;
	protected final int capacity;
	protected boolean destroyed;

	public DefResourceBlock(SNetResourceBlockAllocator allocator, SNetResourceBlock parent, SNetResource resource,
			long resourceOff, int capacity) {
		this.allocator = allocator;
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

	@Override
	public SNetResourceBlockAllocator getAllocator() {
		return allocator;
	}
}
