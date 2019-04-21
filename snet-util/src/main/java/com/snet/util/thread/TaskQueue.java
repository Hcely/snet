package com.snet.util.thread;

import java.util.List;

public interface TaskQueue extends ThreadCtrl {

	void add(Object task);

	int size();

	Object pop();

	List<Object> remainTasks();
}
