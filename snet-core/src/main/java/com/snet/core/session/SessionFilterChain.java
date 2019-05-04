package com.snet.core.session;

import com.snet.core.filter.SNetDataFilter;

public interface SessionFilterChain {
	void addFilter(String filterName, SNetDataFilter filter);

	SNetDataFilter removeFilter(String filterName);

	SNetDataFilter replaceFilter(String oldFilterName, String newFilterName, SNetDataFilter newFilter);
}
