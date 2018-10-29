package com.snet.core.coder;

import com.snet.buffer.BinaryBuffer;
import com.snet.core.SNetSession;
import com.snet.core.frame.SNetFrame;

public interface SNetFrameDecoder<T extends SNetFrame<?>> {
	boolean enough(SNetSession session, BinaryBuffer buffer);

	T decode(SNetSession session, BinaryBuffer buffer);
}
