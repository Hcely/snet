package com.snet.util;

public class RuntimeUtil {
	public static final int CORE_PROCESSOR;
	public static final int DOUBLE_CORE_PROCESSOR;
	public static final int QUAD_CORE_PROCESSOR;

	public static final byte[] EMPTY_BYTES = {};
	public static final int[] EMPTY_INTS = {};
	public static final long[] EMPTY_LONGS = {};


	static {
		int i = Runtime.getRuntime().availableProcessors();
		i = i < 1 ? 1 : i;
		CORE_PROCESSOR = i;
		DOUBLE_CORE_PROCESSOR = i << 1;
		QUAD_CORE_PROCESSOR = i << 2;
	}
}
