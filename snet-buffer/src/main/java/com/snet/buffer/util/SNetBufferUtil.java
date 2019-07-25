package com.snet.buffer.util;

import com.snet.buffer.SNetResource;

import java.nio.ByteBuffer;

public class SNetBufferUtil {
	public static final int BUFFER_CACHE_SIZE = 512;
	private static final ThreadLocal<ByteBuffer> BUFFER_CACHE = ThreadLocal
			.withInitial(() -> ByteBuffer.allocateDirect(BUFFER_CACHE_SIZE));

	public static ByteBuffer getBufferCache() {
		ByteBuffer buffer = BUFFER_CACHE.get();
		buffer.clear();
		return buffer;
	}

	public static int copy(SNetResource src, int srcOff, SNetResource dst, int dstOff, int len) {
		int result = 0, cacheLen, cacheWrite;
		ByteBuffer cache = SNetBufferUtil.getBufferCache();
		while (len > 0) {
			cacheLen = len < BUFFER_CACHE_SIZE ? len : BUFFER_CACHE_SIZE;
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
