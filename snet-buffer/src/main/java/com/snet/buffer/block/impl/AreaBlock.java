package com.snet.buffer.block.impl;

import com.snet.Releasable;
import com.snet.buffer.block.DefBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.util.BPTreeMap;

public class AreaBlock extends ProxyBlock implements Releasable {
	protected final BPTreeMap<Integer, Cell> cells;
	protected final AreaBlockArena arena;
	protected long lastUsingTime;
	protected int remaining;
	protected boolean released;

	public AreaBlock(AreaBlockArena arena, SNetBlock block) {
		super(arena, block);
		this.arena = arena;
		this.cells = new BPTreeMap<>(2);
		this.lastUsingTime = System.currentTimeMillis();
		this.remaining = block.getCapacity();
		this.released = false;
		Cell cell = new Cell(this, block.getResourceOffset(), block.getCapacity());
		cells.put(cell.offset, cell);
		cell.sortNode = arena.addSort(cell);
	}

	public Cell firstCell() {
		return cells.firstEntity().getValue();
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

	private void allocate(int capacity) {
		this.remaining -= capacity;
		this.lastUsingTime = System.currentTimeMillis();
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
			prevCell.sortNode = arena.addSort(prevCell);
		} else {
			if (node != null)
				newCell.tryCombineNextNode(node.getNext());
			cells.put(offset, newCell);
			newCell.sortNode = arena.addSort(newCell);
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

		public SNetBlock allocate(int capacity) {
			this.remaining -= capacity;
			areaBlock.allocate(capacity);
			removeSort();
			int blockOffset = offset + remaining;
			if (remaining > 0)
				this.sortNode = areaBlock.arena.addSort(this);
			else {
				areaBlock.cells.remove(offset);
				this.sortNode = null;
			}
			return new DefBlock(blockOffset, capacity, areaBlock.arena, areaBlock);
		}

		public void combine(Cell block) {
			remaining += block.remaining;
		}

		public boolean isNeighbor(Cell block) {
			return this.offset + remaining == block.offset;
		}

		private void removeSort() {
			if (sortNode != null)
				sortNode.remove();
		}
	}
}
