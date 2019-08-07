package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceManager;
import com.snet.buffer.exception.SNetBufferException;
import com.snet.buffer.util.SNetBufferUtil;
import com.snet.util.MathUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class FileResource extends AbsResource {
	protected final File file;
	protected final int channelLength;
	protected final int channelMask;
	protected FileChannel0[] channels;

	public FileResource(SNetResourceManager manager, File file, long capacity) {
		this(manager, file, capacity, 4);
	}

	public FileResource(SNetResourceManager manager, File file, long capacity, int channelLength) {
		super(manager, capacity);
		this.file = file;
		this.channelLength = MathUtil.ceil2(channelLength);
		this.channelMask = this.channelLength - 1;
	}

	@Override
	protected void initialize0() {
		try {
			final int len = channelLength;
			channels = new FileChannel0[len];
			for (int i = 0; i < len; ++i) {
				channels[i] = new FileChannel0(this);
			}
		} catch (IOException e) {
			throw new SNetBufferException("file io error", e);
		}
	}

	@Override
	protected SNetResource slice0(int count) {
		return this;
	}

	@Override
	protected void destroy0() {
		if (channels != null) {
			try {
				for (FileChannel0 channel : channels) {
					channel.close();
				}
			} catch (IOException ignored) {
			} finally {
				channels = null;
			}
		}
	}

	@Override
	public Object getRawObject() {
		return getChannel().channel;
	}

	@Override
	public int write(long off, byte[] src, int srcOff, int srcLen) {
		return write(off, ByteBuffer.wrap(src, srcOff, srcLen), srcLen);
	}

	@Override
	public int write(long off, ByteBuffer src, int srcLen) {
		final int len = enableLength(off, srcLen);
		int oldLimit = src.limit();
		FileChannel0 channel = getChannel();
		for (int remain = len, i; remain > 0; remain -= i, off += i) {
			i = remain < SNetBufferUtil.BUFFER_CACHE_SIZE ? SNetBufferUtil.BUFFER_CACHE_SIZE : remain;
			src.limit(src.position() + i);
			channel.write(src, off);
		}
		src.limit(oldLimit);
		return len;
	}


	@Override
	public int read(long off, byte[] dst, int dstOff, int dstLen) {
		return read(off, ByteBuffer.wrap(dst, dstOff, dstLen), dstLen);
	}

	@Override
	public int read(long off, ByteBuffer dst, int dstLen) {
		int len = enableLength(off, dstLen);
		int oldLimit = dst.limit();
		dst.limit(dst.position() + len);
		getChannel().read(dst, off);
		dst.limit(oldLimit);
		return len;
	}

	@Override
	public long write(long off, SNetResource src, long srcOff, long srcLen) {
		return SNetBufferUtil.copy(src, srcOff, this, off, srcLen);
	}

	public void force(boolean metaData) {
		for (FileChannel0 channel : channels) {
			if (channel.checkForce(metaData)) {
				break;
			}
		}
	}

	protected FileChannel0 getChannel() {
		return channels[(int) (Thread.currentThread().getId() & channelMask)];
	}

	protected static class FileChannel0 {
		protected final FileResource resource;
		protected final FileChannel channel;
		protected final AtomicInteger wroteCount;

		public FileChannel0(FileResource resource) throws IOException {
			this.resource = resource;
			this.channel = SNetBufferUtil.checkCreateChannel(resource.file, resource.capacity);
			this.wroteCount = new AtomicInteger(0);
		}

		public void write(ByteBuffer src, long position) {
			final int len = src.remaining();
			write0(src, position);
			thresholdForce(len);
		}

		public void write0(ByteBuffer src, long position) {
			try {
				channel.write(src, position);
			} catch (IOException e) {
				throw new SNetBufferException("file write error", e);
			}
		}

		protected boolean checkForce(boolean metaData) {
			if (wroteCount.get() > 0) {
				force(metaData);
				return true;
			}
			return false;
		}

		private void thresholdForce(int size) {
			if (size > SNetBufferUtil.BUFFER_CACHE_SIZE - 1
					|| wroteCount.addAndGet(size) > SNetBufferUtil.BUFFER_CACHE_SIZE - 1) {
				force(false);
			}
		}

		public void force(boolean metaData) {
			try {
				wroteCount.set(0);
				channel.force(metaData);
			} catch (IOException e) {
				throw new SNetBufferException("file force error", e);
			}
		}

		public void read(ByteBuffer dst, long position) {
			try {
				channel.read(dst, position);
			} catch (IOException e) {
				throw new SNetBufferException("file read error", e);
			}
		}

		public void close() throws IOException {
			channel.force(true);
			channel.close();
		}
	}
}
