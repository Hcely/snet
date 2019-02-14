package com.snet.util.thread;

import com.snet.util.ThreadCtrl;

import java.util.List;

public interface TaskQueue extends ThreadCtrl {

	int add(Object task);

	int size();

	Object pop();

	List<Object> remainTasks();
}
