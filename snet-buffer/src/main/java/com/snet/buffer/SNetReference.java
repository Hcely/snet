package com.snet.buffer;

import com.snet.Releasable;

public interface SNetReference extends Releasable {
	void retain();
}
