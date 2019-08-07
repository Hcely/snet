package com.snet.buffer;

public interface SNetResourceBlock {

	SNetResourceBlock getParent();

	int getCapacity();

	SNetResource getResource();

	long getResourceOff();
}
