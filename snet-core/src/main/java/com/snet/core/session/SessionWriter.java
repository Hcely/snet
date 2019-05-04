package com.snet.core.session;

import com.snet.io.SNetBuffer;

public interface SessionWriter {
	void write(SNetSession session, SNetBuffer buffer);
}
