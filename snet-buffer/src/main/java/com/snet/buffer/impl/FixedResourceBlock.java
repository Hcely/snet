package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.Bitmap;
import com.snet.util.MathUtil;

public class FixedResourceBlock extends DefResourceBlock implements SNetResourceBlockAllocator {
	protected final SNetResourceBlockAllocator allocator;
	protected final int cellCapacity;
	protected final int cellSize;
	protected final int cellCapacityShift;
	protected final Bitmap freeBitmap;
	protected int minIdx;
	protected int remainCellSize;

	public FixedResourceBlock(SNetResourceBlockAllocator allocator, SNetResourceBlock target, int cellCapacity) {
		this(allocator, target.getParent(), target.getResource(), target.getResourceOff(), target.getCapacity(),
				cellCapacity);
	}

	public FixedResourceBlock(SNetResourceBlockAllocator allocator, SNetResourceBlock parent, SNetResource resource,
			long resourceOff, int capacity, int cellCapacity) {
		super(parent, resource, resourceOff, MathUtil.floor2(capacity));
		this.allocator = allocator;
		this.cellCapacityShift = MathUtil.ceilLog2(cellCapacity);
		this.cellCapacity = 1 << cellCapacityShift;
		this.cellSize = this.capacity >>> cellCapacityShift;
		this.remainCellSize = cellSize;
		this.freeBitmap = new Bitmap(cellSize);
		this.minIdx = 0;
	}

	public int getCellCapacity() {
		return cellCapacity;
	}

	public int getCellSize() {
		return cellSize;
	}

	public int getRemainCellSize() {
		return remainCellSize;
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		if (remainCellSize > 0) {
			final Bitmap freeBitmap = this.freeBitmap;
			final int len = cellSize, mark = len - 1;
			int i = 0, idx = minIdx & mark;
			for (; i < len; ++i, idx = (idx + 1) & mark) {
				if (!freeBitmap.getSet(idx, true)) {
					long cellOffset = getChildResourceOff(idx);
					--remainCellSize;
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
			++remainCellSize;
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
