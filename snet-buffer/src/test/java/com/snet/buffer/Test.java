package com.snet.buffer;

import java.nio.ByteBuffer;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		byte[] a = "12345678902222222222".getBytes();
		byte[] b=new byte[20];
		ByteBuffer buf = ByteBuffer.allocateDirect(20);
		ByteBuffer rbuf = buf.asReadOnlyBuffer();
		buf.put(a);
		rbuf.clear();
		rbuf.get(b);
		System.out.println(new String(b));
		rbuf.position(0).limit(10);
		buf.position(1).limit(11);
		buf.put(rbuf);
		rbuf.clear();
		rbuf.get(b);
		System.out.println(new String(b));
	}

}
