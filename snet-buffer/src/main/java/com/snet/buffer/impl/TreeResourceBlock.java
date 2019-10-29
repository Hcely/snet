package com.snet.buffer.impl;

import com.snet.buffer.SNetAllocatableResourceBlock;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.MathUtil;

class TreeResourceBlock extends DefResourceBlock implements SNetAllocatableResourceBlock {
	protected final SNetResourceBlockAllocator allocator;
	protected final SNetResourceBlock rawBlock;
	protected final int cellCapacityShift;
	protected final int levelNum;
	protected final byte[] tree;
	protected int remainCapacity;

	public TreeResourceBlock(SNetResourceBlockAllocator allocator, SNetResourceBlock rawBlock, int cellCapacity) {
		super(rawBlock.getParent(), rawBlock.getResource(), rawBlock.getCapacity(),
				MathUtil.floor2(rawBlock.getCapacity()));
		this.allocator = allocator;
		this.rawBlock = rawBlock;
		this.cellCapacityShift = MathUtil.ceilLog2(cellCapacity);
		this.levelNum = MathUtil.floorLog2(capacity) + 1 - cellCapacityShift;
		this.remainCapacity = this.capacity;
		this.tree = new byte[1 << levelNum];
		for (byte i = 0; i < levelNum; ) {
			for (int idx = 1 << i, end = 1 << (++i); idx < end; ++idx) {
				tree[idx] = i;
			}
		}
	}

	public SNetResourceBlock getRawBlock() {
		return rawBlock;
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		int level = getLevel(capacity);
		int idx = allocateTree(level);
		if (idx == -1) {
			return null;
		}
		long offset = getChildResourceOff(idx, level);
		capacity = getChildCapacity(level);
		remainCapacity -= capacity;
		return new DefResourceBlock(this, resource.slice(), offset, capacity);
	}

	@Override
	public void recycle(SNetResourceBlock block) {
		if (block.isDestroyed() && block.getParent() == this) {
			final int level = getLevel(block.getCapacity());
			final int idx = getTreeIdx(block.getResourceOff(), level);
			recycleTree(idx, level);
			remainCapacity += block.getCapacity();
			block.destroy();
		}
	}

	@Override
	public int getRemainCapacity() {
		return remainCapacity;
	}

	protected int allocateTree(int level) {
		final byte[] tree = this.tree;
		int idx = 1;
		for (int i = 1; i < level; ++i) {
			idx <<= 1;
			if (tree[idx] > level && tree[idx |= 1] > idx) {
				return -1;
			}
		}
		if (tree[idx] > level) { return -1; }
		byte value = Byte.MAX_VALUE;
		int tmpIdx = idx;
		while (tmpIdx > 0 && tree[tmpIdx] < value) {
			tree[tmpIdx] = value;
			value = min(value, tree[tmpIdx ^ 1]);
			tmpIdx >>>= 1;
		}
		return idx;
	}

	protected void recycleTree(int idx, final int level) {
		byte value = (byte) level;
		final byte[] tree = this.tree;
		while (idx > 0 && tree[idx] > value) {
			tree[idx] = value;
			if (tree[idx ^ 1] == value) {
				--value;
			}
			idx >>>= 1;
		}
	}


	protected int getChildCapacity(int level) {
		return 1 << (levelNum + cellCapacityShift - level);
	}

	protected int getLevel(int childCapacity) {
		return levelNum + cellCapacityShift - MathUtil.ceilLog2(childCapacity);
	}

	protected long getChildResourceOff(int idx, int level) {
		int off = idx - (1 << level);
		off <<= levelNum + cellCapacityShift - level;
		return this.resourceOff + off;
	}

	protected int getTreeIdx(long childResourceOff, int level) {
		int idx = (int) (childResourceOff - this.resourceOff);
		idx >>>= levelNum + cellCapacityShift - level;
		return idx + (1 << level);
	}

	protected static byte min(byte a, byte b) {
		return a < b ? a : b;
	}


}
