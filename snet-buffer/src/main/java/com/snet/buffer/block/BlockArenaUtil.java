package com.snet.buffer.block;

import com.snet.util.MathUtil;

public class BlockArenaUtil {
	public static final int MIN_SHIFT = 7;
	public static final int MIN_CAPACITY = 1 << MIN_SHIFT;

	public static final BlockCache[] getCaches(int len, int minCapacity, int maxCapacity) {
		BlockCache[] caches = new BlockCache[len];
		for (int i = 0; i < len; ++i)
			caches[i] = new BlockCache(Math.min(minCapacity, maxCapacity >>> i));
		return caches;
	}

	public static final int normalCapacity(int capacity) {
		if (capacity < MIN_CAPACITY + 1)
			return MIN_CAPACITY;
		return MathUtil.ceil2(capacity);
	}
}
