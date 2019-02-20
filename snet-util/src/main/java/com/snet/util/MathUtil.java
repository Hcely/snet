package com.snet.util;

public class MathUtil {
	public static int ceil2(int size) {
		size = size > 0 ? size : 1;
		return 1 << ceilLog2(size - 1);
	}

	public static int ceilLog2(int size) {
		return 32 - Integer.numberOfLeadingZeros(size - 1);
	}

	public static int ceilFactor(int size, int factor) {
		if (size < factor)
			return factor;
		int i = (size / factor) * factor;
		return i < size ? (i + factor) : i;
	}

	public static int camp(int i, int min, int max) {
		if (i < min)
			return min;
		if (i > max)
			return max;
		return i;
	}
}
