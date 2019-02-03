package com.pj.snet;

/**
 * Unit test for simple App.
 */
public class AppTest {
	public static void main(String[] args) {
		int i = 0;
		System.out.println(i < (i = get(i)));
		System.out.println(i);
	}

	public static final int get(int i) {
		return i + 1;
	}
}