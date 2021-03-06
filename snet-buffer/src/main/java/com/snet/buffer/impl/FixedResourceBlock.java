package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.Bitmap;
import com.snet.util.MathUtil;

class FixedResourceBlock extends BlockListNode<FixedResourceBlock> {
	public static final long EQUAL_MASK = 0xFFL << 56;
	protected final SNetResourceBlockAllocator allocator;
	protected final int cellSize;
	protected final int cellCapacityShift;
	protected final Bitmap freeMap;
	protected int minIdx;

	public FixedResourceBlock(SNetResourceBlockAllocator allocator, SNetResourceBlock rawBlock, int cellCapacity) {
		super(allocator, rawBlock, cellCapacity);
		this.allocator = allocator;
		this.cellCapacityShift = MathUtil.ceilLog2(cellCapacity);
		this.cellSize = this.capacity >>> cellCapacityShift;
		this.freeMap = new Bitmap(cellSize);
		this.minIdx = 0;
	}

	public int getCellSize() {
		return cellSize;
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		if (remainCapacity < 1) {
			return null;
		}
		final Bitmap freeMap = this.freeMap;
		final int len = cellSize;
		int idx = minIdx;
		if (!freeMap.getSet(idx, true)) {
			return allocate0(idx);
		}
		long equalMask = EQUAL_MASK;
		int mapIdx = 0;
		for (idx = 0; idx < len; ) {
			if (freeMap.equalsMask(mapIdx, equalMask, true)) {
				idx += 8;
				equalMask >>>= 8;
				if (equalMask == 0) {
					equalMask = EQUAL_MASK;
					++mapIdx;
				}
			} else {
				while (idx < len) {
					if (!freeMap.getSet(idx, true)) {
						return allocate0(idx);
					}
					++idx;
				}
			}
		}
		return null;
	}

	private SNetResourceBlock allocate0(int idx) {
		long cellOffset = getChildResourceOff(idx);
		remainCapacity -= cellCapacity;
		setMinIdx(idx + 1);
		SNetResource resource = this.resource.slice();
		return new DefResourceBlock(allocator, this, resource.slice(), cellOffset, cellCapacity);
	}

	@Override
	public void recycle(SNetResourceBlock block) {
		if (!block.isDestroyed() && block.getParent() == this) {
			final int idx = getIdx(block.getResourceOff());
			freeMap.set(idx, false);
			setMinIdx(idx);
			remainCapacity += cellCapacity;
			block.destroy();
		}
	}

	protected long getChildResourceOff(int idx) {
		return resourceOff + (idx << cellCapacityShift);
	}

	protected int getIdx(long offset) {
		return (int) ((offset - resourceOff) >>> cellCapacityShift);
	}

	protected void setMinIdx(int idx) {
		idx &= (cellSize - 1);
		int startIdx = this.minIdx;
		this.minIdx = idx < startIdx ? idx : startIdx;
	}

}
