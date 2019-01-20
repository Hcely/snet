package com.snet.buffer.block.level;

import com.snet.Releasable;
import com.snet.buffer.block.DefBufferBlock;
import com.snet.buffer.block.SNetAllocatableBufferBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.block.SNetBufferBlock;
import com.snet.buffer.resource.SNetBufferResource;

public class ProxyAllocatableBufferBlock implements SNetAllocatableBufferBlock, Releasable, Comparable<ProxyAllocatableBufferBlock> {
	protected final SNetBlockArena arena;
	protected final SNetBufferBlock block;
	protected boolean released;
	protected int remaining;
	protected int pos;
	protected int recycled;

	public ProxyAllocatableBufferBlock(SNetBlockArena arena, SNetBufferBlock block) {
		this.arena = arena;
		this.block = block;
		this.released = false;
		this.remaining = block.getCapacity();
		this.pos = 0;
		this.recycled = 0;
	}

	@Override
	public SNetBufferBlock allocate(int capacity) {
		if (released)
			return null;
		if (capacity > remaining)
			return null;
		int offset = block.getResourceOffset() + pos;
		remaining -= capacity;
		pos += capacity;
		return new DefBufferBlock(offset, capacity, arena, this);
	}

	@Override
	public void recycle(SNetBufferBlock block) {
		if (block.getParent() == this)
			recycled += block.getCapacity();
	}

	@Override
	public void reset() {
		remaining = block.getCapacity();
		pos = 0;
		recycled = 0;
	}

	@Override
	public boolean enableReleased() {
		return pos == recycled && !released;
	}

	@Override
	public void release() {
		if (released)
			return;
		block.getArena().recycle(block);
		released = true;
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
		return block.isReleased();
	}

	@Override
	public SNetBufferResource getResource() {
		return block.getResource();
	}

	@Override
	public SNetBlockArena getArena() {
		return arena;
	}

	@Override
	public SNetBufferBlock getParent() {
		return block.getParent();
	}

	@Override
	public int getRemaining() {
		return remaining;
	}

	@Override
	public int compareTo(ProxyAllocatableBufferBlock o) {
		if (remaining < o.remaining)
			return -1;
		if (remaining > o.remaining)
			return 1;
		return hashCode() < o.hashCode() ? -1 : 1;
	}
}
