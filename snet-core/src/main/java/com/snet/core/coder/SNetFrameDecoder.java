package com.snet.core.coder;

import com.snet.core.session.SNetSession;
import com.snet.core.frame.SNetFrame;
import com.snet.io.BufferView;

public interface SNetFrameDecoder<T extends SNetFrame<?>> {
	boolean enough(SNetSession session, BufferView buffer);

	T decode(SNetSession session, BufferView buffer);
}
