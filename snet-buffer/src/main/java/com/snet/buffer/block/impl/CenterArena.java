package com.snet.buffer.block.impl;

import com.snet.buffer.block.DefBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.resource.SNetResource;

import java.util.concurrent.ConcurrentHashMap;

class CenterArena extends AbstractBlockArena {
	private static final int MAX_SHIFT = 24;
	public static final int DEF_MAX_CAPACITY = 1 << MAX_SHIFT;//16mb
	protected final int maxCapacity;
	protected final int threshold;
	protected final ConcurrentHashMap<SNetBlock, Boolean> blocks;

	public CenterArena(BlockArenaManager manager) {
		this(manager, null);
	}

	public CenterArena(BlockArenaManager manager, SNetBlockArena parent) {
		this(manager, parent, DEF_MAX_CAPACITY);
	}

	public CenterArena(BlockArenaManager manager, SNetBlockArena parent, int maxCapacity) {
		super(manager, parent);
		this.maxCapacity = maxCapacity;
		this.threshold = maxCapacity + 1;
		this.blocks = new ConcurrentHashMap<>();
	}

	@Override
	protected boolean supports(int capacity) {
		return capacity < threshold;
	}

	@Override
	public SNetBlock allocate0(int capacity) {
		SNetResource resource = manager.createResource(capacity);
		SNetBlock block = new DefBlock(resource, this);
		blocks.put(block, Boolean.TRUE);
		return block;
	}

	@Override
	public void recycle(SNetBlock block) {
		if (blocks.remove(block) != null)
			block.release();
	}

	@Override
	public void trimArena() {
	}
}
