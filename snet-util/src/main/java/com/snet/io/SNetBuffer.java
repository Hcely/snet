package com.snet.io;

import com.snet.SNetReference;

public interface SNetBuffer extends BufferWriteView, BufferReadView, SNetReference {
	void ensureCapacity(int capacity);

	int getWritePos();

	void setWritePos(int pos);

	int getReadPos();

	void setReadPos(int pos);

	int getCapacity();

	int getRemaining();

	boolean hasRemaining();

	SNetBuffer slice();

}
