package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlock;

class ProvinceBlock extends BuddyBlock {
	protected ProvinceBlockList list;
	protected ProvinceBlock next, prev;

	public ProvinceBlock(AreaArena arena, SNetBlock block, int unitLen) {
		super(arena, block, unitLen);
	}

	public boolean remove() {
		final ProvinceBlockList list = this.list;
		if (list == null)
			return false;
		this.list = null;
		if (next == null) {
			
		} else {

		}
		if (prev == null) {

		} else {

		}
		return true;
	}
}
