package com.snet.util;

import java.util.Arrays;

public class HeapIntSet {
	protected int stepLength;
	protected int capacity;
	protected int[] heaps;
	protected int size;

	public void add(int i) {
		checkCapacity(i + size);
		heaps[size] = i;
		
	}

	public void checkCapacity(int size) {
		if (size > capacity) {
			int newCapacity = ArrayMap.getNewCapacity(size, stepLength);
			int[] array = Arrays.copyOf(heaps, newCapacity);
			heaps = array;
			capacity = newCapacity;
		}
	}
}
