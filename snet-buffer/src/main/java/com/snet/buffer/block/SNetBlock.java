package com.snet.buffer.block;

import com.snet.Releasable;
import com.snet.buffer.resource.SNetResource;

public interface SNetBlock extends Releasable {
	int getCapacity();

	int getResourceOffset();

	SNetResource getResource();

	SNetBlockArena getArena();

	SNetBlock getParent();
}
