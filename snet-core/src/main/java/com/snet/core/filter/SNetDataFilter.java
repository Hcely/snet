package com.snet.core.filter;

import com.snet.core.session.SNetSession;
import com.snet.io.BufferView;

public interface SNetDataFilter {

	boolean onEncode(SNetSession session, BufferView buffer);

	BufferView onDecode(SNetSession session, BufferView buffer);
}
