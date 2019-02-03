package com.snet.buffer.resource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class BytesResource implements SNetResource {
	protected byte[] buffer;
	protected AtomicInteger retainCount;

	public BytesResource(byte[] buffer) {
		this.buffer = buffer;
		this.retainCount = new AtomicInteger(1);
	}

	@Override
	public int getCapacity() {
		return buffer.length;
	}

	@Override
	public void write(int bufOff, byte b) {
		buffer[bufOff] = b;
	}

	@Override
	public void write(int bufOff, byte[] buf, int off, int len) {
		System.arraycopy(buf, off, buffer, bufOff, len);
	}

	@Override
	public void write(int bufOff, ByteBuffer buf, int len) {
		buf.get(buffer, bufOff, len);
	}

	@Override
	public void write(int bufOff, SNetResource buf, int off, int len) {
		buf.read(off, buffer, bufOff, len);
	}

	@Override
	public int write(int bufOff, ReadableByteChannel channel, int len) throws IOException {
		ByteBuffer buffer = SNetResource.getCacheBuffer();
		int result = 0, l;
		while (len > 0) {
			l = len < CACHE_CAPACITY ? len : CACHE_CAPACITY;
			buffer.position(0).limit(l);
			if ((l = channel.read(buffer)) == -1)
				break;
			buffer.flip();
			buffer.get(this.buffer, bufOff, l);
			len -= l;
			bufOff += l;
			result += l;
		}
		return result;
	}

	@Override
	public byte read(int bufOff) {
		return buffer[bufOff];
	}

	@Override
	public void read(int bufOff, byte[] buf, int off, int len) {
		System.arraycopy(buffer, bufOff, buf, off, len);
	}

	@Override
	public void read(int bufOff, ByteBuffer buf) {
		int len = buf.remaining();
		buf.put(buffer, bufOff, len);
	}

	@Override
	public void read(int bufOff, SNetResource buf, int off, int len) {
		buf.write(off, buffer, bufOff, len);
	}

	@Override
	public Object getRawObject() {
		return buffer;
	}

	@Override
	public int read(int bufOff, WritableByteChannel channel, int len) throws IOException {
		ByteBuffer buffer = SNetResource.getCacheBuffer();
		int result = 0, l;
		while (len > 0) {
			l = len < CACHE_CAPACITY ? len : CACHE_CAPACITY;
			buffer.position(0).limit(l);
			buffer.put(this.buffer, bufOff, l);
			buffer.flip();
			if ((l = channel.write(buffer)) < 1)
				break;
			len -= l;
			bufOff += l;
			result += l;
		}
		return result;
	}

	@Override
	public void release() {
		if (retainCount.decrementAndGet() == 0)
			buffer = null;
	}

	@Override
	public SNetResource duplicate() {
		retain();
		return this;
	}

	@Override
	public boolean isReleased() {
		return buffer == null;
	}

	@Override
	public void retain() {
		retainCount.incrementAndGet();
	}

}
