package com.snet.buffer.block;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbsBlockArena implements SNetBlockArena {
	protected final Lock lock;

	public AbsBlockArena() {
		this.lock = new ReentrantLock();
	}

	public Lock getLock() {
		return lock;
	}
}
