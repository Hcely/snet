package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.resource.SNetResource;

public class ProxyBlock implements SNetBlock {
	protected final SNetBlockArena arena;
	protected final int resourceOffset;
	protected final int capacity;
	protected final SNetBlock block;

	public ProxyBlock(SNetBlockArena arena, SNetBlock block) {
		this(arena, block, block.getResourceOffset(), block.getCapacity());
	}

	public ProxyBlock(SNetBlockArena arena, SNetBlock block, int capacity) {
		this(arena, block, block.getResourceOffset(), capacity);
	}

	public ProxyBlock(SNetBlockArena arena, SNetBlock block, int resourceOffset, int capacity) {
		this.arena = arena;
		this.block = block;
		this.resourceOffset = resourceOffset;
		this.capacity = capacity;
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
		return block.getResource();
	}

	@Override
	public SNetBlockArena getArena() {
		return arena;
	}

	@Override
	public SNetBlock getParent() {
		return block.getParent();
	}

	@Override
	public void recycle() {
		arena.recycle(this);
	}

	public SNetBlock getBlock() {
		return block;
	}

	@Override
	public void release() {
	}
}
