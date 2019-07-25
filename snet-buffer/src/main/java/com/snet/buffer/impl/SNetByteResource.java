package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceManager;

import java.nio.ByteBuffer;

public class SNetByteResource extends AbsSNetResource {
	protected byte[] buffer;

	public SNetByteResource(SNetResourceManager manager, int capacity) {
		super(manager, capacity);
	}

	@Override
	protected void initialize0() {
		buffer = new byte[capacity];
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
	public int write(int off, byte[] src, int srcOff, int srcLen) {
		int len = enableLength(off, srcLen);
		System.arraycopy(src, srcOff, this.buffer, off, len);
		return len;
	}

	@Override
	public int write(int off, ByteBuffer src, int srcLen) {
		int len = enableLength(off, srcLen);
		src.get(buffer, off, len);
		return len;
	}

	@Override
	public int read(int off, byte[] dst, int dstOff, int dstLen) {
		int len = enableLength(off, dstLen);
		System.arraycopy(buffer, off, dst, dstOff, len);
		return len;
	}

	@Override
	public int read(int off, ByteBuffer dst, int dstLen) {
		int len = enableLength(off, dstLen);
		dst.put(buffer, off, len);
		return len;
	}
}
