package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;
import com.snet.buffer.SNetResourceBlockAllocator;
import com.snet.util.MathUtil;
import com.snet.util.coll.BPTreeMap;

public class BigResourceBlock extends DefResourceBlock implements SNetResourceBlockAllocator {
	protected final SNetResourceBlockAllocator allocator;
	protected final long capacityL;
	protected final int cellCapacityShift;
	protected final int cellCapacity;
	protected final BPTreeMap<OffsetBlock, Void> blocks;
	protected final BPTreeMap<OffsetBlock, Void> sortBlocks;
	protected long remainCapacity;

	public BigResourceBlock(SNetResourceBlockAllocator allocator, SNetResource resource, int cellCapacity) {
		this(allocator, resource, MathUtil.floor2(resource.getCapacity()), cellCapacity);
	}

	private BigResourceBlock(SNetResourceBlockAllocator allocator, SNetResource resource, long capacityL,
			int cellCapacity) {
		super(null, resource, 0, (int) capacityL);
		this.allocator = allocator;
		this.capacityL = capacityL;
		this.cellCapacityShift = MathUtil.ceilLog2(cellCapacity);
		this.cellCapacity = 1 << cellCapacityShift;
		this.blocks = new BPTreeMap<>(OFFSET_COMPARATOR);
		this.sortBlocks = new BPTreeMap<>(SORT_COMPARATOR);
		this.remainCapacity = capacityL;
		OffsetBlock block = new OffsetBlock(0, capacityL);
		blocks.put(block, null);
		sortBlocks.put(block, null);
	}

	public long getCapacityL() {
		return capacityL;
	}

	public long getRemainCapacity() {
		return remainCapacity;
	}

	public int getCellCapacity() {
		return cellCapacity;
	}

	@Override
	public SNetResourceBlock allocate(int capacity) {
		capacity = fixCapacity(capacity);
		if (remainCapacity < capacity) {
			return null;
		}
		BPTreeMap.LeafNode<OffsetBlock, Void> node = sortBlocks.ceilEntity(capacity);
		if (node == null)
			return null;
		node.remove();
		OffsetBlock block = node.getKey();
		final long childResourceOff = block.allocate(capacity);
		remainCapacity -= capacity;
		if (block.capacity > 0) {
			sortBlocks.putEntity(node);
		} else {
			blocks.remove(block);
		}
		return new DefResourceBlock(this, resource.slice(), childResourceOff, capacity);
	}

	@Override
	public void recycle(SNetResourceBlock block) {
		if (block.isDestroyed() && block.getParent() == this) {
			combineBlock(block);
			remainCapacity += block.getCapacity();
			block.destroy();
		}
	}

	protected void combineBlock(SNetResourceBlock block) {
		BPTreeMap.LeafNode<OffsetBlock, Void> node = blocks.floorEntity(block);
		OffsetBlock offsetBlock;
		if (node != null && (offsetBlock = node.getKey()).isAfter(block.getResourceOff())) {
			sortBlocks.remove(offsetBlock);
			offsetBlock.capacity += block.getCapacity();
		} else {
			offsetBlock = new OffsetBlock(block.getResourceOff(), block.getCapacity());
			node = blocks.getEntity(offsetBlock, true);
		}
		if (node.getNext() != null) {
			OffsetBlock nextB = node.getNext().getKey();
			if (offsetBlock.isAfter(nextB.offset)) {
				node.getNext().remove();
				sortBlocks.remove(nextB);
				offsetBlock.capacity += nextB.capacity;
			}
		}
		sortBlocks.put(offsetBlock, null);
	}


	protected int fixCapacity(int capacity) {
		int result = (capacity >>> cellCapacityShift) << cellCapacityShift;
		if (result < capacity)
			result += cellCapacity;
		return result;
	}

	private static final BPTreeMap.KeyComparator<OffsetBlock, Object> SORT_COMPARATOR = (key, keyObj) -> {
		if (keyObj instanceof Number) {
			return Long.compare(key.capacity, ((Number) keyObj).longValue());
		} else {
			OffsetBlock block = (OffsetBlock) keyObj;
			if (key.capacity < block.capacity)
				return -1;
			else if (key.capacity > block.capacity)
				return 1;
			else
				return Long.compare(key.offset, block.offset);
		}
	};

	private static final BPTreeMap.KeyComparator<OffsetBlock, Object> OFFSET_COMPARATOR = (key, keyObj) -> {
		if (keyObj instanceof SNetResourceBlock) {
			return Long.compare(key.offset, ((SNetResourceBlock) keyObj).getResourceOff());
		} else {
			return Long.compare(key.offset, ((OffsetBlock) keyObj).offset);
		}
	};

	protected static class OffsetBlock {
		protected final long offset;
		protected long capacity;

		public OffsetBlock(long offset, long capacity) {
			this.offset = offset;
			this.capacity = capacity;
		}

		public long allocate(int capacity) {
			this.capacity -= capacity;
			return offset + this.capacity;
		}

		public boolean isAfter(long offset) {
			return this.offset + this.capacity == offset;
		}
	}
}
