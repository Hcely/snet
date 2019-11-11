package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.Bitmap;
import com.snet.util.MathUtil;

public class BitmapBlock extends BlockListNode<BitmapBlock> {
	protected final SNetResourceBlockAllocator allocator;
	protected final int cellSize;
	protected final int cellCapacityShift;
	protected final Bitmap freeMap;

	public BitmapBlock(SNetResourceBlockAllocator allocator, SNetResource resource, int cellCapacity) {
		this(allocator, resource, (int) MathUtil.floor2(resource.getCapacity()), cellCapacity);
	}

	private BitmapBlock(SNetResourceBlockAllocator allocator, SNetResource resource, int capacity,
			int cellCapacity) {
		super(resource, 0, capacity, cellCapacity);
		this.allocator = allocator;
		this.cellCapacityShift = MathUtil.ceilLog2(cellCapacity);
		this.cellSize = capacity >>> cellCapacityShift;
		this.freeMap = new Bitmap(cellSize);
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		int cellCount = capacity >>> cellCapacityShift;
		if ((cellCount << cellCapacityShift) < capacity) {
			++cellCount;
		}
		final int newCapacity = cellCount << cellCapacityShift;
		if (remainCapacity < newCapacity) {
			return null;
		}
		Bitmap freeMap = this.freeMap;
		for (int idx = 0, size = cellSize; idx < size; ++idx) {
			if (freeMap.equals(idx, cellCount, false)) {
				freeMap.set(idx, cellCount, true);
				this.remainCapacity -= newCapacity;
				final long childResourceOff = resourceOff + (idx << cellCapacityShift);
				return new DefResourceBlock(this, resource.slice(), childResourceOff, newCapacity);
			}
		}
		return null;
	}

	@Override
	public void recycle(SNetResourceBlock block) {
		if (!block.isDestroyed() && block.getParent() == this) {
			final int cellCount = block.getCapacity() >>> cellCapacityShift;
			final int idx = (int) ((block.getResourceOff() - resourceOff) >>> cellCapacityShift);
			remainCapacity += block.getCapacity();
			freeMap.set(idx, cellCount, false);
			block.destroy();
		}
	}


}
