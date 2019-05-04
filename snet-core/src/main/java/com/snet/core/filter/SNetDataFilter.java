package com.snet.core.filter;

import com.snet.core.session.SNetSession;
import com.snet.io.SNetBuffer;

public interface SNetDataFilter {

	boolean onEncode(SNetSession session, SNetBuffer buffer);

	SNetBuffer onDecode(SNetSession session, SNetBuffer buffer);
}
