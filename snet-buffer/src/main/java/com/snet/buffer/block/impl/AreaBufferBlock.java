package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetAllocatableBufferBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.resource.SNetResource;

public class AreaBufferBlock implements SNetAllocatableBufferBlock {


	@Override
	public int getRemaining() {
		return 0;
	}

	@Override
	public boolean enableReleased() {
		return false;
	}

	@Override
	public void reset() {

	}

	@Override
	public void recycle(SNetBlock block) {

	}

	@Override
	public SNetBlock allocate(int capacity) {
		return null;
	}

	@Override
	public int getCapacity() {
		return 0;
	}

	@Override
	public int getResourceOffset() {
		return 0;
	}

	@Override
	public boolean isReleased() {
		return false;
	}

	@Override
	public SNetResource getResource() {
		return null;
	}

	@Override
	public SNetBlockArena getArena() {
		return null;
	}

	@Override
	public SNetBlock getParent() {
		return null;
	}
}
