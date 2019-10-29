package com.snet.buffer.impl;

import com.snet.buffer.SNetAllocatableResourceBlock;
import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.buffer.util.SNetBlockSet;
import com.snet.util.Bitmap;
import com.snet.util.MathUtil;

class FixedResourceBlock extends DefResourceBlock implements SNetAllocatableResourceBlock {
	protected final SNetResourceBlockAllocator allocator;
	protected final SNetResourceBlock rawBlock;
	protected final int cellCapacity;
	protected final int cellSize;
	protected final int cellCapacityShift;
	protected final Bitmap freeBitmap;
	protected int minIdx;
	protected int remainCapacity;
	protected SNetBlockSet<FixedResourceBlock> blockSet;


	public FixedResourceBlock(SNetResourceBlockAllocator allocator, SNetResourceBlock rawBlock, int cellCapacity) {
		super(rawBlock.getParent(), rawBlock.getResource(), rawBlock.getResourceOff(),
				MathUtil.floor2(rawBlock.getCapacity()));
		this.rawBlock = rawBlock;
		this.allocator = allocator;
		this.cellCapacityShift = MathUtil.ceilLog2(cellCapacity);
		this.cellCapacity = 1 << cellCapacityShift;
		this.cellSize = this.capacity >>> cellCapacityShift;
		this.remainCapacity = this.capacity;
		this.freeBitmap = new Bitmap(cellSize);
		this.minIdx = 0;
	}

	public SNetResourceBlock getRawBlock() {
		return rawBlock;
	}

	public int getCellCapacity() {
		return cellCapacity;
	}

	public int getCellSize() {
		return cellSize;
	}

	public int getRemainCapacity() {
		return remainCapacity;
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		if (remainCapacity > 0) {
			final Bitmap freeBitmap = this.freeBitmap;
			final int len = cellSize, mark = len - 1;
			int i = 0, idx = minIdx & mark;
			for (; i < len; ++i, idx = (idx + 1) & mark) {
				if (!freeBitmap.getSet(idx, true)) {
					long cellOffset = getChildResourceOff(idx);
					remainCapacity -= cellCapacity;
					setMinIdx((idx + 1) & mark);
					SNetResource resource = this.resource.slice();
					return new DefResourceBlock(this, resource, cellOffset, cellCapacity);
				}
			}
		}
		return null;
	}

	@Override
	public void recycle(SNetResourceBlock block) {
		if (block.isDestroyed() && block.getParent() == this) {
			final int idx = getIdx(block.getResourceOff());
			freeBitmap.set(idx, false);
			setMinIdx(idx);
			remainCapacity += cellCapacity;
			block.destroy();
		}
	}

	protected long getChildResourceOff(int idx) {
		return resourceOff + idx << cellCapacityShift;
	}

	protected int getIdx(long offset) {
		return (int) ((offset - resourceOff) >>> cellCapacityShift);
	}

	protected void setMinIdx(int idx) {
		int startIdx = this.minIdx;
		this.minIdx = idx < startIdx ? idx : startIdx;
	}

}
