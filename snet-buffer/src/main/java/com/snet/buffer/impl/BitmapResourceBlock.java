package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.Bitmap;
import com.snet.util.MathUtil;

public class BitmapResourceBlock extends BlockListNode<BitmapResourceBlock> {
	public static final long EQUAL_MASK = 0xFFL << 56;
	protected final int cellSize;
	protected final int cellCapacityShift;
	protected final Bitmap freeMap;

	public BitmapResourceBlock(SNetResourceBlockAllocator allocator, SNetResource resource, int cellCapacity) {
		this(allocator, resource, (int) MathUtil.floor2(resource.getCapacity()), cellCapacity);
	}

	private BitmapResourceBlock(SNetResourceBlockAllocator allocator, SNetResource resource, int capacity,
			int cellCapacity) {
		super(allocator, resource, 0, capacity, cellCapacity);
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
		for (int idx = 0, size = cellSize - cellCount + 1, half = cellCount >>> 1; idx < size; ++idx) {
			if (freeMap.equals(idx, cellCount, false)) {
				freeMap.set(idx, cellCount, true);
				this.remainCapacity -= newCapacity;
				final long childResourceOff = resourceOff + (idx << cellCapacityShift);
				return new DefResourceBlock(allocator, this, resource.slice(), childResourceOff, newCapacity);
			} else if (half > 2) {
				if (!freeMap.equals(idx + half, half, false)) {
					idx += half;
				}
				while (++idx < size) {
					if (freeMap.equals(idx, true)) {
						break;
					}
				}
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
