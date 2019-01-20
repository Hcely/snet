package com.snet.core.coder;

import com.snet.core.SNetSession;
import com.snet.core.frame.SNetFrame;
import com.snet.io.SNetBuffer;

public interface SNetFrameDecoder<T extends SNetFrame<?>> {
	boolean enough(SNetSession session, SNetBuffer buffer);

	T decode(SNetSession session, SNetBuffer buffer);
}
