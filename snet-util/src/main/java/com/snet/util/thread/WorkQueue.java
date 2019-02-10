package com.snet.util.thread;

import com.snet.util.ThreadCtrl;

public interface WorkQueue extends ThreadCtrl {

	void add(Object work);


	int size();

	Object pop();
}
