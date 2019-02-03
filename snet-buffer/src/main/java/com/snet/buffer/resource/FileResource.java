package com.snet.buffer.resource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class FileResource implements SNetResource {
	protected long offset;
	protected int capacity;
	protected FileChannel fileChannel;
	protected AtomicInteger retainCount;

	@Override
	public void release() {
		if (retainCount.decrementAndGet() == 0)
			release0();
	}

	private void release0() {
		try {
			fileChannel.force(false);
			fileChannel.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			fileChannel = null;
		}
	}

	protected void write0(int bufOff, ByteBuffer buffer) {
		try {
			fileChannel.write(buffer, offset + bufOff);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void read0(int bufOff, ByteBuffer buffer) {
		try {
			int len = buffer.remaining();
			if (fileChannel.read(buffer, offset + bufOff) != len)
				throw new RuntimeException("file buffer capacity error");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void write(int bufOff, byte b) {
		ByteBuffer buffer = SNetResource.getCacheBuffer();
		buffer.put(b);
		buffer.flip();
		write0(bufOff, buffer);
	}

	@Override
	public void write(int bufOff, byte[] buf, int off, int len) {
		ByteBuffer buffer = SNetResource.getCacheBuffer();
		while (len > 0) {
			int l = CACHE_CAPACITY;
			if (len < l)
				l = len;
			buffer.clear();
			buffer.put(buf, off, l);
			buffer.flip();
			write0(bufOff, buffer);
			len -= l;
			off += l;
			bufOff += l;
		}
	}

	@Override
	public void write(int bufOff, ByteBuffer buf, int len) {
		final int limit = buf.limit();
		buf.limit(buf.position() + len);
		write0(bufOff, buf);
		buf.limit(limit);
	}

	@Override
	public void write(int bufOff, SNetResource buf, int off, int len) {
		Object rawBuf = buf.getRawObject();
		if (rawBuf instanceof byte[]) {
			write(bufOff, (byte[]) rawBuf, off, len);
		} else if (rawBuf instanceof ByteBuffer) {
			ByteBuffer b = (ByteBuffer) rawBuf;
			b.position(off).limit(off + len);
			write0(bufOff, b);
		} else {
			ByteBuffer buffer = SNetResource.getCacheBuffer();
			int l;
			while (len > 0) {
				l = len < CACHE_CAPACITY ? len : CACHE_CAPACITY;
				buffer.position(0).limit(l);
				buf.read(off, buffer);
				buffer.flip();
				write0(bufOff, buffer);
				len -= l;
				off += l;
				bufOff += l;
			}
		}
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
			write0(bufOff, buffer);
			len -= l;
			bufOff += l;
			result += l;
		}
		return result;
	}

	@Override
	public byte read(int bufOff) {
		ByteBuffer buffer = SNetResource.getCacheBuffer();
		buffer.position(0).limit(1);
		read0(bufOff, buffer);
		buffer.flip();
		return buffer.get();
	}

	@Override
	public void read(int bufOff, byte[] buf, int off, int len) {
		ByteBuffer buffer = SNetResource.getCacheBuffer();
		int l;
		while (len > 0) {
			l = len < CACHE_CAPACITY ? len : CACHE_CAPACITY;
			buffer.position(0).limit(l);
			read0(bufOff, buffer);
			buffer.flip();
			buffer.get(buf, off, l);
			len -= l;
			bufOff += l;
			off += l;
		}
	}

	@Override
	public void read(int bufOff, ByteBuffer buf) {
		read0(bufOff, buf);
	}

	@Override
	public void read(int bufOff, SNetResource buf, int off, int len) {
		Object rawBuf = buf.getRawObject();
		if (rawBuf instanceof byte[]) {
			read(bufOff, (byte[]) rawBuf, off, len);
		} else if (rawBuf instanceof ByteBuffer) {
			ByteBuffer b = (ByteBuffer) rawBuf;
			b.position(off).limit(off + len);
			read0(bufOff, b);
		} else {
			ByteBuffer buffer = SNetResource.getCacheBuffer();
			int l;
			while (len > 0) {
				l = len < CACHE_CAPACITY ? len : CACHE_CAPACITY;
				buffer.position(0).limit(l);
				read0(bufOff, buffer);
				buffer.flip();
				buf.write(off, buffer, l);
				len -= l;
				bufOff += l;
				off += l;
			}
		}
	}

	@Override
	public int read(int bufOff, WritableByteChannel channel, int len) throws IOException {
		ByteBuffer buffer = SNetResource.getCacheBuffer();
		int result = 0, l;
		while (len > 0) {
			l = len < CACHE_CAPACITY ? len : CACHE_CAPACITY;
			buffer.position(0).limit(l);
			read0(bufOff, buffer);
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
	public int getCapacity() {
		return capacity;
	}

	@Override
	public Object getRawObject() {
		return fileChannel;
	}

	@Override
	public SNetResource duplicate() {
		retain();
		return this;
	}

	@Override
	public boolean isReleased() {
		return fileChannel == null;
	}

	@Override
	public void retain() {
		retainCount.incrementAndGet();
	}
}
