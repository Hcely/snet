package com.snet.core.session;

import com.snet.io.BufferView;

public interface SessionWriter {
	void write(SNetSession session, BufferView buffer);
}
