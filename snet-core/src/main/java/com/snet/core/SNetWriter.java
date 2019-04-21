package com.snet.core;

import com.snet.io.SNetBuffer;

public interface SNetWriter {
	void write(SNetSession session, SNetBuffer buffer);
}
