package com.snet.util.thread;

import com.snet.util.DefThreadCtrl;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DefWorkQueue extends DefThreadCtrl implements WorkQueue {
	protected final ConcurrentLinkedQueue<Object> queue;
	protected final AtomicInteger count;

	public DefWorkQueue() {
		this.queue = new ConcurrentLinkedQueue<>();
		this.count = new AtomicInteger(0);
	}

	@Override
	public void add(Object task) {
		queue.add(task);
		if (count.incrementAndGet() == 1)
			notifyAllCount(4);
	}


	@Override
	public Object pop() {
		Object work = queue.poll();
		if (work != null)
			count.decrementAndGet();
		return work;
	}

	@Override
	public int size() {
		return count.get();
	}
}
