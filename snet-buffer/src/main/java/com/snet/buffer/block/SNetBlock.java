package com.snet.buffer.block;

import com.snet.buffer.resource.SNetResource;

public interface SNetBlock {
	int getCapacity();

	int getResourceOffset();

	boolean isReleased();

	SNetResource getResource();

	SNetBlockArena getArena();

	SNetBlock getParent();
}
