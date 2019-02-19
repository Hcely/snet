package com.snet.buffer.block.impl;

import com.snet.Releasable;
import com.snet.buffer.block.DefBlock;
import com.snet.buffer.block.SNetBlock;
import com.snet.util.BPTreeMap;
import javafx.scene.control.Cell;

public class AreaBlock extends ProxyBlock implements Releasable {
	protected final byte[] treeMap;
	protected final byte[] depthMap;
	protected long lastUsingTime;
	protected int remaining;
	protected boolean released;

	public AreaBlock(AreaBlockArena arena, SNetBlock block) {
		super(arena, block);
		this.lastUsingTime = System.currentTimeMillis();
		this.remaining = block.getCapacity();
		this.released = false;

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
}
