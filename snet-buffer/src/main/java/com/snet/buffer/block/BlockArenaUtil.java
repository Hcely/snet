package com.snet.buffer.block;

public class BlockArenaUtil {
	public static final int MIN_SHIFT = 8;
	public static final int MIN_CAPACITY = 1 << MIN_SHIFT;

	public static final int getIdx(int capacity) {
		if (capacity < MIN_CAPACITY + 1)
			return 0;
		int i = 32 - Integer.numberOfLeadingZeros(capacity - 1);
		return i - MIN_SHIFT;
	}
}
