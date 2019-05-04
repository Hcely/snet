package com.snet.core.session;

import com.snet.Destroyable;
import com.snet.core.frame.SNetFrame;

public interface SNetSession extends Destroyable {

	Object getSessionId();

	void send(SNetFrame<?> frame);

	SNetContext getContext();


}
