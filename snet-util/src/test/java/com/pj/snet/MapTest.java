package com.pj.snet;

import com.snet.util.BPTreeMap;

import java.util.Comparator;
import java.util.Map;

public class MapTest {

	public static void main(String[] args) {
		test();
		test();
		test();
		test();
		test();
	}

	protected static void test() {
		Map<Integer, String> map = new BPTreeMap<>(COMPARATOR);
		for (int i = 0; i < 1000000; ++i) {
			map.put(i, "str" + i);
		}
		long l = System.currentTimeMillis();
		for (int i = 0; i < 1000000; ++i) {
			map.get(i);
		}
		System.out.println(System.currentTimeMillis() - l);
	}

	public static final Comparator<Integer> COMPARATOR = Integer::compareTo;
}
