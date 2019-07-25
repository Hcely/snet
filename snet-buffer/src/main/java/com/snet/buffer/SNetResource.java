package com.snet.buffer;

import com.snet.Destroyable;
import com.snet.Initializable;
import com.snet.Releasable;
import com.snet.Slicable;

import java.nio.ByteBuffer;

public interface SNetResource extends Initializable, Releasable, Destroyable, Slicable<SNetResource> {

	SNetResourceManager getManager();

	SNetResource slice();

	Object getRawObject();

	int getCapacity();

	int write(int off, byte[] src, int srcOff, int srcLen);

	default int write(int off, ByteBuffer src) {
		return write(off, src, src.remaining());
	}

	int write(int off, ByteBuffer src, int srcLen);

	int write(int off, SNetResource src, int srcOff, int srcLen);

	int read(int off, byte[] dst, int dstOff, int dstLen);

	default int read(int off, ByteBuffer dst) {
		return read(off, dst, dst.remaining());
	}

	int read(int off, ByteBuffer dst, int dstLen);
}
