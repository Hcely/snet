package com.snet.core.coder;

import com.snet.core.session.SNetSession;
import com.snet.core.frame.SNetFrame;
import com.snet.io.SNetBuffer;

public interface SNetFrameEncoder<T extends SNetFrame<?>> {
	SNetBuffer encode(SNetSession session, T frame);
}
