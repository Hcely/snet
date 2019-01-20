package com.snet.buffer.resource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferResource implements SNetBufferResource {
	protected AtomicInteger remainCount;
	protected ByteBuffer wBuffer;
	protected ByteBuffer rBuffer;

	public BufferResource(ByteBuffer buffer) {
		this.remainCount = new AtomicInteger(1);
		this.wBuffer = buffer;
		this.rBuffer = buffer.asReadOnlyBuffer();
	}

	private BufferResource(AtomicInteger remainCount, ByteBuffer wBuffer, ByteBuffer rBuffer) {
		this.remainCount = remainCount;
		this.wBuffer = wBuffer;
		this.rBuffer = rBuffer;
	}

	@Override
	public int getCapacity() {
		return wBuffer.capacity();
	}

	@Override
	public void write(int bufOff, byte b) {
		wBuffer.position(bufOff);
		wBuffer.put(b);
	}

	@Override
	public void write(int bufOff, byte[] buf, int off, int len) {
		wBuffer.position(bufOff);
		wBuffer.put(buf, off, len);
	}

	@Override
	public void write(int bufOff, ByteBuffer buf, int len) {
		wBuffer.position(bufOff);
		final int limit = buf.limit();
		buf.limit(buf.position() + len);
		wBuffer.put(buf);
		buf.limit(limit);
	}

	@Override
	public void write(int bufOff, SNetBufferResource buf, int off, int len) {
		if (buf == this) {
			wBuffer.position(bufOff).limit(bufOff + len);
			rBuffer.position(off).limit(off + len);
			wBuffer.put(rBuffer);
		} else {
			wBuffer.position(bufOff).limit(bufOff + len);
			buf.read(off, wBuffer);
		}
	}

	@Override
	public int write(int bufOff, ReadableByteChannel channel, int len) throws IOException {
		wBuffer.position(bufOff).limit(bufOff + len);
		return channel.read(wBuffer);
	}

	@Override
	public byte read(int bufOff) {
		wBuffer.position(bufOff);
		return wBuffer.get();
	}

	@Override
	public void read(int bufOff, byte[] buf, int off, int len) {
		wBuffer.position(bufOff);
		wBuffer.get(buf, off, len);
	}

	@Override
	public void read(int bufOff, ByteBuffer buf) {
		int len = buf.remaining();
		wBuffer.position(bufOff).limit(bufOff + len);
		buf.put(wBuffer);
	}

	@Override
	public void read(int bufOff, SNetBufferResource buf, int off, int len) {
		if (buf == this) {
			rBuffer.position(bufOff).limit(bufOff + len);
			wBuffer.position(off).limit(off + len);
			wBuffer.put(rBuffer);
		} else {
			wBuffer.position(bufOff).limit(bufOff + len);
			buf.write(off, wBuffer, len);
		}
	}

	@Override
	public int read(int bufOff, WritableByteChannel channel, int len) throws IOException {
		wBuffer.position(bufOff).limit(bufOff + len);
		return channel.write(wBuffer);
	}

	@Override
	public Object getRaw() {
		return wBuffer;
	}

	@Override
	public void release() {
		remainCount.decrementAndGet();
		wBuffer = null;
		rBuffer = null;
	}

	@Override
	public SNetBufferResource duplicate() {
		return new BufferResource(remainCount, wBuffer.duplicate(), wBuffer.asReadOnlyBuffer());
	}

	@Override
	public void retain() {
		remainCount.incrementAndGet();
	}

}
