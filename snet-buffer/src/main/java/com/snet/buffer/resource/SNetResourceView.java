package com.snet.buffer.resource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface SNetResourceView {
	void write(int bufOff, byte b);

	default void write(int bufOff, byte[] buf) {
		write(bufOff, buf, 0, buf.length);
	}

	void write(int bufOff, byte[] buf, int off, int len);

	default void write(int bufOff, ByteBuffer buf) {
		write(bufOff, buf, buf.remaining());
	}

	void write(int bufOff, ByteBuffer buf, int len);

	default void write(int bufOff, SNetBufferResource buf) {
		write(bufOff, buf, 0, buf.getCapacity());
	}

	void write(int bufOff, SNetBufferResource buf, int off, int len);

	int write(int bufOff, ReadableByteChannel channel, int len) throws IOException;

	byte read(int bufOff);

	default void read(int bufOff, byte[] buf) {
		read(bufOff, buf, 0, buf.length);
	}

	void read(int bufOff, byte[] buf, int off, int len);

	void read(int bufOff, ByteBuffer buf);

	void read(int bufOff, SNetBufferResource buf, int off, int len);

	int read(int bufOff, WritableByteChannel channel, int len) throws IOException;
}
