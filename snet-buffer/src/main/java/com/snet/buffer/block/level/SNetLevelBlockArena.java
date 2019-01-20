package com.snet.buffer.block.level;

import com.snet.buffer.block.SNetBlockArena;

public interface SNetLevelBlockArena extends SNetBlockArena {
	SNetBlockArena getParent();
}
