package com.snet.util.thread;

import com.snet.util.DefThreadCtrl;
import com.snet.util.FixedQueue;
import com.snet.util.ProducerConsumerBuffer;

import java.util.LinkedList;
import java.util.List;

public class ProducerConsumerTaskQueue extends DefThreadCtrl implements TaskQueue {
	protected ProducerConsumerBuffer<Object> queue;

	@Override
	public void add(Object task) {
		queue.add(task);
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public Object pop() {
		return queue.poll(0);
	}

	@Override
	public List<Object> remainTasks() {
		List<Object> remain = new LinkedList<>();
		Object task;
		while ((task = queue.poll(0)) != null)
			remain.add(task);
		return remain;
	}
}
