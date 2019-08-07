package com.snet.buffer;

import com.snet.Referencable;
import com.snet.io.BufferView;

public interface SNetBuffer extends BufferView, Referencable {
	void ensureCapacity(int capacity);

	int getWritePos();

	void setWritePos(int pos);

	int getReadPos();

	void setReadPos(int pos);

	int getCapacity();

	default int getRemaining() {
		return getWritePos() - getReadPos();
	}

	default boolean hasRemaining() {
		return getWritePos() > getReadPos();
	}

	int writeBuf(SNetBuffer buf, int len);

	int readBuf(SNetBuffer buf, int len);

	SNetBuffer slice();
}
