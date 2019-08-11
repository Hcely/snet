package com.snet.buffer;

import com.snet.SNetObject;

public interface SNetResourceBlock extends SNetObject {

	SNetResourceBlock getParent();

	int getCapacity();

	SNetResource getResource();

	long getResourceOff();

}
