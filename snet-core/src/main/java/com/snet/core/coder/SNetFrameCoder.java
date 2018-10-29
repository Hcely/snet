package com.snet.core.coder;

import com.snet.core.frame.SNetFrame;

public interface SNetFrameCoder<T extends SNetFrame<?>> {
	SNetFrameDecoder<T> getDecoder();

	SNetFrameEncoder<T> getEncoder();
}
