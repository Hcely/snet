package com.snet.buffer.block;

import com.snet.buffer.resource.SNetBufferResource;

public interface SNetBufferBlock {
	int getCapacity();

	int getResourceOffset();

	boolean isReleased();

	SNetBufferResource getResource();

	SNetBlockArena getArena();

	SNetBufferBlock getParent();
}
