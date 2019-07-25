package com.snet.io;

import java.nio.ByteBuffer;

public class BufferUtil {
	public static void writeShort(byte[] buf, int pos, int v) {
		buf[pos++] = (byte) (0xFF & (v >>> 8));
		buf[pos] = (byte) (0xFF & v);
	}

	public static void writeInt(byte[] buf, int pos, int v) {
		buf[pos++] = (byte) (0xFF & (v >>> 24));
		buf[pos++] = (byte) (0xFF & (v >>> 16));
		buf[pos++] = (byte) (0xFF & (v >>> 8));
		buf[pos] = (byte) (0xFF & v);
	}

	public static void writeLong(byte[] buf, int pos, long v) {
		buf[pos++] = (byte) (0xFF & (v >>> 56));
		buf[pos++] = (byte) (0xFF & (v >>> 48));
		buf[pos++] = (byte) (0xFF & (v >>> 40));
		buf[pos++] = (byte) (0xFF & (v >>> 32));
		buf[pos++] = (byte) (0xFF & (v >>> 24));
		buf[pos++] = (byte) (0xFF & (v >>> 16));
		buf[pos++] = (byte) (0xFF & (v >>> 8));
		buf[pos] = (byte) (0xFF & v);
	}

	public static short readShort(byte[] buf, int pos) {
		short v = buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos];
		return v;
	}

	public static int readInt(byte[] buf, int pos) {
		int v = buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos];
		return v;
	}

	public static long readLong(byte[] buf, int pos) {
		long v = buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos++];
		v <<= 8;
		v |= 0xFF & buf[pos];
		return v;
	}

	public static int writeNumber(BufferWriteView view, long i) {
		if (i == Long.MIN_VALUE) {
			view.writeByte((byte) 1);
			return 1;
		}
		byte last = 0;
		int len = 1;
		if (i < 0) {
			last = 1;
			i = -1;
		}
		last |= (0x3f & i) << 1;
		i >>>= 6;
		i <<= Long.numberOfLeadingZeros(i);
		while (i != 0) {
			view.writeByte((byte) (0x80 | i >>> 57));
			i <<= 7;
			++len;
		}
		view.writeByte(last);
		return len;
	}

	public static int writeNumber(byte[] buf, int off, long i) {
		if (i == Long.MIN_VALUE) {
			buf[off] = 1;
			return 1;
		}
		byte last = 0;
		int len = 1;
		if (i < 0) {
			last = 1;
			i = -1;
		}
		last |= (0x3f & i) << 1;
		i >>>= 6;
		i <<= Long.numberOfLeadingZeros(i);
		while (i != 0) {
			buf[off] = (byte) (0x80 | i >>> 57);
			i <<= 7;
			++off;
			++len;
		}
		buf[off] = last;
		return len;
	}

	public static int writeNumber(ByteBuffer buf, long i) {
		if (i == Long.MIN_VALUE) {
			buf.put((byte) 1);
			return 1;
		}
		byte last = 0;
		int len = 1;
		if (i < 0) {
			last = 1;
			i = -1;
		}
		last |= (0x3f & i) << 1;
		i >>>= 6;
		i <<= Long.numberOfLeadingZeros(i);
		while (i != 0) {
			buf.put((byte) (0x80 | i >>> 57));
			i <<= 7;
			++len;
		}
		buf.put(last);
		return len;
	}

	public static long readNumber(BufferReadView view) {
		byte b = view.readByte();
		if (b == 1)
			return Long.MIN_VALUE;
		long result = 0;
		do {
			result <<= 7;
			result |= 0x7F & b;
		} while ((b = view.readByte()) < 0);
		result <<= 6;
		result |= 0x3F & (b >>> 1);
		return (b & 1) == 0 ? result : -result;
	}
}
