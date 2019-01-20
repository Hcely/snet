package com.snet.io;

import java.nio.ByteBuffer;

public interface BufferWriteView {
	int writeByte(byte b);

	int writeShort(short i);

	default int writeChar(char c) {
		return writeShort((short) c);
	}

	int writeInt(int i);

	int writeLong(long i);

	default int writeNumber(long i) {
		return BufferUtil.writeNumber(this, i);
	}

	default int writeFloat(float i) {
		return writeInt(Float.floatToRawIntBits(i));
	}

	default int writeDouble(double i) {
		return writeLong(Double.doubleToRawLongBits(i));
	}

	default int writeBuf(byte[] buf) {
		return writeBuf(buf, 0, buf.length);
	}

	int writeBuf(byte[] buf, int off, int len);

	default int writeBuf(ByteBuffer buf) {
		return writeBuf(buf, buf.remaining());
	}

	int writeBuf(ByteBuffer buf, int len);
}
