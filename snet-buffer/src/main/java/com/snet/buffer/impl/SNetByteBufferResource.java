package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceManager;

import java.nio.ByteBuffer;

public class SNetByteBufferResource extends AbsSNetResource {
	protected final boolean direct;
	protected ByteBuffer buffer;

	public SNetByteBufferResource(SNetResourceManager manager, boolean direct, int capacity) {
		super(manager, capacity);
		this.direct = direct;
	}

	private SNetByteBufferResource(SNetByteBufferResource resource) {
		super(resource.manager, resource.sliceCount, resource.capacity);
		this.state = INITIALIZED;
		this.direct = resource.direct;
		this.buffer = resource.buffer;
	}

	public boolean isDirect() {
		return direct;
	}

	@Override
	protected void initialize0() {
		this.buffer = direct ? ByteBuffer.allocateDirect((int) capacity) : ByteBuffer.allocate((int) capacity);
	}

	@Override
	protected SNetResource slice0(int count) {
		return new SNetByteBufferResource(this);
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
		buffer.clear().position(off);
		buffer.put(src, srcOff, len);
		return len;
	}

	@Override
	public int write(int off, ByteBuffer src, int srcLen) {
		int len = enableLength(off, srcLen);
		buffer.clear().position(off);
		int srcLimit = src.limit();
		src.limit(src.position() + len);
		buffer.put(src);
		src.limit(srcLimit);
		return len;
	}

	@Override
	public int read(int off, byte[] dst, int dstOff, int dstLen) {
		int len = enableLength(off, dstLen);
		buffer.clear().position(off);
		buffer.get(dst, dstOff, len);
		return len;
	}

	@Override
	public int read(int off, ByteBuffer dst, int dstLen) {
		int len = enableLength(off, dstLen);
		buffer.clear().position(off).limit(off + len);
		dst.put(buffer);
		return len;
	}
}
