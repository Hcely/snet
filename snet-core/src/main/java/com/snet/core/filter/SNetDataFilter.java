package com.snet.core.filter;

import com.snet.core.SNetSession;
import com.snet.io.SNetBuffer;

public interface SNetDataFilter {

	SNetBuffer onEncode(SNetSession session, SNetBuffer buffer);

	SNetBuffer onDecode(SNetSession session, SNetBuffer buffer);
}
