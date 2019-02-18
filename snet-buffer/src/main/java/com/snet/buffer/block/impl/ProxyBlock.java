package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.resource.SNetResource;

public class ProxyBlock implements SNetBlock {
	protected final SNetBlockArena arena;
	protected final SNetBlock block;
	protected boolean released;

	public ProxyBlock(SNetBlockArena arena, SNetBlock block) {
		this.arena = arena;
		this.block = block;
	}

	@Override
	public int getCapacity() {
		return block.getCapacity();
	}

	@Override
	public int getResourceOffset() {
		return block.getResourceOffset();
	}

	@Override
	public boolean isReleased() {
		return released;
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

	@Override
	public void release() {
		released = true;
	}

	public SNetBlock getBlock() {
		return block;
	}
}
