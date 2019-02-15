package com.snet.util.thread;

import com.snet.util.DefThreadCtrl;
import com.snet.util.RuntimeUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DefTaskQueue extends DefThreadCtrl implements TaskQueue {
	protected final ConcurrentLinkedQueue<Object> queue;
	protected final AtomicInteger count;

	public DefTaskQueue() {
		this.queue = new ConcurrentLinkedQueue<>();
		this.count = new AtomicInteger(0);
	}

	@Override
	public void add(Object task) {
		queue.add(task);
		if (count.incrementAndGet() == 1)
			notifyAllCount(RuntimeUtil.DOUBLE_CORE_PROCESSOR);
	}

	@Override
	public Object pop() {
		Object work = queue.poll();
		if (work != null)
			count.decrementAndGet();
		return work;
	}

	@Override
	public List<Object> remainTasks() {
		List<Object> remain = new LinkedList<>(queue);
		queue.clear();
		return remain;
	}

	@Override
	public int size() {
		return count.get();
	}
}
