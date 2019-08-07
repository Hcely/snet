package com.snet.buffer.util;

import com.snet.buffer.SNetResource;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class SNetBufferUtil {
	public static final int BUFFER_CACHE_SIZE = 1024 * 4;
	private static final ThreadLocal<ByteBuffer> BUFFER_CACHE = ThreadLocal
			.withInitial(() -> ByteBuffer.allocateDirect(BUFFER_CACHE_SIZE));

	public static ByteBuffer getBufferCache() {
		ByteBuffer buffer = BUFFER_CACHE.get();
		buffer.clear();
		return buffer;
	}

	public static FileChannel checkCreateChannel(File file, long size) throws IOException {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
		}
		FileChannel channel = FileChannel
				.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
		if (channel.size() < size) {
			ByteBuffer buffer = getBufferCache();
			buffer.limit(1);
			channel.position(size - 1);
			channel.write(buffer);
		}
		return channel;
	}

	public static long copy(SNetResource src, long srcOff, SNetResource dst, long dstOff, long len) {
		long result = 0;
		int cacheLen, cacheWrite;
		ByteBuffer cache = SNetBufferUtil.getBufferCache();
		while (len > 0) {
			cacheLen = len < BUFFER_CACHE_SIZE ? (int) len : BUFFER_CACHE_SIZE;
			src.read(srcOff, cache, cacheLen);
			cache.flip();
			cacheWrite = dst.write(dstOff, cache);
			result += cacheWrite;
			if (cacheWrite < cacheLen) {
				len = 0;
			} else {
				len -= cacheWrite;
				srcOff += cacheWrite;
				dstOff += cacheWrite;
				cache.clear();
			}
		}
		return result;
	}
}
