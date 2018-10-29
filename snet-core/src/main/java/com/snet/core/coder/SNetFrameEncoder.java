package com.snet.core.coder;

import com.snet.buffer.BinaryBuffer;
import com.snet.core.SNetSession;
import com.snet.core.frame.SNetFrame;

public interface SNetFrameEncoder<T extends SNetFrame<?>> {
	BinaryBuffer encode(SNetSession session, T frame);
}
