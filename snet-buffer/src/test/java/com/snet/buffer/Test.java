package com.snet.buffer;

import com.snet.buffer.util.SNetBufferUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Test {

	public static void main(String[] args) throws IOException {
		File file = new File("d:/test.txt");
		FileChannel channel1 = SNetBufferUtil.checkCreateChannel(file, 4 * 1024);
		FileChannel channel2 = SNetBufferUtil.checkCreateChannel(file, 4 * 1024);
		byte[] data1 = "abc".getBytes();
		channel1.write(ByteBuffer.wrap(data1), 0);
		channel2.write(ByteBuffer.wrap(data1), 4);
	}

}
