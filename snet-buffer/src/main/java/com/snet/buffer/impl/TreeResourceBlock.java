package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceBlock;

public class TreeResourceBlock extends DefResourceBlock {
	public TreeResourceBlock(SNetResourceBlock parent, SNetResource resource, long resourceOff, int capacity) {
		super(parent, resource, resourceOff, capacity);
	}
}
