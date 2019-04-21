package com.pj.snet;

import com.snet.util.ring.ProducerConsumerBuffer;

import java.util.concurrent.atomic.AtomicLong;

public class RingTest2 {
	protected static final AtomicLong result = new AtomicLong(0);
	protected static final int SIZE = 12800000;

	public static void main(String[] args) throws InterruptedException {
		ProducerConsumerBuffer<Integer> buffer = new ProducerConsumerBuffer<>(1 << 20);
		Producer[] producers = new Producer[4];

		for (int i = 0; i < 4; ++i)
			new Customer(buffer).start();
		for (int i = 0; i < producers.length; ++i)
			producers[i] = new Producer(buffer);
		for (Producer producer : producers)
			producer.start();
		for (Producer producer : producers)
			producer.join();

		buffer.destroy();
		Thread.sleep(4000);
		System.out.println(result.get());
	}

	protected static class Producer extends Thread {
		protected final ProducerConsumerBuffer<Integer> buffer;

		public Producer(ProducerConsumerBuffer<Integer> buffer) {
			this.buffer = buffer;
		}

		@Override
		public void run() {
			long l = System.currentTimeMillis(), id;
			ProducerConsumerBuffer<Integer> buffer = this.buffer;
			Integer val = 1;
			for (int i = 0; i < SIZE; ++i)
				buffer.add(val);
			System.out.println(System.currentTimeMillis() - l);
		}
	}

	protected static class Customer extends Thread {
		protected final ProducerConsumerBuffer<Integer> buffer;

		public Customer(ProducerConsumerBuffer<Integer> buffer) {
			this.buffer = buffer;

		}

		@Override
		public void run() {
			ProducerConsumerBuffer<Integer> buffer = this.buffer;
			Integer i;
			while ((i = buffer.poll()) != null) {
				result.addAndGet(i);
			}
		}
	}

}
