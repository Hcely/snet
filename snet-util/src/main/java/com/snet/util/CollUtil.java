package com.snet.util;

public class CollUtil {
	public static final int ceil2(int size) {
		int i = 32 - Integer.numberOfLeadingZeros(size - 1);
		return 1 << i;
	}

	public static final int camp(int i, int min, int max) {
		if (i < min)
			return min;
		if (i > max)
			return max;
		return i;
	}
}
