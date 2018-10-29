package com.snet.core.filter;

import com.snet.buffer.BinaryBuffer;
import com.snet.core.SNetSession;

public interface SNetDataFilter {

	BinaryBuffer onEncode(SNetSession session, BinaryBuffer buffer);

	BinaryBuffer onDecode(SNetSession session, BinaryBuffer buffer);
}
