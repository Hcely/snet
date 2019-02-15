package com.pj.snet;

import com.snet.util.RingBuffer;

import java.util.concurrent.atomic.AtomicLong;

public class RingTest {
	protected static final AtomicLong result = new AtomicLong(0);
	protected static final int SIZE = 128000000;

	public static void main(String[] args) throws InterruptedException {
		RingBuffer<Cell> buffer = new RingBuffer<>(2, 1 << 20, Cell::new);
		Producer[] producers = new Producer[4];

		for (int i = 0; i < 4; ++i)
			new Customer(1, buffer).start();
		for (int i = 0; i < 4; ++i)
			new Customer(2, buffer).start();

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

	protected static class Cell {
		protected int i;
	}

	protected static class Producer extends Thread {
		protected final RingBuffer<Cell> buffer;

		public Producer(RingBuffer<Cell> buffer) {
			this.buffer = buffer;
		}

		@Override
		public void run() {
			long l = System.currentTimeMillis(), id;
			RingBuffer<Cell> buffer = this.buffer;
			for (int i = 0; i < SIZE; ++i) {
				id = buffer.acquire(0);
				buffer.get(id).i = i;
				buffer.publish(0, id);
			}
			System.out.println(System.currentTimeMillis() - l);
		}
	}

	protected static class Customer extends Thread {
		protected final int state;
		protected final RingBuffer<Cell> buffer;

		public Customer(int state, RingBuffer<Cell> buffer) {
			this.state = state;
			this.buffer = buffer;

		}

		@Override
		public void run() {
			RingBuffer<Cell> buffer = this.buffer;
			long id;
			int state = this.state;
			while ((id = buffer.acquire(state)) != RingBuffer.DESTROY_ID) {
				if (id == RingBuffer.EMPTY_ID)
					continue;
				result.addAndGet(buffer.get(id).i);
				buffer.publish(state, id);
			}
		}
	}


}
