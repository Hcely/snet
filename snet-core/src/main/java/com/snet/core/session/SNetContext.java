package com.snet.core.session;

import com.snet.util.coll.EntryPlus;

public interface SNetContext {
	default EntryPlus<String, Object> attribute(String name) {
		return attribute(name, true);
	}

	EntryPlus<String, Object> attribute(String name, boolean absentCreate);

	SessionFilterChain getFilterChain();

	void setFilterChain(SessionFilterChain filterChain);

	SessionReader getReader();

	SessionWriter getWriter();
}
