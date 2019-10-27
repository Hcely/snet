package com.pj.snet;

import com.snet.util.coll.FixedQueue;

public class FixedQueueTest {
	protected static boolean b = true;
	//	protected static final AtomicLong v = new AtomicLong(0);
	protected static long v = 0;

	public static void main(String[] args) throws InterruptedException {
		FixedQueue<Long> queue = new FixedQueue<>(1 << 20);
		Product[] products = new Product[4];
		Worker[] workers = new Worker[4];
		for (int i = 0; i < 4; ++i) {
			products[i] = new Product(queue);
		}
		for (int i = 0; i < 4; ++i) {
			workers[i] = new Worker(queue);
		}
		long l = System.currentTimeMillis();
		for (Product p : products) {
			p.start();
		}
		for (Worker w : workers) {
			w.start();
		}
		for (Product p : products) {
			p.join();
		}
		b = false;
		for (Worker w : workers) {
			w.join();
		}
		System.out.println(System.currentTimeMillis() - l);
		System.out.println(v);

	}

	private static void add(long i) {
		v += i;
	}

	private static class Worker extends Thread {
		protected final FixedQueue<Long> queue;

		private Worker(FixedQueue<Long> queue) {
			this.queue = queue;
		}

		@Override
		public void run() {
			while (true) {
				Long v = queue.poll();
				if (v != null) {
					add(v);
				} else if (!b) {
					break;
				}
			}
		}
	}

	private static class Product extends Thread {
		protected final FixedQueue<Long> queue;

		private Product(FixedQueue<Long> queue) {
			this.queue = queue;
		}


		@Override
		public void run() {
			for (int i = 0; i < 12800000; ++i) {
				Long v = i + 1L;
				while (!queue.add(v)) {
					Thread.yield();
				}
			}
		}
	}


}
