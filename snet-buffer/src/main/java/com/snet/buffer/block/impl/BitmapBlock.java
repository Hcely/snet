package com.snet.buffer.block.impl;

import com.snet.Releasable;
import com.snet.buffer.block.SNetAllocatableBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.util.Bitmap;
import com.snet.util.MathUtil;

public class BitmapBlock extends ProxyBlock implements SNetAllocatableBlock, Releasable {


	protected int cellLength;
	protected int cellLengthShift;
	protected int mapSize;
	protected int remainSize;
	protected Bitmap bitmap;

	public BitmapBlock(SNetBlockArena arena, SNetBlock block, int cellLength) {
		super(arena, block);
		this.cellLength = MathUtil.ceil2(cellLength);
		this.cellLengthShift = MathUtil.ceilLog2(this.cellLength);
		this.mapSize = getCapacity() >>> cellLengthShift;
		this.remainSize = mapSize;
		this.bitmap = new Bitmap(mapSize);
	}

	@Override
	public void recycle(SNetBlock block) {

	}

	@Override
	public SNetBlock allocate(int capacity) {
		return null;
	}
}
