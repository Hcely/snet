package com.snet.core;

import com.snet.Destroyable;
import com.snet.core.frame.SNetFrame;
import com.snet.io.SNetBuffer;

public interface SNetSession extends SNetObject, Destroyable {

	Object getSessionId();

	void send(SNetFrame<?> frame);

	SNetContext getContext();

	SNetBuffer readCache();
}
