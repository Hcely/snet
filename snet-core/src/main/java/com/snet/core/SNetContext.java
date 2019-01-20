package com.snet.core;

import com.snet.core.filter.SNetDataFilter;

public interface SNetContext extends SNetObject {
	void addFilter(SNetDataFilter filter);

	void removeFilter(SNetDataFilter filter);

}
