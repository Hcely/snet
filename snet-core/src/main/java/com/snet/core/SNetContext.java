package com.snet.core;

import com.snet.core.filter.SNetDataFilter;

public interface SNetContext extends SNetObject {
	void addFilter(String filterName, SNetDataFilter filter);

	SNetDataFilter removeFilter(String filterName);

	SNetDataFilter replaceFilter(String oldFilterName, String newFilterName, SNetDataFilter newFilter);
}
