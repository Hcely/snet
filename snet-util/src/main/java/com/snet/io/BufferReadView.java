package com.snet.io;

import java.nio.ByteBuffer;

public interface BufferReadView {
	byte readByte();

	short readShort();

	default char readChar() {
		return (char) readShort();
	}

	int readInt();

	long readLong();

	default long readNumber() {
		return BufferUtil.readNumber(this);
	}

	default float readFloat() {
		return Float.intBitsToFloat(readInt());
	}

	default double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	default int readBuf(byte[] buf) {
		return readBuf(buf, 0, buf.length);
	}

	int readBuf(byte[] buf, int off, int len);

	default int readBuf(ByteBuffer buf) {
		return readBuf(buf, buf.remaining());
	}

	int readBuf(ByteBuffer buf, int len);
}
