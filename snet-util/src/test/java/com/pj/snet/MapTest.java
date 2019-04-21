package com.pj.snet;

import com.snet.util.coll.BPTreeMap;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class MapTest {

	public static void main(String[] args) {
		test();
		System.out.println("=====");
		test1();
	}

	protected static void test() {
		Map<Integer, String> map = new TreeMap<>(COMPARATOR);
		for (int i = 0; i < 2000000; ++i) {
			map.put(i, "str" + i);
		}
		for (int m = 0; m < 10; ++m) {
			long l = System.currentTimeMillis();
			for (int n = 0; n < 5; ++n)
				for (int i = 0; i < 2000000; ++i) {
					map.get(i);
				}
			System.out.println(System.currentTimeMillis() - l);
		}
	}

	protected static void test1() {
		Map<Integer, String> map = new BPTreeMap<>(2, (key, keyObj) -> key.compareTo((Integer) keyObj));
		for (int i = 0; i < 2000000; ++i) {
			map.put(i, "str" + i);
		}
		for (int m = 0; m < 10; ++m) {
			long l = System.currentTimeMillis();
			for (int n = 0; n < 5; ++n)
				for (int i = 0; i < 2000000; ++i) {
					map.get(i);
				}
			System.out.println(System.currentTimeMillis() - l);
		}
	}

	public static final Comparator<Integer> COMPARATOR = Integer::compareTo;
}
