package com.snet.buffer.block;

import com.snet.buffer.resource.SNetResource;

public class DefBlock implements SNetBlock {
	protected final int resourceOffset;
	protected final int capacity;
	protected final SNetResource resource;
	protected final SNetBlockArena arena;
	protected final SNetBlock parent;
	protected boolean released;

	public DefBlock(int resourceOffset, int capacity, SNetBlockArena arena, SNetBlock parent) {
		this(resourceOffset, capacity, parent.getResource().duplicate(), arena, parent);
	}

	public DefBlock(int resourceOffset, int capacity, SNetResource resource, SNetBlockArena arena, SNetBlock parent) {
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
	public SNetResource getResource() {
		return resource;
	}

	@Override
	public SNetBlockArena getArena() {
		return arena;
	}

	@Override
	public SNetBlock getParent() {
		return parent;
	}

	@Override
	public boolean isReleased() {
		return released;
	}
}
