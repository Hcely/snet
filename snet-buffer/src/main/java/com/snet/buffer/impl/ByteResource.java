package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceManager;

import java.nio.ByteBuffer;

public class ByteResource extends AbsResource {
	protected byte[] buffer;

	public ByteResource(SNetResourceManager manager, int capacity) {
		super(manager, capacity);
	}

	@Override
	protected void initialize0() {
		buffer = new byte[(int) capacity];
	}

	@Override
	protected SNetResource slice0(int count) {
		return this;
	}

	@Override
	protected void destroy0() {
		this.buffer = null;
	}

	@Override
	public Object getRawObject() {
		return buffer;
	}

	@Override
	public int write(long off, byte[] src, int srcOff, int srcLen) {
		int len = enableLength(off, srcLen);
		System.arraycopy(src, srcOff, this.buffer, (int) off, len);
		return len;
	}

	@Override
	public int write(long off, ByteBuffer src, int srcLen) {
		int len = enableLength(off, srcLen);
		src.get(buffer, (int) off, len);
		return len;
	}

	@Override
	public int read(long off, byte[] dst, int dstOff, int dstLen) {
		int len = enableLength(off, dstLen);
		System.arraycopy(buffer, (int) off, dst, dstOff, len);
		return len;
	}

	@Override
	public int read(long off, ByteBuffer dst, int dstLen) {
		int len = enableLength(off, dstLen);
		dst.put(buffer, (int) off, len);
		return len;
	}
}
