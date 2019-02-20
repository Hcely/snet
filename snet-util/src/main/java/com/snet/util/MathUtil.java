package com.snet.util;

public class MathUtil {
	public static int ceil2(int i) {
		return i < 1 ? 1 : (1 << ceilLog2(i - 1));
	}

	public static int ceilLog2(int i) {
		return 32 - Integer.numberOfLeadingZeros(i - 1);
	}

	public static int floor2(int i) {
		return i < 1 ? 1 : (1 << floorLog2(i));
	}

	public static int floorLog2(int i) {
		return 31 - Integer.numberOfLeadingZeros(i);
	}

	public static int floorFactor(int i, int factor) {
		return (i / factor) * factor;
	}

	public static int ceilFactor(int i, int factor) {
		if (i > factor) {
			int result = (i / factor) * factor;
			return result < i ? (result + factor) : result;
		} else
			return factor;
	}

	public static int camp(int i, int min, int max) {
		if (i < min)
			return min;
		if (i > max)
			return max;
		return i;
	}
}
