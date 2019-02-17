package com.snet.buffer.block.impl;

import com.snet.buffer.block.DefBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.resource.SNetResource;
import com.snet.util.BPTreeMap;

public class AreaBlock implements SNetBlock {
	protected final AreaBlockArena arena;
	protected final SNetBlock block;
	protected final BPTreeMap<Integer, Cell> cells;
	protected int remaining;

	public AreaBlock(AreaBlockArena arena, SNetBlock block) {
		this.arena = arena;
		this.block = block;
		this.cells = new BPTreeMap<>(2);
		this.remaining = block.getCapacity();

		Cell cell = new Cell(this, block.getResourceOffset(), block.getCapacity());
		cells.put(cell.offset, cell);
		cell.sortNode = arena.addSort(cell);
	}

	public Cell firstCell() {
		return cells.firstEntity().getValue();
	}

	public boolean enableReleased() {
		return remaining == block.getCapacity();
	}

	public SNetBlock allocate(Cell cell, int capacity) {
		remaining -= capacity;
		cell.remaining -= capacity;

		int blockOffset = cell.offset + remaining;
		cell.removeSort();
		if (cell.hasRemaining()) {
			cell.sortNode = arena.addSort(cell);
		} else {
			cells.remove(cell.offset);
			cell.sortNode = null;
		}
		return new DefBlock(blockOffset, capacity, getResource().duplicate(), arena, this);
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
		remaining += capacity;
	}

	@Override
	public int getCapacity() {
		return block.getCapacity();
	}

	@Override
	public int getResourceOffset() {
		return block.getResourceOffset();
	}

	@Override
	public boolean isReleased() {
		return block.isReleased();
	}

	@Override
	public SNetResource getResource() {
		return block.getResource();
	}

	@Override
	public SNetBlockArena getArena() {
		return arena;
	}

	@Override
	public SNetBlock getParent() {
		return block.getParent();
	}

	public static final class Cell {
		protected final AreaBlock combineBlock;
		protected final int offset;
		protected int remaining;
		protected BPTreeMap.LeafNode<Cell, Void> sortNode;

		public Cell(AreaBlock combineBlock, int offset, int remaining) {
			this.combineBlock = combineBlock;
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

		public void combine(Cell block) {
			remaining += block.remaining;
		}

		public boolean hasRemaining() {
			return remaining > 0;
		}

		public boolean isNeighbor(Cell block) {
			return this.offset + remaining == block.offset;
		}

		public void removeSort() {
			if (sortNode != null)
				sortNode.remove();
		}
	}
}
