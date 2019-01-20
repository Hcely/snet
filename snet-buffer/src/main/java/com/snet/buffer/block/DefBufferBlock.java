package com.snet.buffer.block;

import com.snet.buffer.resource.SNetBufferResource;

public class DefBufferBlock implements SNetBufferBlock {
	protected final int resourceOffset;
	protected final int capacity;

	protected final SNetBufferResource resource;
	protected final SNetBlockArena arena;
	protected final SNetBufferBlock parent;
	protected boolean released;

	public DefBufferBlock(int resourceOffset, int capacity, SNetBlockArena arena, SNetBufferBlock parent) {
		this(resourceOffset, capacity, parent.getResource(), arena, parent);
	}

	public DefBufferBlock(int resourceOffset, int capacity, SNetBufferResource resource, SNetBlockArena arena, SNetBufferBlock parent) {
		this.resourceOffset = resourceOffset;
		this.capacity = capacity;
		this.resource = resource;
		this.arena = arena;
		this.parent = parent;
		this.released = false;
	}

	@Override
	public int getCapacity() {
		return capacity;
	}

	@Override
	public int getResourceOffset() {
		return resourceOffset;
	}

	@Override
	public SNetBufferResource getResource() {
		return resource;
	}

	@Override
	public SNetBlockArena getArena() {
		return arena;
	}

	@Override
	public SNetBufferBlock getParent() {
		return parent;
	}

	@Override
	public boolean isReleased() {
		return released;
	}
}
