package com.snet.buffer.impl;

import com.snet.buffer.util.SNetBufferUtil;
import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceManager;
import com.snet.buffer.exception.SNetBufferException;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbsSNetResource implements SNetResource {
	public static final int CREATED = 0;
	public static final int INITIALIZED = 1;
	public static final int DESTROYED = 2;
	protected final SNetResourceManager manager;
	protected final AtomicInteger sliceCount;
	protected volatile int state = CREATED;
	protected final int capacity;

	protected AbsSNetResource(SNetResourceManager manager, int capacity) {
		this(manager, new AtomicInteger(0), capacity);
	}

	protected AbsSNetResource(SNetResourceManager manager, AtomicInteger sliceCount, int capacity) {
		this.manager = manager;
		this.sliceCount = sliceCount;
		this.capacity = capacity;
	}


	@Override
	public void initialize() {
		if (state != CREATED)
			return;
		synchronized (this) {
			if (state != CREATED)
				return;
			state = INITIALIZED;
			initialize0();
		}
	}

	protected abstract void initialize0();

	@Override
	public SNetResource slice() {
		checkInitialized();
		final int i = sliceCount.incrementAndGet();
		return slice0(i);
	}

	@Override
	public void release() {
		checkInitialized();
		int i;
		if ((i = sliceCount.get()) < 1 || (i = sliceCount.decrementAndGet()) < 0) {
			throw new SNetBufferException("release error:" + i);
		}
		this.release0(i);
	}

	protected abstract SNetResource slice0(int count);

	@SuppressWarnings("unused")
	protected void release0(int count) {
	}

	@Override
	public SNetResourceManager getManager() {
		return manager;
	}

	@Override
	public int getCapacity() {
		return capacity;
	}

	@Override
	public void destroy() {
		if (state != INITIALIZED)
			return;
		synchronized (this) {
			if (state != INITIALIZED)
				return;
			state = DESTROYED;
			destroy0();
		}
	}

	protected abstract void destroy0();

	@Override
	protected void finalize() {
		this.destroy();
	}

	protected void checkInitialized() {
		if (state != INITIALIZED) {
			throw new SNetBufferException("resource is not initialized");
		}
	}

	protected int enableLength(int off, int len) {
		int remain = this.capacity - off;
		return remain < len ? remain : len;
	}

	@Override
	public int write(int off, SNetResource src, int srcOff, int srcLen) {
		Object srcRaw = src.getRawObject();
		if (srcRaw.getClass() == byte[].class) {
			return write(off, (byte[]) srcRaw, srcOff, srcLen);
		} else if (srcRaw.getClass() == ByteBuffer.class) {
			ByteBuffer srcBuffer = (ByteBuffer) srcRaw;
			srcBuffer.position(srcOff);
			srcBuffer.limit(srcOff + srcLen);
			return write(off, srcBuffer, srcLen);
		} else {
			return SNetBufferUtil.copy(src, srcOff, this, off, srcLen);
		}
	}

}
