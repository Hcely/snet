package com.snet.util;

public class MathUtil {
	public static int ceil2(int size) {
		size = size > 0 ? size : 1;
		int i = 32 - Integer.numberOfLeadingZeros(size - 1);
		return 1 << i;
	}

	public static int camp(int i, int min, int max) {
		if (i < min)
			return min;
		if (i > max)
			return max;
		return i;
	}
}
