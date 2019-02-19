package com.snet.test;

import io.netty.buffer.PooledByteBufAllocator;

public class Test01 {
	public static void main(String[] args) {
		PooledByteBufAllocator allocator =PooledByteBufAllocator.DEFAULT;
		allocator.buffer(10000);
	}
}
