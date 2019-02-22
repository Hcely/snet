package com.snet.buffer.block.impl;

import com.snet.Releasable;
import com.snet.buffer.block.DefBlock;
import com.snet.buffer.block.SNetAllocatableBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.util.BPTreeMap;
import com.snet.util.MathUtil;
import javafx.scene.control.Cell;

public class ProvinceBlock extends ProxyBlock implements SNetAllocatableBlock, Releasable {
	protected final byte[] tree;
	protected final byte[] depths;
	protected final int depth;
	protected final int unitLen;
	protected final int unitLenShift;
	protected final int lenShift;
	protected long lastUsingTime;
	protected int remaining;
	protected boolean released;

	public ProvinceBlock(AreaArena arena, SNetBlock block, int unitLen) {
		super(arena, block, MathUtil.floor2(block.getCapacity() / (unitLen = MathUtil.ceil2(unitLen))));
		this.lastUsingTime = System.currentTimeMillis();
		this.remaining = capacity;
		this.released = false;
		this.depth = MathUtil.floorLog2(capacity / unitLen) + 1;
		this.unitLenShift = MathUtil.ceilLog2(unitLen);
		this.lenShift = unitLenShift + depth - 1;
		this.unitLen = unitLen;
		this.tree = new byte[1 << depth];
		this.depths = new byte[tree.length];
		for (int d = 0, idx = 1; d < depth; ++d) {
			for (int i = 0, len = 1 << d; i < len; ++i, ++idx) {
				this.tree[idx] = (byte) d;
				this.depths[idx] = (byte) d;
			}
		}
	}

	public boolean isReleased() {
		return released;
	}

	@Override
	public void release() {
		if (released)
			return;
		if (!enableReleased())
			throw new RuntimeException("");
		released = true;
	}

	public boolean enableReleased() {
		return remaining == block.getCapacity();
	}

	public long getLastUsingTime() {
		return lastUsingTime;
	}

	public SNetBlock allocate(int capacity) {
		final int d = getCapacityDepth(capacity);
		final byte[] tree = this.tree;
		if (tree[1] > d)
			return null;
		int idx = 1;
		for (int i = 0; i < d; ++i) {
			idx <<= 1;
			if (tree[idx] > d)
				idx ^= 1;
		}
		updateAlloc(idx);
		this.lastUsingTime = System.currentTimeMillis();
		this.remaining -= capacity;
		final int offset = getOffset(idx);
		return new DefBlock(offset, capacity, arena, this);
	}

	public void recycle(SNetBlock block) {
		block.release();
		int idx = getIdx(block.getResourceOffset(), block.getCapacity());
		updateFree(idx);
		remaining += block.getCapacity();
	}

	protected int getCapacityDepth(int capacity) {
		return MathUtil.ceilLog2(capacity >>> unitLenShift);
	}

	protected void updateAlloc(int idx) {
		final byte[] tree = this.tree;
		tree[idx] = Byte.MAX_VALUE;
		int parentIdx;
		byte val;
		while (idx > 0) {
			parentIdx = idx >>> 1;
			val = min(tree[idx], tree[idx ^ 1]);
			if (tree[parentIdx] == val)
				break;
			tree[parentIdx] = val;
			idx = parentIdx;
		}
	}

	protected void updateFree(int idx) {
		final byte[] tree = this.tree;
		byte val = depths[idx], bVal;
		tree[idx] = val;
		int parentIdx;
		while (idx > 0) {
			parentIdx = idx >>> 1;
			val = val == (bVal = tree[idx ^ 1]) ? --val : min(val, bVal);
			if (tree[parentIdx] == val)
				break;
			tree[parentIdx] = val;
			idx = parentIdx;
		}
	}

	protected int getOffset(final int idx) {
		final int d = depths[idx];
		int i = 1 << d;
		i = (idx - i) << (lenShift - d);
		return resourceOffset + i;
	}

	protected int getIdx(int offset, int capacity) {
		final int d = getCapacityDepth(capacity);
		int i = (offset - resourceOffset) >>> (lenShift - d);
		return i + (1 << d);
	}

	protected static final byte min(byte a, byte b) {
		return a < b ? a : b;
	}
}
