package com.snet.buffer.block.impl;

import com.snet.Releasable;
import com.snet.buffer.block.DefBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.util.BPTreeMap;

public class AreaBlock extends ProxyBlock implements Releasable {
	protected static final BPTreeMap.KeyComparator<Cell, Object> REMAIN_COMPARATOR = (o1, o2) -> {
		final int remain1 = o1.remaining;
		if (o2 instanceof Integer)
			return remain1 < (Integer) o2 ? -1 : 1;
		final int remain2 = ((Cell) o2).remaining;
		return remain1 < remain2 ? -1 : (remain1 == remain2 ? 0 : 1);
	};

	protected final BPTreeMap<Integer, Cell> cells;
	protected final BPTreeMap<Cell, Void> sortCells;
	protected long lastUsingTime;
	protected int remaining;
	protected boolean released;

	public AreaBlock(AreaBlockArena arena, SNetBlock block) {
		super(arena, block);
		this.cells = new BPTreeMap<>(2);
		this.sortCells = new BPTreeMap<>(2, REMAIN_COMPARATOR, BPTreeMap.IDENTITY_EQUALS);
		this.lastUsingTime = System.currentTimeMillis();
		this.remaining = block.getCapacity();
		this.released = false;

		Cell cell = new Cell(this, block.getResourceOffset(), block.getCapacity());
		cells.put(cell.offset, cell);
		cell.sortNode = sortCells.getEntity(cell, true);
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
		for (Cell cell : cells.values())
			cell.removeSort();
	}

	public boolean enableReleased() {
		return remaining == block.getCapacity();
	}

	public long getLastUsingTime() {
		return lastUsingTime;
	}

	public SNetBlock allocate(int capacity) {
		BPTreeMap.LeafNode<Cell, Void> node = sortCells.ceilEntity(capacity);
		if (node == null)
			return null;
		Cell cell = node.getKey();
		cell.removeSort();
		this.remaining -= capacity;
		final int blockOffset = cell.allocateOff(capacity);
		if (cell.hasRemaining())
			cell.sortNode = sortCells.getEntity(cell, true);
		else {
			cells.remove(cell.offset);
			cell.sortNode = null;
		}
		return new DefBlock(blockOffset, capacity, arena, this);
	}

	public void recycle(SNetBlock block) {
		final int offset = block.getResourceOffset();
		final int capacity = block.getCapacity();
		Cell newCell = new Cell(this, offset, capacity), prevCell;
		BPTreeMap.LeafNode<Integer, Cell> node = cells.floorEntity(offset);
		if (node != null && (prevCell = node.getValue()).isNeighbor(newCell)) {
			prevCell.removeSort();
			prevCell.combine(newCell);
			prevCell.tryCombineNextNode(node.getNext());
			prevCell.sortNode = sortCells.getEntity(prevCell, true);
		} else {
			if (node != null)
				newCell.tryCombineNextNode(node.getNext());
			cells.put(offset, newCell);
			newCell.sortNode = sortCells.getEntity(newCell, true);
		}
		this.remaining += capacity;
	}

	public static final class Cell {
		protected final AreaBlock areaBlock;
		protected final int offset;
		protected int remaining;
		protected BPTreeMap.LeafNode<Cell, Void> sortNode;

		public Cell(AreaBlock areaBlock, int offset, int remaining) {
			this.areaBlock = areaBlock;
			this.offset = offset;
			this.remaining = remaining;
		}

		public void tryCombineNextNode(BPTreeMap.LeafNode<Integer, Cell> next) {
			if (next != null) {
				Cell nextBlock = next.getValue();
				if (isNeighbor(nextBlock)) {
					next.remove();
					nextBlock.removeSort();
					combine(nextBlock);
				}
			}
		}

		public int allocateOff(int capacity) {
			remaining -= capacity;
			return offset + remaining;
		}

		public void combine(Cell block) {
			remaining += block.remaining;
		}

		public boolean isNeighbor(Cell block) {
			return this.offset + remaining == block.offset;
		}

		public boolean hasRemaining() {
			return remaining > 0;
		}

		private void removeSort() {
			if (sortNode != null)
				sortNode.remove();
		}
	}
}
