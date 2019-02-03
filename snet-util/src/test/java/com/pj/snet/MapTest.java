package com.pj.snet;

import java.util.HashMap;
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
		Map<Integer, String> map = new HashMap<>();
		for (int i = 0; i < 1000000; ++i) {
			map.put(i, "str" + i);
		}
		long l = System.currentTimeMillis();
		for (int i = 0; i < 1000000; ++i) {
			map.get(i);
		}
		System.out.println(System.currentTimeMillis() - l);
	}
}
