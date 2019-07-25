package com.snet.core.coder;

import com.snet.core.session.SNetSession;
import com.snet.core.frame.SNetFrame;
import com.snet.io.BufferView;

public interface SNetFrameEncoder<T extends SNetFrame<?>> {
	BufferView encode(SNetSession session, T frame);
}
